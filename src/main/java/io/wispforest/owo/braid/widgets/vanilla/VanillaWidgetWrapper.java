package io.wispforest.owo.braid.widgets.vanilla;

import com.mojang.blaze3d.platform.GlStateManager;
import io.wispforest.owo.braid.core.BraidDrawContext;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.instance.MouseListener;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;

import java.util.OptionalDouble;

public class VanillaWidgetWrapper<T extends Drawable & Element> extends LeafInstanceWidget {

    public final T wrapped;

    public VanillaWidgetWrapper(T wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public LeafWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends LeafWidgetInstance<VanillaWidgetWrapper<?>> implements MouseListener {
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
        protected double measureIntrinsicWidth(double height) {
            return 0;
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return 0;
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return OptionalDouble.empty();
        }

        @Override
        public void draw(BraidDrawContext ctx) {
            widget.wrapped.render(ctx, (int) x, (int) y, host().client().getRenderTickCounter().getTickDelta(false));

            GlStateManager._enableScissorTest();
        }

        public boolean onKeyDown(int keyCode, KeyModifiers modifiers) {
            return widget.wrapped.keyPressed(keyCode, 0, modifiers.bitMask());
        }

        public boolean onKeyUp(int keyCode, KeyModifiers modifiers) {
            return widget.wrapped.keyReleased(keyCode, 0, modifiers.bitMask());
        }

        public boolean onChar(int charCode, KeyModifiers modifiers) {
            return widget.wrapped.charTyped((char) charCode, modifiers.bitMask());
        }

        public void onFocusGained() {
            this.widget.wrapped.setFocused(true);
        }

        public void onFocusLost() {
            this.widget.wrapped.setFocused(false);
        }

        @Override
        public boolean onMouseDown(double x, double y, int button, KeyModifiers modifiers) {
            return widget.wrapped.mouseClicked(x, y, button);
        }

        @Override
        public boolean onMouseUp(double x, double y, int button, KeyModifiers modifiers) {
            return widget.wrapped.mouseReleased(x, y, button);
        }

        @Override
        public void onMouseMove(double toX, double toY) {
            this.x = toX;
            this.y = toY;
        }

        @Override
        public void onMouseDragStart(int button, KeyModifiers modifiers) {
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
