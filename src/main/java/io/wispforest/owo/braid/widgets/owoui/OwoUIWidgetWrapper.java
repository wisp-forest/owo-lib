package io.wispforest.owo.braid.widgets.owoui;

import com.mojang.blaze3d.platform.GlStateManager;
import io.wispforest.owo.braid.core.BraidDrawContext;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.instance.MouseListener;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.ParentComponent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.OptionalDouble;

public class OwoUIWidgetWrapper extends LeafInstanceWidget {
    private final ParentComponent rootComponent;

    public OwoUIWidgetWrapper(ParentComponent rootComponent) {
        this.rootComponent = rootComponent;
    }

    @Override
    public LeafWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends LeafWidgetInstance<OwoUIWidgetWrapper> implements MouseListener {
        private int mouseX = -100;
        private int mouseY = -100;

        private int dragButton = -1;

        public Instance(OwoUIWidgetWrapper widget) {
            super(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            Size space = constraints.maxFiniteOrMinSize();

            widget.rootComponent.inflate(io.wispforest.owo.ui.core.Size.of((int) space.width(), (int) space.height()));
            widget.rootComponent.mount(null, 0, 0);

            this.transform.setSize(Size.of(widget.rootComponent.width(), widget.rootComponent.height())
                .constrained(constraints));
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            // the focus handler is created on mount, therefore it's null only if the
            // component wasn't mounted.
            if (widget.rootComponent.focusHandler() != null) {
                throw new IllegalStateException("Tried to measure intrinsic width of mounted owo-ui component");
            }

            widget.rootComponent.inflate(io.wispforest.owo.ui.core.Size.of(Integer.MAX_VALUE, (int) height));
            return widget.rootComponent.width();
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            // the focus handler is created on mount, therefore it's null only if the
            // component wasn't mounted.
            if (widget.rootComponent.focusHandler() != null) {
                throw new IllegalStateException("Tried to measure intrinsic height of mounted owo-ui component");
            }

            widget.rootComponent.inflate(io.wispforest.owo.ui.core.Size.of((int) width, Integer.MAX_VALUE));
            return widget.rootComponent.height();
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return OptionalDouble.empty();
        }

        @Override
        public void onMouseMove(double toX, double toY) {
            this.mouseX = (int) toX;
            this.mouseY = (int) toY;
        }

        @Override
        public void onMouseExit() {
            this.mouseX = -100;
            this.mouseY = -100;
        }

        public void onFocusLost() {
            this.widget.rootComponent.focusHandler().focus(null, Component.FocusSource.MOUSE_CLICK);
        }

        @Override
        public @Nullable CursorStyle cursorStyleAt(double x, double y) {
            var hovered = this.widget.rootComponent.childAt((int) x, (int) y);

            if (hovered == null) return null;

            return switch (hovered.cursorStyle()) {
                case NONE -> CursorStyle.NONE;
                case POINTER -> CursorStyle.POINTER;
                case TEXT -> CursorStyle.TEXT;
                case HAND -> CursorStyle.HAND;
                case CROSSHAIR -> CursorStyle.CROSSHAIR;
                case MOVE -> CursorStyle.MOVE;
                case HORIZONTAL_RESIZE -> CursorStyle.HORIZONTAL_RESIZE;
                case VERTICAL_RESIZE -> CursorStyle.VERTICAL_RESIZE;
                case NWSE_RESIZE -> CursorStyle.NWSE_RESIZE;
                case NESW_RESIZE -> CursorStyle.NESW_RESIZE;
                case NOT_ALLOWED -> CursorStyle.NOT_ALLOWED;
            };
        }

        @Override
        public boolean onMouseDown(double x, double y, int button, KeyModifiers modifiers) {
            return this.widget.rootComponent.onMouseDown(x, y, button);
        }

        @Override
        public boolean onMouseUp(double x, double y, int button, KeyModifiers modifiers) {
            return this.widget.rootComponent.onMouseUp(x, y, button);
        }

        @Override
        public boolean onMouseScroll(double x, double y, double horizontal, double vertical) {
            return this.widget.rootComponent.onMouseScroll(x, y, vertical);
        }

        @Override
        public void onMouseDragStart(int button, KeyModifiers modifiers) {
            this.dragButton = button;
        }

        @Override
        public void onMouseDrag(double x, double y, double dx, double dy) {
            this.widget.rootComponent.onMouseDrag(x, y, dx, dy, dragButton);
        }

        @Override
        public void onMouseDragEnd() {
            this.dragButton = -1;
        }

        public boolean onKeyDown(int keyCode, KeyModifiers modifiers) {
            return this.widget.rootComponent.onKeyPress(keyCode, GLFW.glfwGetKeyScancode(keyCode), modifiers.bitMask());
        }

        public boolean onChar(int charCode, KeyModifiers modifiers) {
            return this.widget.rootComponent.onCharTyped((char) charCode, modifiers.bitMask());
        }

        @Override
        public void draw(BraidDrawContext ctx) {
            var client = host().client();

            this.widget.rootComponent.update(
                client.getRenderTickCounter().getLastFrameDuration(),
                mouseX,
                mouseY
            );

            this.widget.rootComponent.draw(
                ctx,
                mouseX,
                mouseY,
                client.getRenderTickCounter().getTickDelta(false),
                client.getRenderTickCounter().getLastFrameDuration()
            );

            // TODO: tooltips.

            // this mitigates the vanilla scissor stack disabling the scissor stack if it's empty
            GlStateManager._enableScissorTest();
        }
    }
}
