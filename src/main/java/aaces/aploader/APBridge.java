package aaces.aploader;

import aaces.aploader.client.APClient;
import aaces.aploader.client.ItemManager;
import aaces.aploader.client.LocationManager;
import audio.SoundMaestro;
import basics.DisplayManager;
import blueprints.Blueprint;
import breedingTraits.FloatTrait;
import breedingTraits.Trait;
import breedingTraits.VectorTrait;
import componentArchitecture.Component;
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
import guiRendering.GuiRenderData;
import guis.GuiComponent;
import guis.GuiTexture;
import health.LifeCompBlueprint;
import instances.Entity;
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
import taskUi.TaskInfoFrameUi;
import tasks.CashReward;
import tasks.Reward;
import tasks.Task;
import tasks.TaskState;
import unlockGuide.EvolveChainUi;
import userInterfaces.ClickListener;
import userInterfaces.GuiImage;
import userInterfaces.TitlePanelUi;
import utils.BinaryReader;
import utils.BinaryWriter;
import world.WorldConfigs;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("unchecked")
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


    /// Called every game tick. The way the mod detects entering the game probably to be changed.
    public static void gameManagerUpdate() {
        if(GameManager.getGameState() != GameState.GAME_MENU && GameManager.getGameState() != GameState.SPLASH_SCREEN){
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

            boolean distribute_cash_rewards = APClient.Connected() ? client.slotData.tasksRewardDP() : true;

            if(distribute_cash_rewards) {
                for (Reward reward : task.getRewards()) {
                    if (reward instanceof CashReward) {
                        reward.payOut();
                    }
                }
            }

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
    public static void rewardsPanelIntercept(TaskInfoFrameUi taskInfoFrameUi) {
        boolean showCashRewards = APClient.Connected() ? client.slotData.tasksRewardDP() : true;

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
                Text apRewardText = Text.newText("- Unlocks an Archipelago Item").setFontSize(UiSettings.NORM_FONT).indent().create();
                apRewardText.setColour(ColourPalette.WHITE);
                addText(apRewardText, 0.0F, 0.0F, 1.0F);

                if (!showCashRewards) return;

                try {
                    Field taskField = TaskInfoFrameUi.class.getDeclaredField("task");
                    taskField.setAccessible(true);
                    Task task = (Task) taskField.get(taskInfoFrameUi);

                    float textGap = 8.0F / (super.getScale().y * (float) DisplayManager.getUiHeight());
                    float yPos = textGap + apRewardText.getHeight() / super.getScale().y;

                    for(Reward reward : task.getRewards()){
                        if(reward instanceof CashReward){
                            Text text = Text.newText("- " + reward.getInfo()).setFontSize(UiSettings.NORM_FONT).indent().create();
                            text.setColour(ColourPalette.WHITE);
                            super.addText(text, 0.0F, yPos, 1.0F);
                        }
                    }

                } catch (Exception e) {
                    APAgent.log("\t" + e.getMessage());
                    e.printStackTrace();
                }

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
        extraInfoGui.display(GameText.getText(1120), new ArrayList<>(), new ArrayList<>(), c);
    }


    /// Called when the game tries to save. We add the room/slot info, the
    /// received item index, and the queued-but-not-yet-sent location checks
    /// to the end of the save file.
    public static void saveAPState(BinaryWriter writer){
        APAgent.log("Trying to save the AP state of the game");
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
        APAgent.log("Trying to load saved ap info");
        try {
            host = reader.readString();
            slot = reader.readString();
            password = reader.readString();

            itemManager = new ItemManager(reader.readInt());

            int numQueuedLocs = reader.readInt();
            locationManager = new LocationManager();
            for(int i = 0; i < numQueuedLocs; i++){
                locationManager.queueLocation(reader.readInt());
            }

            APAgent.log("Trying to connect: host: "+host+", slot: "+slot+", password: "+password + ", item index: "+itemManager.getIndex());
            client = APClient.tryConnect(host, slot, password, itemManager, locationManager);
        } catch (Exception e) {
            APAgent.log("couldn't load the saved state (this can happen if trying to load a world created without this mod installed)");
            APAgent.log("\t"+e.getMessage());
            e.printStackTrace();

            itemManager = new ItemManager(0);
            locationManager = new LocationManager();
            sendNotification("Couldn't Load Saved AP Info!", "Try connecting directly from the main menu, or create a new world.");
        }
        saveLoaded = true;
    }


    /// Called when the SessionManager tries to save the currently loaded Session. Stops it from saving
    /// the dummy save created by {@link gameManagerInit} on startup. (Note, this doesn't apply to the autosave)
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


    /// Called when the game tries to autosave.
    /// Stops the game from autosaving the dummy session created by {@link gameManagerInit} on startup.
    ///
    /// @param session The session trying to autosave
    public static void safeUpdateAutoSave(Session session){
        //APAgent.log("safe autosave wrapper called");
        if (!saveLoaded) return;
        try {
            Method updateAutoSave = Session.class.getDeclaredMethod("updateAutoSave");
            updateAutoSave.setAccessible(true);
            updateAutoSave.invoke(session);
        } catch (Exception e){
            APAgent.log("couldn't load the saved state (this can happen if trying to load a world created without this mod installed)");
            APAgent.log("\t"+e.getMessage());
            e.printStackTrace();
        }
    }




    // -------------------------------------------------
    // Internal/helper methods for this class or the mod
    // -------------------------------------------------



    /// Handles initialization that can't happen until a save is loaded.
    private static void inGameInit(){

        // Usually, some tasks are locked behind the completion of other tasks. Since
        // tasks can be completed out of order, we need to just unlock all of them right
        // at the start.
        for(Task task : GameManager.getTaskManager().getTasks()){
            task.unlock();
        }
        //testing:
        //GameManager.getShops().unlockAll();

        EventManager.EVOLUTION.addListener(APBridge::onEvolution);
        EventManager.POPULATION_ADD.addListener(APBridge::onPopulationAdd);

        inGameInitialized = true;
        APAgent.log("In-game initialized");
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
        itemManager = new ItemManager(0);
        locationManager = new LocationManager();
        client = APClient.tryConnect(host, slot, password, itemManager, locationManager);
        return true;
    }


    /// Called by {@link onTaskComplete} to try and send the relevant check.
    ///
    /// @param task the completed task
    public static void trySendTaskCheck(Task task){
        int locId = LocationManager.TaskNameToLocID.get(task.name);
        APAgent.log("Trying to send loc check for task " + task.name + " (id " + locId + ").");
        sendCheck(locId);
    }


    /// Called by  to try and send the relevant check
    ///
    /// @param species the species evolved
    public static void trySendEvolutionCheck(String species){
        int locId = LocationManager.SpeciesEvolutionToLocID.get(species);
        APAgent.log("Trying to send loc check for evolving " + species + " (id " + locId + ").");
        sendCheck(locId);
    }


    /// Syncs the internal checked location list with the server's checked location list
    /// @param checkedLocs the list of checked locations provided by the server
    public static void setCheckedLocs(Set<Long> checkedLocs){
        locationManager.setCheckedLocs(checkedLocs);
    }


    /// Tries to send a location check
    ///
    /// @param id the location id to check
    /// @param force try to send the check even if in the local list of checked locations
    /// @return true if the check is successfully sent
    private static boolean sendCheck(int id, boolean force){
        if (!saveLoaded) return false;
        if (!APClient.Connected()){
            APAgent.log("Can't send check while disconnected; queueing (id " + id + ")");
            locationManager.queueLocation(id);
            return false;
        } else if (locationManager.locAlreadyChecked(id) && !force){
            APAgent.log("Previously sent check " + id);
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


    /// Tries to send a location check
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


    /// Listens for the Evolution event to send the relevant check
    /// @param data the EventData (contains a `Blueprint` object)
    private static void onEvolution(EventData data){
        Blueprint speciesEvolved = (Blueprint) data.object;
        String speciesName = speciesEvolved.getName();
        APAgent.log("Evolution occurred! Name: " + speciesName);
        trySendEvolutionCheck(speciesName);
    }

    /// Listens for the Population Add event to send the relevant check
    /// @param data always null, but needed for the event
    private static void onPopulationAdd(EventData data){
        Map<Integer, Entity> entitiesMap = GameManager.getWorld().getEntityGrid().getWorldEntities();
        Entity newEntity = entitiesMap.get(Collections.max(entitiesMap.keySet()));
        APAgent.log("Population add event: ");
        APAgent.log("Species: " + newEntity.getBlueprint().getName());
        APAgent.log("New species?: " + newEntity.isNewSpecies());
        APAgent.log("Id: "+newEntity.getId());
        for(Trait trait : newEntity.getTraits()){
            float value = -100f;
            if (trait instanceof FloatTrait) {
                value = ((FloatTrait) trait).value;
            }
            APAgent.log("Trait: " + trait.blueprint.getName() + ": " + value);
            if(trait.blueprint.getName().equals("Size")){
                processNewSpeciesSize(newEntity.getBlueprint().getName(), value * 10);
            }
        }
    }

    /// Called whenever a new entity spawns to check its size.
    /// @param species The species of the entity that spawned
    /// @param size The size of the entity that spawned
    private static void processNewSpeciesSize(String species, float size){
        if(!saveLoaded) return;
        if (size >= 1.095){
            int locId = LocationManager.SpeciesAndSizeToSizeLocID.get(species + "1.10");
            if(!locationManager.locAlreadyChecked(locId)){
                APAgent.log("Trying to send 'Size 1.10' check for " + species);
                if (sendCheck(locId)){
                    sendNotification("Size check sent!", "Size 1.10 check for " + species + " sent.");
                }
            }
        } else if (size <= 0.905){
            int locId = LocationManager.SpeciesAndSizeToSizeLocID.get(species + "0.90");
            if(!locationManager.locAlreadyChecked(locId)){
                APAgent.log("Trying to send 'Size 0.90' check for " + species);
                if (sendCheck(locId)){
                    sendNotification("Size check sent!", "Size 0.90 check for " + species + " sent.");
                }
            }
        }
    }


    // ---------------------------------------------------
    // Methods called by the mod/client to impact the game
    // ---------------------------------------------------



    /// Unlocks a species in-game
    /// @param id the id of the species to unlock
    public static void unlockShopItem(int id){
        GameManager.getShops().unlockItem(id);
    }


    /// Sends the player an in-game notification
    /// @param title the notification title text
    /// @param msg the notification body text
    public static void sendNotification(String title, String msg){
        EquilinoxGuis.notify(title, msg, GuiRepository.INFO, null);
    }


    /// Increases the player's dp amount
    /// @param amt the amount of dp to give
    public static void giveMoney(int amt){
        GameManager.getSession().getStats().increaseDp(amt);
    }

}
