package io.wispforest.owo.braid.widgets.vanilla;

import com.mojang.blaze3d.opengl.GlStateManager;
import io.wispforest.owo.braid.core.BraidGraphics;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.framework.instance.LeafWidgetInstance;
import io.wispforest.owo.braid.framework.instance.MouseListener;
import io.wispforest.owo.braid.framework.widget.LeafInstanceWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

import java.util.OptionalDouble;

public class VanillaWidgetWrapper<T extends Renderable & GuiEventListener> extends LeafInstanceWidget {

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
            if (widget.wrapped instanceof LayoutElement layoutElement) {
                layoutElement.setPosition(0, 0);
            }

            var size = constraints.hasBoundedWidth() && constraints.hasBoundedHeight()
                ? constraints.maxSize()
                : constraints.minSize();

            if (widget.wrapped instanceof AbstractWidget abstractWidget) {
                abstractWidget.setWidth((int) size.width());
                abstractWidget.setHeight((int) size.height());
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
        public void draw(BraidGraphics graphics) {
            widget.wrapped.render(graphics, (int) x, (int) y, host().client().getDeltaTracker().getGameTimeDeltaPartialTick(false));

            GlStateManager._enableScissorTest();
        }

        public boolean onKeyDown(int keyCode, KeyModifiers modifiers) {
            return widget.wrapped.keyPressed(new KeyEvent(keyCode, 0, modifiers.bitMask()));
        }

        public boolean onKeyUp(int keyCode, KeyModifiers modifiers) {
            return widget.wrapped.keyReleased(new KeyEvent(keyCode, 0, modifiers.bitMask()));
        }

        public boolean onChar(int charCode, KeyModifiers modifiers) {
            return widget.wrapped.charTyped(new CharacterEvent(charCode, modifiers.bitMask()));
        }

        public void onFocusGained() {
            this.widget.wrapped.setFocused(true);
        }

        public void onFocusLost() {
            this.widget.wrapped.setFocused(false);
        }

        @Override
        public boolean onMouseDown(double x, double y, int button, KeyModifiers modifiers) {
            return widget.wrapped.mouseClicked(new MouseButtonEvent(x, y, new MouseButtonInfo(button, modifiers.bitMask())), false);
        }

        @Override
        public boolean onMouseUp(double x, double y, int button, KeyModifiers modifiers) {
            return widget.wrapped.mouseReleased(new MouseButtonEvent(x, y, new MouseButtonInfo(button, modifiers.bitMask())));
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
            this.widget.wrapped.mouseDragged(new MouseButtonEvent(x, y, new MouseButtonInfo(draggingMouseButton, 0)), (int) dx, (int) dy);
        }

        @Override
        public boolean onMouseScroll(double x, double y, double horizontal, double vertical) {
            return widget.wrapped.mouseScrolled(x, y, horizontal, vertical);
        }
    }
}
