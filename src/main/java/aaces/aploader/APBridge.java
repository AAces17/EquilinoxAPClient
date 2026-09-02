package aaces.aploader;

import aaces.aploader.client.APClient;
import aaces.aploader.client.ItemManager;
import aaces.aploader.client.LocationManager;
import audio.SoundMaestro;
import blueprints.Blueprint;
import componentArchitecture.ComponentType;
import events.EventData;
import events.EventManager;
import extraInfoGui.ExtraInfoContent;
import extraInfoGui.ExtraInfoGui;
import extraInfoGui.ExtraToolbarGui;
import fontRendering.Text;
import gameManaging.GameManager;
import gameManaging.GameState;
import gameMenu.DnaButtonGui;
import gameMenu.GameMenuBackground;
import gameMenu.GameMenuGui;
import gameMenu.MenuPanelGui;
import gridLayout.ItemPageGui;
import guiRendering.GuiRenderData;
import guis.GuiComponent;
import guis.GuiTexture;
import health.LifeCompBlueprint;
import io.github.archipelagomw.APResult;
import languages.GameText;
import mainGuis.*;
import org.lwjgl.util.vector.Vector2f;
import saves.SaveSlot;
import saves.Saves;
import session.GameMode;
import session.Session;
import session.SessionManager;
import shopping.BlueprintShopItem;
import shops.LockedRequirementUi;
import shops.ShopItem;
import shops.ShopItemGui;
import taskUi.TaskInfoFrameUi;
import tasks.Task;
import tasks.TaskState;
import textures.Texture;
import unlockGuide.EvolveChainUi;
import unlockGuide.UnlockGuideUi;
import userInterfaces.ClickListener;
import userInterfaces.GuiImage;
import userInterfaces.TitlePanelUi;
import utils.BinaryReader;
import utils.BinaryWriter;
import utils.MyFile;
import world.WorldConfigs;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class APBridge {

    private static final String gameVersion = "1.7.2";
    private static final String modVersion = "0.0.1";

    public static String host = "";
    public static String slot = "";
    public static String password = "";

    private static ItemManager itemManager = null;
    private static LocationManager locationManager = null;
    private static APClient client;

    private static boolean inGameInitialized = false;
    public static boolean saveLoaded = false;

    // ----------------------------------------------------------------
    // Methods called by transformers to overwrite/change game behavior
    // ----------------------------------------------------------------

    /// Called on game init. Stops the game from automatically loading the previous save.
    /// This is achieved by creating and loading a new world in an invisible SaveSlot (slot 11).
    /// If the game tries to save this dummy world, it throws an error (but does not crash).
    /// The game is prevented from saving this dummy session by {@link saveOverride}
    ///
    /// @param sm the SessionManager instance being intercepted
    public static void gameManagerInit(SessionManager sm) {
        APAgent.log("DEBUG: GameManager.init() called");
        //sm.loadInitialSession();
        try{
            Field SAVESField = SessionManager.class.getDeclaredField("SAVES");
            SAVESField.setAccessible(true);
            Saves SAVES = (Saves) SAVESField.get(sm);

            Constructor<SaveSlot> saveSlotConstructor = SaveSlot.class.getDeclaredConstructor(int.class, String.class, Saves.class);
            saveSlotConstructor.setAccessible(true);

            Method setSession = SessionManager.class.getDeclaredMethod("setSession", Session.class);
            setSession.setAccessible(true);

            SaveSlot newSaveSlot = saveSlotConstructor.newInstance(11, "Select/Create Your AP Save", SAVES);
            WorldConfigs configs = WorldConfigs.createDefault();
            setSession.invoke(sm, Session.createNewSession(newSaveSlot, configs, GameMode.NORMAL));
        } catch (Exception e) {
            APAgent.log("\t"+e.getMessage());
            e.printStackTrace();
        }
    }

    /// Called every game tick. The way the game detects entering the game needs to be changed.
    public static void gameManagerUpdate() {
        GameState state = GameManager.getGameState();
        if(state == null){
            if(!inGameInitialized){
                inGameInit();
            }
        }
    }

    /// Called when the main menu loads. Replaces the "New World" button
    /// with an Archipelago button.
    ///
    /// @param menuPanelGui the GUI we need to modify
    public static void menuPanelGuiInit(MenuPanelGui menuPanelGui){

        try{
            Field buttonsField = MenuPanelGui.class.getDeclaredField("buttons");
            Field gameMenuField = MenuPanelGui.class.getDeclaredField("gameMenu");
            Field superMenuField = MenuPanelGui.class.getDeclaredField("superMenu");
            Field newWorldButtonField = MenuPanelGui.class.getDeclaredField("newWorldButton");

            newWorldButtonField.setAccessible(true);
            superMenuField.setAccessible(true);
            gameMenuField.setAccessible(true);
            buttonsField.setAccessible(true);

            GameMenuGui gameMenu = (GameMenuGui) gameMenuField.get(menuPanelGui);
            GameMenuBackground superMenu = (GameMenuBackground) superMenuField.get(menuPanelGui);

            DnaButtonGui newWorldButton = (DnaButtonGui) newWorldButtonField.get(menuPanelGui);

            Method addButton = MenuPanelGui.class.getDeclaredMethod("addButton", int.class, GuiTexture.class, String.class, ClickListener.class);
            addButton.setAccessible(true);

            ClickListener cl = event -> {
                if (event.isLeftClick()) {
                    APAgent.log("Opening AP gui");
                    gameMenu.setNewSecondaryScreen(new ArchipelagoGui(gameMenu, superMenu));
                }
            };

            menuPanelGui.removeComponent(newWorldButton);

            DnaButtonGui APButton = (DnaButtonGui) addButton.invoke(menuPanelGui, 1, (new GuiImage(GuiRepository.LINES[3])).getTexture(), "New World (Archipelago)", cl);

            @SuppressWarnings("unchecked")
            List<DnaButtonGui> buttons = (List<DnaButtonGui>) buttonsField.get(menuPanelGui);

            buttons.add(APButton);
            buttons.remove(newWorldButton);

            buttonsField.set(menuPanelGui, buttons);

        } catch (Exception e){
            APAgent.log("Error adding AP button!");
            APAgent.log("\t"+e.getMessage());
            e.printStackTrace();
        }
    }

    /// Adds the mod version to the game version on the main menu.
    ///
    /// @param gameMenuGui the GUI we need to modify
    public static void addVersionText(GameMenuGui gameMenuGui){
        //APAgent.log("set version method called with \n\t" + gameMenuGui.toString());
        Text text = Text.newText("Version " + gameVersion + " -- EqAP Version " + modVersion).setFontSize(UiSettings.LARGE_FONT).create();
        text.setColour(ColourPalette.LIGHT_GREY);
        gameMenuGui.addText(text, 0.6F, 0.3F, 1.0F);
    }

    /// Called when the game quits. Used for cleanup.
    public static void onApplicationQuit(){
        APClient.tryDisconnect();
    }

    /// Replaces the internal task completion handling. Used to prevent
    /// the game from distributing the normal rewards, and calls {@link trySendTaskCheck}
    /// to send the proper archipelago check.
    ///
    /// @param task the task completed
    public static void onTaskComplete(Task task){
        APAgent.log("task completed: " + task.name);
        //Original complete method:
//        SoundMaestro.playSystemSound(GuiSounds.CASH);
//        this.state = TaskState.COMPLETE;
//
//        for(Reward reward : task.rewards) {
//            reward.payOut();
//        }
//
//        if (this.repeatable) {
//            if (!this.autoCollect) {
//                EventManager.TASK_COMPLETE.registerEvent(new EventData(), new String[]{Integer.toString(this.id)});
//            }
//
//            this.autoCollect = true;
//            this.state = TaskState.IN_PROGRESS;
//
//            for(TaskRequirement requirement : this.requirements) {
//                requirement.reset(GameManager.getSession());
//            }
//        } else {
//            EventManager.TASK_COMPLETE.registerEvent(new EventData(), new String[]{Integer.toString(this.id)});
//        }
        SoundMaestro.playSystemSound(GuiSounds.CASH);

        try {
            Field stateField = Task.class.getDeclaredField("state");
            stateField.setAccessible(true);
            stateField.set(task, TaskState.COMPLETE);

            Field idField = Task.class.getDeclaredField("id");
            idField.setAccessible(true);
            int id = (int) idField.get(task);

            trySendTaskCheck(task);

            EventManager.TASK_COMPLETE.registerEvent(new EventData(), Integer.toString(id));
        } catch (Exception e) {
            APAgent.log("Error on task ("+task.name+") completion button!");
            APAgent.log("\t"+e.getMessage());
            e.printStackTrace();
        }

    }

    /// Replaces the listed rewards for completing a task with "Unlocks an Archipelago Item".
    ///
    /// @param taskInfoFrameUi the ui element we add the text to
    public static void rewardsPanelIntercept(TaskInfoFrameUi taskInfoFrameUi){
        TitlePanelUi rewardPanel = new TitlePanelUi(GameText.getText(149), new GuiComponent() {
            @Override
            protected void updateGuiTexturePositions(Vector2f vector2f, Vector2f vector2f1) {
            }

            @Override
            protected void updateSelf() {
            }

            @Override
            protected void getGuiTextures(GuiRenderData guiRenderData) {
            }

            protected void init() {
                Text text = Text.newText("- Unlocks an Archipelago Item").indent().create();
                text.setColour(ColourPalette.WHITE);
                addText(text, 0.0F, 0.0F, 1.0F);
            }
        });
        taskInfoFrameUi.addComponent(rewardPanel, ExtraToolbarGui.X_START, 0.6358824F, 1.0F - 2.0F * ExtraToolbarGui.X_START, 0.23F);
    }

    /// Called when a locked shop item is clicked on.
    /// Changes the displayed "unlock guide" on plants/animals to show that the
    /// item is in the multiworld, not locked behind a task. Still shows the normal
    /// evolution tree, since only base species are locked in the multiworld.
    ///
    /// @param extraInfoGui the existing GUI. We first close the old one, and then create our own
    /// @param item the shop item whose guide is being modified
    public static void reactToLockedClick(ExtraInfoGui extraInfoGui, BlueprintShopItem item){
        extraInfoGui.close();
        ExtraInfoContent c = new ExtraInfoContent() {
            @Override
            protected void updateGuiTexturePositions(Vector2f vector2f, Vector2f vector2f1) {
            }

            @Override
            protected void updateSelf() {
            }

            @Override
            protected void getGuiTextures(GuiRenderData guiRenderData) {
            }

            private float yPos = 0.0f;

            protected void init() {
                super.init();
                this.yPos += super.pixelsToRelativeY(25.0F);
                this.addLockImage();
                if (item.hasLinkedTask()) {
                    this.addAPSection(true);
                } else {
                    Blueprint baseSpecies = this.getBaseSpecies(item.getBlueprint());
                    BlueprintShopItem baseItem = (BlueprintShopItem)GameManager.getShops().getItem(baseSpecies.getId());
                    if (baseItem.isLocked()) {
                        this.addAPSection(false);
                    }

                    this.addEvolveSection(!baseItem.isLocked());
                }
            }
            private void addAPSection(boolean baseSpecies){
                Text text = Text.newText(baseSpecies ? "Find this species in the Multiworld!" : "Find this species' base species in the Multiworld!").center().setFontSize(UiSettings.NORM_FONT).create();
                text.setColour(ColourPalette.WHITE);
                super.addText(text, 0.0F, this.yPos, 1.0F);
                this.yPos += super.pixelsToRelativeY(120.0F);
            }
            private void addEvolveSection(boolean onlyEvolve) {
                this.addEvolveSectionTitle(onlyEvolve);
                EvolveChainUi chainUi = new EvolveChainUi(item.getBlueprint());
                super.addComponent(chainUi, 0.0F, this.yPos, 1.0F, 1.0F - this.yPos);
            }

            private void addEvolveSectionTitle(boolean onlyEvolve) {
                Text text = Text.newText(onlyEvolve ? GameText.getText(1121) : GameText.getText(1122)).center().setFontSize(UiSettings.NORM_FONT).create();
                text.setColour(ColourPalette.WHITE);
                super.addText(text, 0.0F, this.yPos, 1.0F);
                this.yPos += super.pixelsToRelativeY(35.0F);
            }
            private Blueprint getBaseSpecies(Blueprint blueprint) {
                LifeCompBlueprint lifeComp = (LifeCompBlueprint)blueprint.getComponent(ComponentType.LIFE);
                Blueprint parent = lifeComp.breedInfo.getParent();
                return parent != null ? this.getBaseSpecies(parent) : blueprint;
            }
            private void addLockImage() {
                GuiImage lockImage = new GuiImage(GuiRepository.LOCK_ICON);
                float height = super.pixelsToRelativeY(55.0F);
                super.addCenteredComponentX(lockImage, 0.5F, this.yPos, height);
                this.yPos += height + super.pixelsToRelativeY(10.0F);
            }
        };
        extraInfoGui.display(GameText.getText(1120), new ArrayList(), new ArrayList(), c);
    }

    /// Called when the game tries to save. We add the room/slot info, the
    /// received item index, and the queued-but-not-yet-sent location checks
    /// to the end of the save file.
    public static void saveAPState(BinaryWriter writer){
        APAgent.log("Trying to save the game");
        try {
            // Save the AP connection info
            writer.writeString(host);
            writer.writeString(slot);
            writer.writeString(password);

            if(itemManager != null) {
                // Save the current item index to avoid receiving duplicates on reconnect
                writer.writeInt(itemManager.getIndex());
            }

            if(locationManager != null) {
                // Save any queued-but-not-yet-sent location checks
                writer.writeInt(locationManager.numQueuedLocations());
                while (locationManager.nonemptyQueue()) {
                    writer.writeInt(locationManager.popOldestLocation());
                }
            }
        } catch (IOException e) {
            APAgent.log("Couldn't save AP info!");
            APAgent.log("\t"+e.getMessage());
            e.printStackTrace();
        }
    }

    /// Called when the game tries to load a save file.
    /// Tries to load the previously saved AP things from a save file. If the save
    /// file does not contain the expected data, connecting to a room with this save file
    /// will result in every item being received again.
    public static void loadAPState(BinaryReader reader){
        APAgent.log("trying to load saved ap info");
        saveLoaded = true;
        try {
            host = reader.readString();
            slot = reader.readString();
            password = reader.readString();

            itemManager = new ItemManager(reader.readInt());

            int numSavedLocs = reader.readInt();
            locationManager = new LocationManager();
            for(int i = 0; i < numSavedLocs; i++){
                locationManager.queueLocation(reader.readInt());
            }

            //APAgent.log("Trying to connect: host: "+host+", slot: "+slot+", password: "+password + ", item index: "+itemManager.getIndex());
            //client = APClient.tryConnect(host, slot, password, itemManager, locationManager);
        } catch (Exception e) {
            APAgent.log("couldn't load the saved state (this can happen if trying to load a world created without this mod installed)");
            APAgent.log("\t"+e.getMessage());
            e.printStackTrace();

            itemManager = new ItemManager(0);
            locationManager = new LocationManager();
        }
    }

    /// Called when the SessionManager tries to save the currently loaded Session. Stops it from saving
    /// the dummy save created by {@link gameManagerInit} on startup.
    ///
    /// @param session the Session we are trying to save
    /// @return true if the dummy Session is still the current session, or if a non-dummy current session successfully saves
    public static boolean saveOverride(Session session){
        if (saveLoaded) {
            return session.save();
        } else {
            return true;
        }
    }

    // -------------------------------------------------
    // Internal/helper methods for this class or the mod
    // -------------------------------------------------

    /// Handles initialization that can't happen until a save is loaded.
    private static void inGameInit(){
        for(Task task : GameManager.getTaskManager().getTasks()){
            task.unlock();
        }
        //testing:
        //GameManager.getShops().unlockAll();
        inGameInitialized = true;
    }

    /// Gets the current version of the AP mod
    /// @return the mod version
    public static String getModVersion(){
        return modVersion;
    }

    /// Used by the "Archipelago new world" button to initialize the APClient. Probably should
    /// be renamed.
    /// @param h the room host (eg, "archipelago.gg:62414")
    /// @param s the slot name
    /// @param p the room password
    /// @return true (was going to change to a success indicator, but the way the Client handles threads is unclear to me)
    public static boolean tryConnectAndGen(String h, String s, String p){
        host = h;
        slot = s;
        password = p;
        APAgent.log("Trying to generate a world: host: "+host+", slot: "+slot+", password: "+password);
        boolean newManagers = itemManager == null || locationManager == null;
        if(newManagers) {
            itemManager = new ItemManager(0);
            locationManager = new LocationManager();
        }

        client = APClient.tryConnect(host, slot, password, itemManager, locationManager);
        return true;
    }

    /// Called by the {@link onTaskComplete} to try and send the relevant check.
    ///
    /// @param task the completed task
    ///
    public static void trySendTaskCheck(Task task){
        int locId = LocationManager.TaskNameToLocID.get(task.name);
        APAgent.log("Trying to send loc check for task " + task.name + " (id " + locId + ").");
        sendCheck(locId);
    }

    /// Syncs the internal checked location list with the server's checked location list
    /// @param checkedLocs the list of checked locations provided by the server
    public static void setCheckedLocs(Set<Long> checkedLocs){
        locationManager.setCheckedLocs(checkedLocs);
    }

    /// Trys to send a location check
    ///
    /// @param id the location id to check
    /// @param force try to send the check even if in the local list of checked locations
    /// @return true if the check is successfully sent
    private static boolean sendCheck(int id, boolean force){
        if (!APClient.Connected()){
            APAgent.log("Can't send check while disconnected; queueing (id " + id + ")");
            locationManager.queueLocation(id);
            return false;
        } else if (locationManager.locAlreadyChecked(id) && !force){
            return true;
        } else if (client.checkLocation(id).getCode() == APResult.ResultCode.SUCCESS){
            APAgent.log("Successfully sent check! (id " + id + ")");
            locationManager.markChecked(id);
            return true;
        } else {
            APAgent.log("Failed to send location check; queueing (id " + id + ")");
            locationManager.queueLocation(id);
            return false;
        }
    }

    /// Trys to send a location check
    /// @param id the location id to check
    /// @return true if the check is successfully sent
    /// @see sendCheck(int id, boolean force)
    private static boolean sendCheck(int id){
        return sendCheck(id, false);
    }

    /// Tries to send all the queued-but-unsent locations
    /// @param force try to send the checks even if in the local list of checked locations
    /// @return true if the queue is successfully emptied (or already empty), false if any check fails to send
    public static boolean tryFlushLocationQueue(boolean force){
        if(!APClient.Connected()){
            APAgent.log("Can't flush the location queue while disconnected!");
            return false;
        }
        while (locationManager.nonemptyQueue()){
            int id = locationManager.popOldestLocation();
            if(!sendCheck(id, force)){
                return false;
            }
            locationManager.markChecked(id);
        }
        APAgent.log("Successfully flushed the location queue.");
        return true;
    }

    // ---------------------------------------------------
    // Methods called by the mod/client to impact the game
    // ---------------------------------------------------

    public static void unlockShopItem(int id){
        GameManager.getShops().unlockItem(id);
    }

    /// Sends the player an in-game notification
    /// @param title the notification title text
    /// @param msg the notification body text
    public static void sendNotification(String title, String msg){
        EquilinoxGuis.notify(title, msg, GuiRepository.INFO, null);
    }

}
