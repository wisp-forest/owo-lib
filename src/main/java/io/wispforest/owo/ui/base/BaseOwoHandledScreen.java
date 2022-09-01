package io.wispforest.owo.ui.base;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.ui.util.UIErrorToast;
import io.wispforest.owo.util.pond.OwoSlotExtension;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.function.BiFunction;

public abstract class BaseOwoHandledScreen<R extends ParentComponent, S extends ScreenHandler> extends HandledScreen<S> {

    /**
     * The UI adapter of this screen. This handles
     * all user input as well as setting up GL state for rendering
     * and managing component focus
     */
    protected OwoUIAdapter<R> uiAdapter = null;

    /**
     * Whether this screen has encountered an unrecoverable
     * error during its lifecycle and should thus close
     * itself on the next frame
     */
    protected boolean invalid = false;

    protected BaseOwoHandledScreen(S handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    /**
     * Initialize the UI adapter for this screen. Usually
     * the body of this method will simply consist of a call
     * to {@link OwoUIAdapter#create(Screen, BiFunction)}
     *
     * @return The UI adapter for this screen to use
     */
    protected abstract @NotNull OwoUIAdapter<R> createAdapter();

    /**
     * Build the component hierarchy of this screen,
     * called after the adapter and root component have been
     * initialized by {@link #createAdapter()}
     *
     * @param rootComponent The root component created
     *                      in the previous initialization step
     */
    protected abstract void build(R rootComponent);

    @Override
    protected void init() {
        super.init();

        if (this.invalid) return;

        if (this.uiAdapter != null) {
            this.uiAdapter.dispose();
        }

        try {
            this.uiAdapter = this.createAdapter();
            this.build(this.uiAdapter.rootComponent);

            this.uiAdapter.inflateAndMount();
            this.client.keyboard.setRepeatEvents(true);
        } catch (Exception error) {
            Owo.LOGGER.warn("Could not initialize owo screen", error);
            UIErrorToast.report(error);
            this.invalid = true;
        }
    }

    /**
     * Disables the slot at the given index. Note
     * that this is hard override and the slot cannot
     * re-enable itself
     *
     * @param index The index of the slot to disable
     */
    protected void disableSlot(int index) {
        final var slot = (OwoSlotExtension) this.slotByIndex(index);
        if (slot == null) return;

        slot.owo$setDisabledOverride(true);
    }

    /**
     * Enables the slot at the given index. Note
     * that this is an override and cannot enable
     * a slot that is disabled through its own will
     *
     * @param index The index of the slot to enable
     */
    protected void enableSlot(int index) {
        final var slot = (OwoSlotExtension) this.slotByIndex(index);
        if (slot == null) return;

        slot.owo$setDisabledOverride(false);
    }

    protected boolean isSlotEnabled(int index) {
        final var slot = (OwoSlotExtension) this.slotByIndex(index);
        if (slot == null) return false;

        return slot.owo$getDisabledOverride();
    }

    protected @Nullable Slot slotByIndex(int index) {
        for (var slot : this.handler.slots) {
            if (slot.getIndex() == index) return slot;
        }

        return null;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!this.invalid) {
            super.render(matrices, mouseX, mouseY, delta);

            if (this.uiAdapter.enableInspector) {
                matrices.translate(0, 0, 500);

                for (var slot : this.handler.slots) {
                    if (!slot.isEnabled()) continue;

                    Drawer.drawText(matrices, Text.literal(String.valueOf(slot.getIndex())),
                            this.x + slot.x + 15, this.y + slot.y + 15, .6f, 0x5800FF,
                            Drawer.TextAnchor.BOTTOM_RIGHT
                    );
                }

                matrices.translate(0, 0, -500);
            }

            this.drawMouseoverTooltip(matrices, mouseX, mouseY);
        } else {
            this.close();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
            this.close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.uiAdapter.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Nullable
    @Override
    public Element getFocused() {
        return this.uiAdapter;
    }

    @Override
    public void removed() {
        if (this.uiAdapter != null) this.uiAdapter.dispose();
        this.client.keyboard.setRepeatEvents(false);
        super.removed();
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {}
}
