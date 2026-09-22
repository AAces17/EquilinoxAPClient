package aaces.aploader;

import aaces.aploader.client.APClient;
import aaces.aploader.client.ItemManager;
import aaces.aploader.client.LocationManager;
import fontRendering.Text;
import guiRendering.GuiRenderData;
import guis.GuiComponent;
import mainGuis.ColourPalette;
import mainGuis.UiSettings;
import org.lwjgl.util.vector.Vector2f;
import usefulUis.PaddedPanelUi;
import userInterfaces.TextButtonUi;
import userInterfaces.TextFieldGui;

import java.lang.reflect.Field;

public class ArchipelagoToolbarGui extends GuiComponent {

    private final String host;
    private final String slot;
    private final String password;
    private final ItemManager itemManager;
    private final LocationManager locationManager;

    private TextFieldGui hostField;
    private TextFieldGui slotField;
    private TextFieldGui passwordField;


    public ArchipelagoToolbarGui(String host, String slot, String password, ItemManager itemManager, LocationManager locationManager) {
        this.host = host;
        this.slot = slot;
        this.password = password;
        if (itemManager != null) {
            this.itemManager = itemManager;
        } else {
            this.itemManager = new ItemManager(0);
        }
        if (locationManager != null) {
            this.locationManager = locationManager;
        } else {
            this.locationManager = new LocationManager();
        }
    }

    protected void init(){
        super.init();
        addInputFields();
        addConnectButton();
    }

    private void addInputFields(){
        float offset = super.pixelsToRelativeX(40.0F);
        float width = super.pixelsToRelativeX(300.0F);

        addLabel(0.907F - (width + offset), super.pixelsToRelativeY(10.0F), "Host");
        addLabel(0.915F - (width + offset), super.pixelsToRelativeY(50.0F), "Slot");
        addLabel(0.85F - (width + offset), super.pixelsToRelativeY(90.0F), "Password");

        this.hostField = addTextField(1.0F - (width + offset), super.pixelsToRelativeY(10.0F), host.isEmpty() ? "archipelago.gg:xxxxx" : host);
        this.slotField = addTextField(1.0F - (width + offset), super.pixelsToRelativeY(50.0F), slot.isEmpty() ? "Player1" : slot);
        this.passwordField = addTextField(1.0F - (width + offset), super.pixelsToRelativeY(90.0F), password);
    }

    private void addConnectButton(){
        float xPos = 0.5F;
        TextButtonUi connectButton = new TextButtonUi("Connect", ColourPalette.BASE_BLUE, 0.9F, ColourPalette.WHITE, 0.1F);
        super.addComponent(connectButton, xPos, super.pixelsToRelativeY(150.0F), 0.3F, 0.05F);
        connectButton.addListener(event -> {
            if (event.isLeftClick()){
                APAgent.log("Toolbar Connect button pressed");
                if(!APBridge.saveLoaded) {
                    APBridge.sendNotification("Can't Connect This Way", "To connect, load an existing AP save or use the 'New World' button.");
                    APAgent.log("No save loaded yet");
                    return;
                }
                if(APClient.Connecting){
                    APBridge.sendNotification("Already Trying to Connect", "Please wait");
                    APAgent.log("Already trying to connect");
                    return;
                }
                APAgent.log("Trying to connect via toolbar button");
                try {
                    Field textField = TextFieldGui.class.getDeclaredField("text");
                    textField.setAccessible(true);

                    Text hostText = (Text) textField.get(ArchipelagoToolbarGui.this.hostField);
                    Text slotText = (Text) textField.get(ArchipelagoToolbarGui.this.slotField);
                    Text passwordText = (Text) textField.get(ArchipelagoToolbarGui.this.passwordField);

                    if(hostText.isEmpty() || slotText.isEmpty()) {
                        APAgent.log("Can't connect with an empty host/slot");
                        return;
                    }
                    String host = ArchipelagoToolbarGui.this.hostField.getCurrentText();
                    String slot = ArchipelagoToolbarGui.this.slotField.getCurrentText();
                    String password = passwordText.isEmpty() ? "" : ArchipelagoToolbarGui.this.passwordField.getCurrentText();
                    APBridge.newConnection(host, slot, password, ArchipelagoToolbarGui.this.itemManager, ArchipelagoToolbarGui.this.locationManager);
                } catch (Exception e) {
                    APAgent.log("Error reconnecting");
                    APAgent.log(e.getMessage());
                }
            }
        });
    }

    private void addLabel(float xPos, float yPos, String name) {
        Text text = Text.newText(name + ":").setFontSize(0.75F).create();
        text.setColour(ColourPalette.WHITE);
        super.addText(text, xPos, yPos, 1.0F);
    }

    private TextFieldGui addTextField(float xPos, float yPos, String initialText) {
        PaddedPanelUi panel = new PaddedPanelUi(ColourPalette.MIDDLE_GREY);
        panel.setPadding(8, 2, 2, 0);
        TextFieldGui field = new TextFieldGui(initialText, 0.75F, 30, false, false);
        panel.displayComponent(field);
        super.addComponent(panel, xPos, yPos, super.pixelsToRelativeX(300.0F), super.pixelsToRelativeY(35.0F));
        return field;
    }

    @Override
    protected void updateGuiTexturePositions(Vector2f vector2f, Vector2f vector2f1) {
    }

    @Override
    protected void updateSelf() {
        // TODO: display connection status
    }

    @Override
    protected void getGuiTextures(GuiRenderData guiRenderData) {
    }
}
