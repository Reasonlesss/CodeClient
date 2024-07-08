package dev.dfonline.codeclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public abstract class ChestFeature {
    protected HandledScreen<?> screen;

    public ChestFeature(HandledScreen<?> screen) {
        this.screen = screen;
    }

    public void render(DrawContext context, int mouseX, int mouseY, int x, int y) {}

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        return false;
    }

    public void clickSlot(Slot slot, int button, SlotActionType actionType, int syncId, int revision) {}

}
