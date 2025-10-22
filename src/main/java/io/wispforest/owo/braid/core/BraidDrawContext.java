package io.wispforest.owo.braid.core;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.wispforest.owo.braid.core.element.BraidDashedLineElement;
import io.wispforest.owo.mixin.braid.Matrix3x2fStackAccessor;
import io.wispforest.owo.mixin.ui.access.DrawContextAccessor;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.GuiRenderState;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2fc;

import java.util.function.Consumer;

public class BraidDrawContext extends OwoUIDrawContext {

    private final Surface surface;

    protected BraidDrawContext(MinecraftClient client, GuiRenderState renderState, Consumer<Runnable> setTooltipDrawer, Surface surface) {
        super(client, renderState, setTooltipDrawer);
        this.surface = surface;
    }

    public static BraidDrawContext create(DrawContext context, Surface surface) {
        var braidContext = new BraidDrawContext(
            MinecraftClient.getInstance(),
            context.state,
            ((DrawContextAccessor) context)::owo$setTooltipDrawer,
            surface
        );
        ((DrawContextAccessor) braidContext).owo$setScissorStack(((DrawContextAccessor) context).owo$getScissorStack());
        ((DrawContextAccessor) braidContext).owo$setMatrices(new MatrixStack(((DrawContextAccessor) context).owo$getMatrices()));

        return braidContext;
    }

    @Override
    public int getScaledWindowWidth() {
        return this.surface.width();
    }

    @Override
    public int getScaledWindowHeight() {
        return this.surface.height();
    }

    public void buildRectOutline(double x, double y, double width, double height, RectEdgeBuilder builder) {
        builder.edge(x, y, x + width, y);
        builder.edge(x, y + height, x + width, y + height);

        builder.edge(x, y, x, y + height);
        builder.edge(x + width, y, x + width, y + height);
    }

    public void drawDashedLine(RenderPipeline pipeline, double x1, double y1, double x2, double y2, double thiccness, double segmentLength, Color color) {
        this.state.addSimpleElement(new BraidDashedLineElement(
            color,
            thiccness,
            segmentLength,
            pipeline,
            new Matrix3x2f(this.getMatrices()),
            new ScreenRect((int) x1, (int) y1, (int) (x2 - x1), (int) (y2 - y1)),
            this.scissorStack.peekLast()
        ));
    }

    @FunctionalInterface
    public interface RectEdgeBuilder {
        void edge(double x1, double y1, double x2, double y2);
    }

    @SuppressWarnings("ExternalizableWithoutPublicNoArgConstructor")
    public static class MatrixStack extends Matrix3x2fStack {

        public MatrixStack(Matrix3x2fc source) {
            super(16);
            this.mul(source);
        }

        @Override
        public Matrix3x2fStack pushMatrix() {
            var accessor = (Matrix3x2fStackAccessor) this;

            if (accessor.owo$getCurr() == accessor.owo$getMats().length) {
                var newMats = new Matrix3x2f[accessor.owo$getMats().length * 2];
                System.arraycopy(accessor.owo$getMats(), 0, newMats, 0, accessor.owo$getMats().length);
                for (int idx = newMats.length / 2; idx < newMats.length; idx++) {
                    newMats[idx] = new Matrix3x2f();
                }

                accessor.owo$setMats(newMats);
            }

            return super.pushMatrix();
        }
    }
}
