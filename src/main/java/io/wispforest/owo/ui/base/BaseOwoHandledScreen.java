package io.wispforest.owo.ui.base;

import io.wispforest.owo.Owo;
import io.wispforest.owo.mixin.ui.SlotAccessor;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.inject.GreedyInputComponent;
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
import org.lwjgl.opengl.GL11;

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

        // Check whether this screen was already initialized
        if (this.uiAdapter != null) {
            // If it was, only resize the adapter instead of recreating it - this preserves UI state
            this.uiAdapter.moveAndResize(0, 0, this.width, this.height);
            // Re-add it as a child to circumvent vanilla clearing them
            this.addDrawableChild(this.uiAdapter);
        } else {
            try {
                this.uiAdapter = this.createAdapter();
                this.build(this.uiAdapter.rootComponent);

                this.uiAdapter.inflateAndMount();
            } catch (Exception error) {
                Owo.LOGGER.warn("Could not initialize owo screen", error);
                UIErrorToast.report(error);
                this.invalid = true;
            }
        }
    }

    /**
     * Disable the slot at the given index. Note
     * that this is hard override and the slot cannot
     * re-enable itself
     *
     * @param index The index of the slot to disable
     */
    protected void disableSlot(int index) {
        ((OwoSlotExtension) this.handler.slots.get(index)).owo$setDisabledOverride(true);
    }

    /**
     * Disable the given slot. Note that
     * this is hard override and the slot cannot
     * re-enable itself
     */
    protected void disableSlot(Slot slot) {
        ((OwoSlotExtension) slot).owo$setDisabledOverride(true);
    }

    /**
     * Enable the slot at the given index. Note
     * that this is an override and cannot enable
     * a slot that is disabled through its own will
     *
     * @param index The index of the slot to enable
     */
    protected void enableSlot(int index) {
        ((OwoSlotExtension) this.handler.slots.get(index)).owo$setDisabledOverride(false);
    }

    /**
     * Enable the given slot. Note that
     * this is an override and cannot enable
     * a slot that is disabled through its own will
     */
    protected void enableSlot(Slot slot) {
        ((OwoSlotExtension) slot).owo$setDisabledOverride(true);
    }

    protected boolean isSlotEnabled(int index) {
        return ((OwoSlotExtension) this.handler.slots.get(index)).owo$getDisabledOverride();
    }

    protected boolean isSlotEnabled(Slot slot) {
        return ((OwoSlotExtension) slot).owo$getDisabledOverride();
    }

    /**
     * Wrap the slot and the given index in this screen's
     * handler into a component, so it can be managed by the UI system
     *
     * @param index The index the slot occupies in the handler's slot list
     * @return The wrapped slot
     */
    protected SlotComponent slotAsComponent(int index) {
        return new SlotComponent(index);
    }

    /**
     * A convenience shorthand for querying a component from the adapter's
     * root component via {@link ParentComponent#childById(Class, String)}
     */
    protected <C extends Component> @Nullable C component(Class<C> expectedClass, String id) {
        return this.uiAdapter.rootComponent.childById(expectedClass, id);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (!this.invalid) {
            super.render(matrices, mouseX, mouseY, delta);

            if (this.uiAdapter.enableInspector) {
                matrices.translate(0, 0, 500);

                for (int i = 0; i < this.handler.slots.size(); i++) {
                    var slot = this.handler.slots.get(i);
                    if (!slot.isEnabled()) continue;

                    Drawer.drawText(matrices, Text.literal("H:" + i),
                            this.x + slot.x + 15, this.y + slot.y + 9, .5f, 0x0096FF,
                            Drawer.TextAnchor.BOTTOM_RIGHT
                    );
                    Drawer.drawText(matrices, Text.literal("I:" + slot.getIndex()),
                            this.x + slot.x + 15, this.y + slot.y + 15, .5f, 0x5800FF,
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

        return (modifiers & GLFW.GLFW_MOD_CONTROL) == 0 && this.uiAdapter.rootComponent.focusHandler().focused() instanceof GreedyInputComponent inputComponent
                ? inputComponent.onKeyPress(keyCode, scanCode, modifiers)
                : super.keyPressed(keyCode, scanCode, modifiers);
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
        super.removed();
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {}

    public class SlotComponent extends BaseComponent {

        protected final Slot slot;
        protected boolean didDraw = false;

        protected SlotComponent(int index) {
            this.slot = BaseOwoHandledScreen.this.handler.getSlot(index);
        }

        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
            this.didDraw = true;

            int[] scissor = new int[4];
            GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, scissor);

            ((OwoSlotExtension) this.slot).owo$setScissorArea(PositionedRectangle.of(
                    scissor[0], scissor[1], scissor[2], scissor[3]
            ));
        }

        @Override
        public void update(float delta, int mouseX, int mouseY) {
            super.update(delta, mouseX, mouseY);

            ((OwoSlotExtension) this.slot).owo$setDisabledOverride(!this.didDraw);

            this.didDraw = false;
        }

        @Override
        public void drawTooltip(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
            if (!this.slot.hasStack()) {
                super.drawTooltip(matrices, mouseX, mouseY, partialTicks, delta);
            }
        }

        @Override
        public boolean shouldDrawTooltip(double mouseX, double mouseY) {
            return super.shouldDrawTooltip(mouseX, mouseY);
        }

        @Override
        protected int determineHorizontalContentSize(Sizing sizing) {
            return 16;
        }

        @Override
        protected int determineVerticalContentSize(Sizing sizing) {
            return 16;
        }

        @Override
        public void updateX(int x) {
            super.updateX(x);
            ((SlotAccessor) this.slot).owo$setX(x - BaseOwoHandledScreen.this.x);
        }

        @Override
        public void updateY(int y) {
            super.updateY(y);
            ((SlotAccessor) this.slot).owo$setY(y - BaseOwoHandledScreen.this.y);
        }
    }
}
