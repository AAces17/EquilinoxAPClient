package aaces.aploader;

import gameManaging.GameManager;
import guiRendering.GuiRenderData;
import guis.GuiTexture;
import mainGuis.ColourPalette;
import mainGuis.GuiRepository;
import org.lwjgl.util.vector.Vector2f;
import userInterfaces.GuiClickable;

public class SpeedToggleButtonUi extends GuiClickable {

    private final GuiTexture texture;
    private boolean fastForward;

    public SpeedToggleButtonUi() {
        this.texture = new GuiTexture(GuiRepository.FAST_FORWARD);
        this.addToggleListener();
        this.fastForward = false;
    }

    protected void updateGuiTexturePositions(Vector2f position, Vector2f scale) {
        super.updateGuiTexturePositions(position, scale);
        this.texture.setPosition(position.x, position.y, scale.x, scale.y);
    }

    protected void updateSelf() {
        super.updateSelf();
    }

    private void addToggleListener() {
        super.addListener(event -> {
            if (event.isLeftClick()) {
                if (fastForward) {
                    SpeedToggleButtonUi.this.fastForward = false;
                    SpeedToggleButtonUi.this.texture.setOverrideColour(ColourPalette.WHITE);
                    GameManager.getGameSpeed().normalSpeed();
                } else {
                    SpeedToggleButtonUi.this.fastForward = true;
                    SpeedToggleButtonUi.this.texture.setOverrideColour(ColourPalette.BASE_BLUE);
                    GameManager.getGameSpeed().fastSpeed();
                }
            }
        });
    }

    @Override
    protected void getGuiTextures(GuiRenderData data) {
        data.addTexture(this.getLevel(), this.texture);
    }
}
