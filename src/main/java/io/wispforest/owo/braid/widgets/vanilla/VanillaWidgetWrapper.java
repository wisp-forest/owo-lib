package io.wispforest.owo.braid.widgets.vanilla;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.framework.instance.KeyboardListener;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.instance.MouseListener;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;

public class VanillaWidgetWrapper<T extends Drawable & Element> extends LeafInstanceWidget {

    public final T wrapped;

    public VanillaWidgetWrapper(T wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public LeafWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends LeafWidgetInstance<VanillaWidgetWrapper<?>> implements MouseListener, KeyboardListener {
        private int draggingMouseButton = 0;

        private double x, y;

        public Instance(VanillaWidgetWrapper<?> widget) {
            super(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            if (widget.wrapped instanceof Widget wrappedWidget) {
                wrappedWidget.setPosition(0, 0);
            }

            var size = constraints.hasBoundedWidth() && constraints.hasBoundedHeight()
                ? constraints.maxSize()
                : constraints.minSize();

            if (widget.wrapped instanceof ClickableWidget clickableWidget) {
                clickableWidget.setWidth((int) size.width());
                clickableWidget.setHeight((int) size.height());
            }

            this.transform.setSize(size);
        }

        @Override
        public void draw(OwoUIDrawContext ctx) {
            widget.wrapped.render(ctx, (int) x, (int) y, host().client().getRenderTickCounter().getTickDelta(false));
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyModifiers modifiers) {
            return widget.wrapped.keyPressed(keyCode, 0, modifiers.bitMask());
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyModifiers modifiers) {
            return widget.wrapped.keyReleased(keyCode, 0, modifiers.bitMask());
        }

        @Override
        public boolean onChar(int charCode, KeyModifiers modifiers) {
            return widget.wrapped.charTyped((char) charCode, modifiers.bitMask());
        }

        @Override
        public void onFocusGained() {
            this.widget.wrapped.setFocused(true);
        }

        @Override
        public void onFocusLost() {
            this.widget.wrapped.setFocused(false);
        }

        @Override
        public boolean onMouseDown(double x, double y, int button) {
            return widget.wrapped.mouseClicked(x, y, button);
        }

        @Override
        public boolean onMouseUp(double x, double y, int button) {
            return widget.wrapped.mouseReleased(x, y, button);
        }

        @Override
        public void onMouseMove(double toX, double toY) {
            this.x = toX;
            this.y = toY;
        }

        @Override
        public void onMouseDragStart(int button) {
            draggingMouseButton = button;
        }

        @Override
        public void onMouseDrag(double x, double y, double dx, double dy) {
            this.widget.wrapped.mouseDragged((int) x, (int) y, (int) dx, (int) dy, draggingMouseButton);
        }

        @Override
        public boolean onMouseScroll(double x, double y, double horizontal, double vertical) {
            return widget.wrapped.mouseScrolled(x, y, horizontal, vertical);
        }
    }
}
