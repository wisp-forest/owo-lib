package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.BraidDrawContext;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.ui.renderstate.BlurQuadElementRenderState;
import net.minecraft.client.gui.ScreenRect;
import org.joml.Matrix3x2f;

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
        public void draw(BraidDrawContext ctx) {
            if (!this.widget.blurChild) {
                this.drawBlur(ctx);
            }

            super.draw(ctx);

            if (this.widget.blurChild) {
                this.drawBlur(ctx);
            }
        }

        private void drawBlur(BraidDrawContext ctx) {
            ctx.state.addSimpleElement(new BlurQuadElementRenderState(
                new Matrix3x2f(ctx.getMatrices()),
                new ScreenRect(0, 0, (int) this.transform.width(), (int) this.transform.height()),
                ctx.scissorStack.peekLast(),
                16, this.widget.quality, this.widget.size
            ));
        }
    }
}
