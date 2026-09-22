package aaces.aploader;

import aaces.aploader.client.APClient;
import audio.SoundMaestro;
import dropDownBoxUi.ComboBoxUi;
import fontRendering.Text;
import gameManaging.GameManager;
import gameMenu.*;
import languages.GameText;
import mainGuis.ColourPalette;
import mainGuis.GuiSounds;
import session.GameMode;
import usefulUis.PaddedPanelUi;
import userInterfaces.*;
import world.WorldConfigs;
import worldOptions.TerrainType;
import worldOptions.WaterHeights;
import worldOptions.WorldOptionsUi;

import java.lang.reflect.Field;

public class ArchipelagoGui extends SecondPanelUi {
    private final GameMenuGui gameMenu;
    private final GameMenuBackground superMenu;
    private TextFieldGui hostField;
    private TextFieldGui slotField;
    private TextFieldGui passwordField;
    private TextFieldGui nameField;
    private boolean generated = false;
    private ComboBoxUi terrainMenu;
    private ComboBoxUi waterMenu;
    private static final String WORLD_NAME = GameText.getText(885);

    public ArchipelagoGui(GameMenuGui gameMenu, GameMenuBackground superMenu) {
        super(gameMenu);
        this.gameMenu = gameMenu;
        this.superMenu = superMenu;
    }

    protected void init(){
        super.init();
        this.generated = false;
        this.createBackOption();
        this.addConnectButton();
        float yPos = 0.1F;
        this.addHostOption(0.15F, yPos);
        this.addSlotOption(0.4F, yPos);
        this.addPasswordOption(0.65F, yPos);

        yPos += 0.21F;

        this.addNameOption(0.15F, yPos);
        this.waterMenu = this.addOption(0.4F, yPos, GameText.getText(698), WaterHeights.values(), 1);
        this.terrainMenu = this.addOption(0.65F, yPos, GameText.getText(702), TerrainType.values(), 1);

    }

    private void createBackOption() {
        IconButtonUi backButton = new IconButtonUi(GameMenuGui.BACK_ICON, ColourPalette.DARK_GREY, ColourPalette.MIDDLE_GREY);
        super.addComponentY(backButton, GameMenuGui.BACK_BUTTON_POS.x, GameMenuGui.BACK_BUTTON_POS.y, 0.12F);
        ClickListener listener = event -> {
            if (event.isLeftClick()) {
                ArchipelagoGui.this.gameMenu.closeSecondaryScreen();
            }

        };
        backButton.addListener(listener);
    }

    private void addNameOption(float xPos, float yPos) {
        this.addName(xPos, yPos, WORLD_NAME);
        yPos += 0.08F;
        if(GameManager.sessionManager.getSaves().getWaitingSlot() == null) {
            this.nameField = this.addTextField(xPos, yPos, "No Free Save Slots!");
        } else {
            this.nameField = this.addTextField(xPos, yPos, "[AP] " + GameManager.sessionManager.getSaves().getWaitingSlot().getName());
        }
    }

    private void addName(float xPos, float yPos, String name) {
        Text text = Text.newText(name + ":").setFontSize(1.11111F).create();
        text.setColour(ColourPalette.DARK_GREY);
        super.addText(text, xPos, yPos, 1.0F);
    }

    private void addConnectButton() {
        TextButtonUi generateButton = new TextButtonUi("Generate World & Connect", ColourPalette.BASE_BLUE, 1.11111F, ColourPalette.WHITE, 0.2F);
        float yPos = GameMenuGui.BACK_BUTTON_POS.y;
        float xPos = 0.35F;
        super.addComponent(generateButton, xPos, yPos, 0.3F, 0.12F);
        generateButton.addListener(event -> {
            if (event.isLeftClick()) {
                if (ArchipelagoGui.this.generated) {
                    return;
                }

                try {
                    Field textField = TextFieldGui.class.getDeclaredField("text");
                    textField.setAccessible(true);

                    Text hostText = (Text) textField.get(ArchipelagoGui.this.hostField);
                    Text slotText = (Text) textField.get(ArchipelagoGui.this.slotField);
                    Text passwordText = (Text) textField.get(ArchipelagoGui.this.passwordField);

                    if(hostText.isEmpty() || slotText.isEmpty()) {
                        APAgent.log("Can't connect with an empty host/slot");
                        return;
                    }
                    String host = ArchipelagoGui.this.hostField.getCurrentText();
                    String slot = ArchipelagoGui.this.slotField.getCurrentText();
                    String password = passwordText.isEmpty() ? "" : ArchipelagoGui.this.passwordField.getCurrentText();

                    if (APBridge.tryConnectAndGen(host, slot, password)){
                        ArchipelagoGui.this.generated = true;
                        float smoothnessValue = new float[]{0.66F, 0.85F, 1.1F}[ArchipelagoGui.this.terrainMenu.getSelectedIndex()];
                        float waterHeight = new float[]{-4.0F, -2.0F, 0.0F}[ArchipelagoGui.this.waterMenu.getSelectedIndex()];
                        SoundMaestro.playSystemSound(GuiSounds.SELECT);
                        ArchipelagoGui.this.gameMenu.setNewTertiaryScreen(new LoadingScreen(() -> GameManager.sessionManager.hasWorldReady() && !GameManager.sessionManager.isLoading(), ArchipelagoGui.this.superMenu));
                        String name = ArchipelagoGui.this.nameField.getCurrentText();
                        if (name.isEmpty()) {
                            name = "AP Save";
                        }

                        GameManager.sessionManager.startNewWorld(WorldConfigs.create(smoothnessValue, waterHeight), name.replace("-", ""), GameMode.NORMAL);
                        APBridge.saveLoaded = true;
                    }
                } catch (Exception e) {
                    APAgent.log("Error parsing connection info & generating world!");
                    APAgent.log(e.getMessage());
                }
            }

        });
    }

    private void addHostOption(float xPos, float yPos){
        this.addLabel(xPos, yPos, "Host");
        yPos += 0.08F;
        this.hostField = this.addTextField(xPos, yPos, APBridge.host.isEmpty() ? "archipelago.gg:12345" : APBridge.host);
    }

    private void addSlotOption(float xPos, float yPos){
        this.addLabel(xPos, yPos, "Slot");
        yPos += 0.08F;
        this.slotField = this.addTextField(xPos, yPos, APBridge.slot.isEmpty() ? "Player1" : APBridge.slot);
    }

    private void addPasswordOption(float xPos, float yPos){
        this.addLabel(xPos, yPos, "Password");
        yPos += 0.08F;
        this.passwordField = this.addTextField(xPos, yPos, APBridge.password);
    }

    private void addLabel(float xPos, float yPos, String name) {
        Text text = Text.newText(name + ":").setFontSize(1.11111F).create();
        text.setColour(ColourPalette.DARK_GREY);
        super.addText(text, xPos, yPos, 1.0F);
    }

    private TextFieldGui addTextField(float xPos, float yPos, String initialText) {
        PaddedPanelUi panel = new PaddedPanelUi(ColourPalette.MIDDLE_GREY);
        panel.setPadding(8, 2, 2, 0);
        TextFieldGui field = new TextFieldGui(initialText, 1.11111F, 30, false, false);
        panel.displayComponent(field);
        super.addComponent(panel, xPos, yPos, 0.2F, 0.08F);
        return field;
    }

    private ComboBoxUi addOption(float xPos, float yPos, String name, Object[] options, int selected) {
        this.addName(xPos, yPos, name);
        yPos += 0.08F;
        return this.addMenu(xPos, yPos, options, selected);
    }

    private ComboBoxUi addMenu(float xPos, float yPos, Object[] options, int selected) {
        ComboBoxUi menu = new ComboBoxUi(options, selected);
        menu.setFontSize(1.11111F);
        super.addComponent(menu, xPos, yPos, 0.2F, 0.08F);
        return menu;
    }

}
