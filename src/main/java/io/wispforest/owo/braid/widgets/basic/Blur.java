package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.BraidDrawContext;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.client.OwoClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
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
                ctx.draw();
                this.drawBlur(ctx);
            }

            super.draw(ctx);

            if (this.widget.blurChild) {
                ctx.draw();
                this.drawBlur(ctx);
            }
        }

        private void drawBlur(BraidDrawContext ctx) {
            var buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
            var matrix = ctx.getMatrices().peek().getPositionMatrix();

            buffer.vertex(matrix, 0, 0, 0);
            buffer.vertex(matrix, 0, (float) this.transform.height(), 0);
            buffer.vertex(matrix, (float) this.transform.width(), (float) this.transform.height(), 0);
            buffer.vertex(matrix, (float) this.transform.width(), 0, 0);

            OwoClient.BLUR_PROGRAM.setParameters(16, this.widget.quality, this.widget.size);
            OwoClient.BLUR_PROGRAM.use();
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }
    }
}
