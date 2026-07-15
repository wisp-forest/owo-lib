package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.BraidGraphics;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.ui.renderstate.BlurQuadElementRenderState;

public class Blur extends SingleChildInstanceWidget {

    public final float quality;
    public final float size;
    public final boolean blurChild;

    public Blur(float quality, float size, boolean blurChild, Widget child) {
        super(child);
        this.quality = quality;
        this.size = size;
        this.blurChild = blurChild;
    }

    @Override
    public SingleChildWidgetInstance<Blur> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance.ShrinkWrap<Blur> {

        public Instance(Blur widget) {
            super(widget);
        }

        @Override
        public void draw(BraidGraphics graphics) {
            if (!this.widget.blurChild) {
                BlurQuadElementRenderState.blurBackground(graphics.guiRenderState, (int) this.widget.quality, this.widget.size);
            }

            super.draw(graphics);

            if (this.widget.blurChild) {
                BlurQuadElementRenderState.blurBackground(graphics.guiRenderState, (int) this.widget.quality, this.widget.size);
            }
        }
    }
}
