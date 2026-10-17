package aaces.aploader;

import aaces.aploader.client.APClient;
import aaces.aploader.client.GoalType;
import aaces.aploader.client.ItemManager;
import aaces.aploader.client.LocationManager;
import audio.SoundMaestro;
import basics.DisplayManager;
import blueprints.Blueprint;
import breedingTraits.FloatTrait;
import breedingTraits.Trait;
import breedingTraits.TraitBlueprint;
import classification.Classifier;
import componentArchitecture.Component;
import componentArchitecture.ComponentBlueprint;
import componentArchitecture.ComponentType;
import components.InformationComponent;
import events.EventData;
import events.EventManager;
import extraInfoGui.ExtraInfoContent;
import extraInfoGui.ExtraInfoGui;
import extraInfoGui.ExtraToolbarGui;
import fontRendering.Text;
import food.FoodComponent;
import food.FoodSection;
import food.FoodSectionType;
import food.FoodToShare;
import gameManaging.GameManager;
import gameManaging.GameState;
import gameMenu.DnaButtonGui;
import gameMenu.GameMenuBackground;
import gameMenu.GameMenuGui;
import gameMenu.MenuPanelGui;
import guiRendering.GuiRenderData;
import guis.GuiComponent;
import guis.GuiMaster;
import guis.GuiTexture;
import health.LifeCompBlueprint;
import instances.Entity;
import io.github.archipelagomw.APResult;
import io.github.archipelagomw.ClientStatus;
import languages.GameText;
import main.MainApp;
import mainGuis.*;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import resourceManagement.BlueprintRepository;
import saves.SaveSlot;
import saves.Saves;
import session.GameMode;
import session.Session;
import session.SessionManager;
import shopping.BlueprintShopItem;
import shops.ShopItem;
import taskUi.TaskInfoFrameUi;
import tasks.*;
import textures.Texture;
import toolTips.ToolTipInfo;
import toolbar.SlideInPanel;
import toolbar.SlideInPanelSync;
import toolbar.Toolbar;
import unlockGuide.EvolveChainUi;
import userInterfaces.*;
import utils.BinaryReader;
import utils.BinaryWriter;
import utils.MyFile;
import world.WorldConfigs;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings("unchecked")
public class APBridge {

    private static final String gameVersion = "1.7.2";
    private static final String modVersion = "0.1.1";

    public static String host = "";
    public static String slot = "";
    public static String password = "";

    public static boolean goaled = false;
    public static boolean serverKnowsWeGoaled = false;

    private static ItemManager itemManager = null;
    private static LocationManager locationManager = null;
    private static APClient client;

    private static boolean inGameInitialized = false;
    public static boolean saveLoaded = false;

    public static Texture AP_ICON;
    public static Texture AP_COLOR;




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
        AP_ICON = initCustomTexture("archipelago-white-small.png");
        AP_COLOR = initCustomTexture("archipelago-color-icon.png");
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


    /// Called every game tick. The way the mod detects entering the game may need to be changed.
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
            APAgent.log("Error adding AP button to the main menu!");
            APAgent.log("\t"+e.getMessage());
            e.printStackTrace();
        }
    }


    /// Adds the mod version to the game version on the main menu.
    ///
    /// @param gameMenuGui the GUI we need to modify
    public static void addVersionText(GameMenuGui gameMenuGui){
        Text text = Text.newText(MainApp.VERSION_STRING + " -- EqAP Version " + modVersion).setFontSize(UiSettings.LARGE_FONT).create();
        text.setColour(ColourPalette.MIDDLE_GREY);
        gameMenuGui.addText(text, 0.14F, 0.94F, 1.0F);
    }


    /// Called when GameManager.cleanup() runs.
    public static void onApplicationQuit(){
        APAgent.log("Cleaning up");
        APClient.tryDisconnect();
    }


    /// Replaces the internal task completion handling. Used to prevent
    /// the game from distributing the normal rewards, and calls {@link trySendTaskCheck}
    /// to send the proper archipelago check.
    ///
    /// @param task the task completed
    public static void onTaskComplete(Task task){
        APAgent.log("task completed: " + task.name);

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
                boolean showCashRewards = APClient.Connected() ? client.slotData.tasksRewardDP() : true;
                boolean completionist_goal = APClient.Connected() ? client.slotData.getGoalCondition() == GoalType.COMPLETIONIST : false;
                try {
                    Field taskField = TaskInfoFrameUi.class.getDeclaredField("task");
                    taskField.setAccessible(true);
                    Task task = (Task) taskField.get(taskInfoFrameUi);

                    Text apRewardText;
                    if (completionist_goal && task.name.equals("Completionist")) {
                        apRewardText = Text.newText("- Goal Condition").setFontSize(UiSettings.NORM_FONT).indent().create();
                    } else {
                        apRewardText = Text.newText("- Unlocks an Archipelago Item").setFontSize(UiSettings.NORM_FONT).indent().create();
                    }
                    apRewardText.setColour(ColourPalette.WHITE);
                    addText(apRewardText, 0.0F, 0.0F, 1.0F);

                    if (!showCashRewards) return;

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
                    APAgent.log("Error replacing task rewards text");
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
            writer.writeLong(modVersionToLong());

            writer.writeString(host);
            writer.writeString(slot);
            writer.writeString(password);

            if(itemManager != null) {
                writer.writeInt(itemManager.getIndex());
            } else {
                writer.writeInt(0);
            }

            if(locationManager != null) {
                writer.writeInt(locationManager.numQueuedLocations());
                while (locationManager.nonemptyQueue()) {
                    writer.writeInt(locationManager.popOldestLocation());
                }
            } else {
                writer.writeInt(0);
            }

            writer.writeBoolean(goaled);
            if (goaled) writer.writeBoolean(serverKnowsWeGoaled);

        } catch (IOException e) {
            APAgent.log("Couldn't save AP info!");
            APAgent.log("\t"+e.getMessage());
            e.printStackTrace();
        }
    }

    private static long modVersionToLong(){
        String[] exps = modVersion.split("\\.");
        return (long) (Math.pow(5, Integer.parseInt(exps[0])) *
                Math.pow(3, Integer.parseInt(exps[1])) *
                Math.pow(2, Integer.parseInt(exps[2])));
    }

    public static int[] getPrimeFactorCounts(long number, List<Long> knownPrimes) {
        int[] counts = new int[knownPrimes.size()];
        long remainder = number;

        int index = 0;
        for (long prime : knownPrimes) {
            int count = 0;

            while (remainder > 1 && remainder % prime == 0) {
                count++;
                remainder /= prime;
            }

            counts[index] = count;
            index++;
        }

        return counts;
    }

    /// Called when the game tries to load a save file.
    /// Tries to load the previously saved AP things from a save file. If the save
    /// file does not contain the expected data, connecting to a room with this save file
    /// will result in every item being received again.
    public static void loadAPState(BinaryReader reader){
        APAgent.log("Trying to load saved ap info");
        try {
            long modVersionInSave = reader.readLong();
            int[] versionNumbers = getPrimeFactorCounts(modVersionInSave, Arrays.asList(5L, 3L, 2L));
            String loadedModVersion = versionNumbers[0] + "." + versionNumbers[1] + "." + versionNumbers[2];
            boolean lastSavedOnLatestVersion = modVersionInSave == modVersionToLong();

            host = reader.readString();
            slot = reader.readString();
            password = reader.readString();

            itemManager = new ItemManager(reader.readInt());

            int numQueuedLocs = reader.readInt();
            locationManager = new LocationManager();
            for(int i = 0; i < numQueuedLocs; i++){
                locationManager.queueLocation(reader.readInt());
            }

            goaled = reader.readBoolean();
            if (goaled) serverKnowsWeGoaled = reader.readBoolean();

            APAgent.log("Trying to connect: host: "+host+", slot: "+slot+", password: "+password + ", item index: "+itemManager.getIndex());
            client = APClient.tryConnect(host, slot, password, itemManager, locationManager, true);
        } catch (Exception e) {
            APAgent.log("couldn't load the saved state (this can happen if trying to load a world created without this mod installed)");
            APAgent.log("\t"+e.getMessage());
            e.printStackTrace();

            itemManager = new ItemManager(0);
            locationManager = new LocationManager();
            goaled = false;
            serverKnowsWeGoaled = false;
            sendNotification("Couldn't Load Saved AP Info!", "Try connecting from the AP menu in the toolbar, or create a new world.");
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
        if (!saveLoaded) return;
        try {
            Method updateAutoSave = Session.class.getDeclaredMethod("updateAutoSave");
            updateAutoSave.setAccessible(true);
            updateAutoSave.invoke(session);
        } catch (Exception e){
            APAgent.log("Couldn't autosave!");
            APAgent.log("\t"+e.getMessage());
            e.printStackTrace();
        }
    }


    /// Called by the Evolution UI to test if we have enough dp to evolve. If "Free Evolution" is on,
    /// we make the UI think we have enough dp.
    public static int getDpCountForEvolution(){
        boolean freeEvolution = APClient.Connected() ? client.slotData.freeEvolution() : true;
        if(freeEvolution){
            return 999999999;
        } else {
            return GameManager.getSession().getStats().getDpCount();
        }
    }


    /// Called to determine how much dp to deduct on evolution, and what to display as the cost of evolution
    /// @param info The information component containing the original price
    /// @return `0` if "Free Evolution" is enabled, `info.getPrice()` otherwise
    public static int getEvolutionPrice(InformationComponent.InformationCompBlueprint info){
        boolean freeEvolution = APClient.Connected() ? client.slotData.freeEvolution() : true;
        if(freeEvolution){
            return 0;
        } else {
            return info.getPrice();
        }
    }


    /// Equilinox has lots of nested and abstract classes, so this can be used to try and find them
    public static void debug(){
        APAgent.log("debug() call placed inside method " + Thread.currentThread().getStackTrace()[2]);
        APAgent.log("which in turn was called by " + Thread.currentThread().getStackTrace()[3]);
    }


    public static void addAPToolbarButton(Toolbar toolbar){
        try {
            Method addButton = Toolbar.class.getDeclaredMethod("addButton", Texture.class, float.class, ClickListener.class);
            addButton.setAccessible(true);

            TabButtonUi apButton;

            ClickListener cl = event -> {
                try {
                    Method checkCloseSlidePanel = Toolbar.class.getDeclaredMethod("checkCloseSlidePanel");
                    checkCloseSlidePanel.setAccessible(true);

                    Field panelSyncField = Toolbar.class.getDeclaredField("panelSync");
                    panelSyncField.setAccessible(true);
                    SlideInPanelSync panelSync = (SlideInPanelSync) panelSyncField.get(toolbar);

                    Field largePanelField = Toolbar.class.getDeclaredField("largePanel");
                    largePanelField.setAccessible(true);
                    SlideInPanel largePanel = (SlideInPanel) largePanelField.get(toolbar);

                    if(event.isToggleOn()) {
                        EquilinoxGuis.getExtraInfoGui().close();
                        panelSync.display(largePanel, new ArchipelagoToolbarGui(host, slot, password, itemManager, locationManager));
                    } else if (event.isToggleOff() && (boolean) checkCloseSlidePanel.invoke(toolbar)){
                        EquilinoxGuis.getExtraInfoGui().close();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };

            apButton = (TabButtonUi) addButton.invoke(toolbar, AP_ICON, 0.94616F, cl);
            apButton.setToolTip(ToolTipInfo.newInfo("Archipelago Settings", "Manage/view the archipelago connection."));
        } catch (Exception e) {
            APAgent.log("Error adding AP Toolbar button");
            APAgent.log(e.getMessage());
        }


    }


    /// Called when trying to load an image. If it is one of ours, we handle it accordingly
    /// (ie, get the asset from the external "res" folder). If it is a game asset, just
    /// pass it through
    public static InputStream getFileInputStream(MyFile file) {
        //APAgent.log("Loading texture: " + file.getName());
        if(file.getPath().contains("archipelago")){

            APAgent.log("Loading AP texture: " + file.getPath());

            Path path;

            if(file.getPath().contains("/res/guis/")){
                path = Paths.get(file.getPath().replace("/res/guis/", "res/"));
            } else {
                path = Paths.get(file.getPath().substring(1)); // remove the leading /
            }

            try {
                if (Files.exists(path)){
                    APAgent.log("Found the file, returning the InputStream");
                    return Files.newInputStream(path);
                } else {
                    APAgent.log("File not found, defaulting");
                    MyFile def = new MyFile(GuiMaster.GUIS_LOC, "colourWheel.png");
                    return def.getInputStream();
                }
            } catch (Exception e) {
                APAgent.log("Null texture file, defaulting");
                MyFile def = new MyFile(GuiMaster.GUIS_LOC, "colourWheel.png");
                return def.getInputStream();
            }

        } else {
            //APAgent.log("    Game texture");
            return file.getInputStream();
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
            if (task.getState() == TaskState.COMPLETE) continue;
            task.unlock();
        }

        //GameManager.getShops().unlockAll(); // Just for testing

        EventManager.EVOLUTION.addListener(APBridge::onEvolution);
        EventManager.POPULATION_ADD.addListener(APBridge::onPopulationAdd);

        //componentAnalysis(); // Debug

        inGameInitialized = true;
        APAgent.log("In-game initialized");
    }

    /// This was used to create a script to view the contents of an Equilinox save file, it is not used by the mod.
    private static void componentAnalysis(){
        try {
            Field tasksField = TaskManager.class.getDeclaredField("tasks");
            tasksField.setAccessible(true);
            Map<Integer, Task> tasks = (Map<Integer, Task>) tasksField.get(GameManager.getTaskManager());
            for (int i : tasks.keySet()){
                Task task = tasks.get(i);
                StringBuilder req_string = new StringBuilder();
                for (TaskRequirement requirement : task.getRequirements(false)){
                    switch(requirement.getClass().toString()) {
                        case "class tasks.HappyEntitiesReq":
                            req_string.append("i");
                            break;
                        case "class tasks.ColourEntityReq":
                        case "class tasks.EntityCountReq":
                            req_string.append("i");
                            break;
                        case "class tasks.EvolutionReq":
                            req_string.append("i");
                            break;
                        case "class tasks.EatReq":
                            req_string.append("i");
                            break;
                        case "class tasks.BreedReq":
                            req_string.append("i");
                            break;
                        case "class tasks.TaskCompletorRequirement":
                            req_string.append("i");
                            break;
                        case "class tasks.BuildReq":
                            req_string.append("i");
                            break;
                        case "class tasks.BaaReq":
                            req_string.append("i");
                            break;
                        case "class tasks.ButterflyCatchReq":
                            req_string.append("i");
                            break;
                        case "class tasks.HoneyReq":
                            req_string.append("i");
                            break;
                        case "class tasks.TreeCutReq":
                            req_string.append("i");
                            break;
                        case "class tasks.HoneyHarvesterReq":
                            req_string.append("i");
                            break;
                        case "class tasks.HuntingReq":
                            req_string.append("i");
                            break;
                        case "class tasks.SpittingRequirement":
                            req_string.append("i");
                            break;
                        case "class tasks.BloomingReq":
                            req_string.append("i");
                            break;
                        case "class tasks.FlyCatchReq":
                            req_string.append("i");
                            break;
                        case "class tasks.EagleCatchReq":
                            req_string.append("i");
                            break;
                        case "class tasks.HoleDigReq":
                            req_string.append("i");
                            break;
                        default:
                            break;
                    }
                }
                System.out.println(i + ": \"" + req_string + "\",");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        try {
            for (Blueprint blueprint : BlueprintRepository.getAllBlueprints()) {
                int id = blueprint.getId();
                System.out.println(id + ": \"" + blueprint.getName() + "\",");
                if(true) continue;
                //if (id != 20) continue;
                Set<ComponentType> componentTypes = blueprint.getComponentTypes();
                Set<ComponentBlueprint> componentBlueprints = new HashSet<>();
                for (ComponentType type : componentTypes) {
                    componentBlueprints.add(blueprint.getComponent(type));
                }
                int numComponents = componentBlueprints.size();

                Field componentsField = Blueprint.class.getDeclaredField("components");
                componentsField.setAccessible(true);
                Map<ComponentType, ComponentBlueprint> components = (Map<ComponentType, ComponentBlueprint>) componentsField.get(blueprint);

                //System.out.println("Blueprint id: " + id);
                //System.out.println("\tNum Components: " + numComponents);
                StringBuilder loadString = new StringBuilder();

                for (ComponentBlueprint compBlueprint : components.values()) {

                    Component component = compBlueprint.createInstance();

                    // comp trait load
                    List<TraitBlueprint> traitBlueprints = compBlueprint.getTraits();
                    //System.out.println("\t\tComponent: " + compBlueprint.getComponentType().toString());
                    if (traitBlueprints != null) {
                        for (TraitBlueprint traitBlueprint : traitBlueprints) {
                            //System.out.println("\t\t\t"+traitBlueprint.getName());
                            switch (traitBlueprint.getName()) {
                                case "Disease Resist":
                                case "Sting Power":
                                case "Speed":
                                case "Bounce Power":
                                case "Edibility":
                                case "Productivity":
                                case "Yield":
                                case "Attack Power":
                                case "Honey Production":
                                case "Anger":
                                case "Size":
                                    loadString.append("ffi");
                                    break;
                                case "Colour":
                                    loadString.append("c");
                                    break;
                            }
                        }
                    }
                    // then component load
                    boolean isAnimal = blueprint.getSpeciesClassification().isTypeOf(Classifier.getAnimalClassification());
                    switch (component.getClass().toString()) {
                        case "class growth.DynamicGrowthComponent":
                            loadString.append("b!{iff}");
                            break;
                        case "class toolbox.Transformation":
                            loadString.append("vfff");
                            break;
                        case "class sleeping.SleepComponent":
                            loadString.append("b");
                            break;
                        case "class health.LifeComponent":
                            loadString.append("ffffsf");
                            if (isAnimal) {
                                loadString.append("bf");
                            }
                            loadString.append("bfib?{i}");
                            break;
                        case "class components.NameComponent":
                            loadString.append("Sb");
                            break;
                        case "class eating.EatingComponent":
                            loadString.append("fi:{S}");
                            break;
                        case "class components.InformationComponent":
                            loadString.append("vi");
                            break;
                        case "class components.MeshComponent":
                            loadString.append("i");
                            break;
                        case "class growth.StaticGrowthComponent":
                            loadString.append("b!{iff}");
                            break;
                        case "class fruit.FruiterComponent":
                            loadString.append("i");
                            break;
                        case "class food.FoodComponent":
                            FoodComponent foodComponent = (FoodComponent) component;
                            try {
                                Field sectionsField = FoodComponent.class.getDeclaredField("sections");
                                sectionsField.setAccessible(true);
                                Map<FoodSectionType, FoodSection> sections = (Map<FoodSectionType, FoodSection>) sectionsField.get(foodComponent);
                                for (FoodSection section : sections.values()) {
                                    if (section instanceof FoodToShare) {
                                        loadString.append("i");
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case "class perching.PerchComponent":
                            loadString.append("b?{i:{ii}}");
                            break;
                        case "class shellHide.ShellHideComponent":
                            loadString.append("b");
                            break;
                        case "class fruit.DecayComponent":
                            loadString.append("f");
                            break;
                        case "class fighting.HostileComponent":
                            loadString.append("f");
                            break;
                        case "class hiveComponents.HiveComponent":
                            loadString.append("i");
                            break;
                        case "class building.BuildComponent":
                            loadString.append("i");
                            break;
                        case "class beavers.BeaverComponent":
                            loadString.append("b");
                            break;
                        case "class building.DecomposeComponent":
                            loadString.append("b");
                            break;
                        case "class equipping.ItemComponent":
                            loadString.append("f");
                            break;
                        case "class nightBloom.BloomComponent":
                            loadString.append("if");
                            break;
                        default:
                            break;
                    }

                }
                String lString = loadString.toString();

                System.out.println(id + ": \"" + lString + "\",");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /// Gets the current version of the AP mod
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
        client = APClient.tryConnect(host, slot, password, itemManager, locationManager, true);
        return true;
    }

    /// Called by the toolbar AP menu to connect
    /// @param h host
    /// @param s slot
    /// @param p password
    /// @param im ItemManager
    /// @param lm LocationManager
    public static void newConnection(String h, String s, String p, ItemManager im, LocationManager lm){
        host = h;
        slot = s;
        password = p;
        itemManager = im;
        locationManager = lm;
        APAgent.log("Trying to re/connect in an existing world: host: "+host+", slot: "+slot+", password: "+password);
        client = APClient.tryConnect(host, slot, password, itemManager, locationManager, false);
    }


    /// Called by {@link onTaskComplete} to try and send the relevant check.
    public static void trySendTaskCheck(Task task){
        int locId = LocationManager.TaskNameToLocID.get(task.name);
        APAgent.log("Trying to send loc check for task " + task.name + " (id " + locId + ").");
        sendCheck(locId);
    }


    /// Called by  to try and send the relevant check
    public static void trySendEvolutionCheck(String species){
        int locId = LocationManager.SpeciesEvolutionToLocID.get(species);
        APAgent.log("Trying to send loc check for evolving " + species + " (id " + locId + ").");
        sendCheck(locId);
    }


    /// Syncs the internal checked location list with the server's checked location list
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
        return true;
    }


    /// Listens for the Evolution event to send the relevant check
    /// @param data the EventData (contains a `Blueprint` object)
    private static void onEvolution(EventData data){
        Blueprint speciesEvolved = (Blueprint) data.object;
        String speciesName = speciesEvolved.getName();
        APAgent.log("Evolution occurred! Name: " + speciesName);

        if(speciesName.equals("Camel")){
            if (client != null) {
                if (client.slotData.getGoalCondition() == GoalType.CAMEL){
                    if(tryGoal()){
                        sendNotification("Goal Complete!", "You evolved a camel and therefore completed the goal!");
                    } else {
                        sendNotification("Goal Completed, but not sent!", "You completed your goal, but we failed to tell the server. Save and exit, then reload and reconnect to try again.");
                    }
                } else {
                    trySendEvolutionCheck(speciesName);
                }
            }
        } else {
            trySendEvolutionCheck(speciesName);
        }
    }


    /// Listens for the Population Add event to send the relevant check
    /// @param data always null
    private static void onPopulationAdd(EventData data){
        Map<Integer, Entity> entitiesMap = GameManager.getWorld().getEntityGrid().getWorldEntities();
        Entity newEntity = entitiesMap.get(Collections.max(entitiesMap.keySet()));

        for(Trait trait : newEntity.getTraits()){
            float value = -100f;
            if (trait instanceof FloatTrait) {
                value = ((FloatTrait) trait).value;
            }
            if(trait.blueprint.getName().equals("Size")){
                processNewSpeciesSize(newEntity.getBlueprint().getName(), value * 10);
            }
        }
    }


    /// Called whenever a new entity spawns to check its size
    private static void processNewSpeciesSize(String species, float size){
        if(!saveLoaded) return;
        if(!APClient.Connected()) return;
        if(!client.slotData.includeSizeChecks()) return;
        if(client.slotData.getGoalCondition() == GoalType.CAMEL && species.equals("Camel")) return;

        if (size >= 1.195){
            int locId = LocationManager.SpeciesAndSizeToSizeLocID.get(species + "1.20");
            if(!locationManager.locAlreadyChecked(locId)){
                APAgent.log("Trying to send 'Size 1.20' check for " + species);
                if (sendCheck(locId)){
                    sendNotification("Size check sent!", "Size 1.20 check for " + species + " sent.");
                }
            }
        }
        if (size >= 1.095){
            int locId = LocationManager.SpeciesAndSizeToSizeLocID.get(species + "1.10");
            if(!locationManager.locAlreadyChecked(locId)){
                APAgent.log("Trying to send 'Size 1.10' check for " + species);
                if (sendCheck(locId)){
                    sendNotification("Size check sent!", "Size 1.10 check for " + species + " sent.");
                }
            }
        }
    }


    /// Called on a successful connection to an Archipelago server to properly resync
    public static void onConnect(){
        for(Task task : GameManager.getTaskManager().getTasks()){
            if (task.getState() == TaskState.COMPLETE) continue;
            task.unlock();
        }
        lockShops();
        setCheckedLocs(client.getLocationManager().getCheckedLocations());
        if (!tryFlushLocationQueue(false)){
            APAgent.log("Failed to flush the location queue on connection");
        }
        locationManager.unlockAlreadyEvolvedSpecies();

        APAgent.log("Goal condition: " + client.slotData.getGoalCondition());

        if (goaled && !serverKnowsWeGoaled){
            APAgent.log("Previously goaled, but did not send. Sending now...");
            tryGoal();
        }

    }

    /// Called by BinaryReaderTransformer to debug save file loading
    public static void printRead(int i){
        APAgent.log("Read an int. Value: " + i);
    }
    public static void printRead(float i){
        APAgent.log("Read a float. Value: " + i);
    }
    public static void printRead(String i){
        APAgent.log("Read a string. Value: " + i);
    }
    public static void printRead(boolean i){
        APAgent.log("Read a bool. Value: " + i);
    }
    public static void printRead(long i){
        APAgent.log("Read a long. Value: " + i);
    }
    public static void printRead(short i){
        APAgent.log("Read a short. Value: " + i);
    }
    public static void printRead(Vector3f i){
        APAgent.log("Read a vector. Value: " + i);
    }


    private static Texture initCustomTexture(String name) {
        return Texture.newTexture(new MyFile("res/" + name)).noFiltering().clampEdges().create();
    }

    private static boolean tryGoal(){
        if(serverKnowsWeGoaled) return true;
        goaled = true;
        if (client == null) return false;
        if (!client.isConnected()) return false;

        serverKnowsWeGoaled = client.setGameState(ClientStatus.CLIENT_GOAL).getCode() == APResult.ResultCode.SUCCESS;
        return serverKnowsWeGoaled;
    }



    // ---------------------------------------------------
    // Methods called by the mod/client to impact the game
    // ---------------------------------------------------


    /// Locks every shop item. Called on load/connect before manually unlocking the correct
    /// shop items. Used to avoid issues with incorrectly locked/unlocked species.
    public static void lockShops(){
        for (ShopItem item : GameManager.getShops().getPlantShop().getShopStock()){
            item.setLocked(true);
        }
        for (ShopItem item : GameManager.getShops().getAnimalShop().getShopStock()){
            item.setLocked(true);
        }
    }

    /// Unlocks a species in-game
    /// @param id the id of the species to unlock
    public static void unlockShopItem(int id){
        GameManager.getShops().unlockItem(id);
    }


    /// Sends the player an in-game notification
    /// @param title the notification title text
    /// @param msg the notification body text
    public static void sendNotification(String title, String msg){
        EquilinoxGuis.notify(title, msg, AP_COLOR, null);
    }


    /// Increases the player's dp amount
    public static void giveMoney(int amt){
        GameManager.getSession().getStats().increaseDp(amt);
    }

}
