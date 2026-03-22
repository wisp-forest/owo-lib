package io.wispforest.owo.braid.core;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.wispforest.owo.braid.core.element.BraidDashedLineElement;
import io.wispforest.owo.mixin.braid.Matrix3x2fStackAccessor;
import io.wispforest.owo.mixin.ui.access.GuiGraphicsExtractorAccessor;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2fc;

import java.util.function.Consumer;

public class BraidGraphics extends OwoUIGraphics {

    private final Surface surface;

    protected BraidGraphics(Minecraft client, GuiRenderState renderState, int mouseX, int mouseY, Consumer<Runnable> setTooltipDrawer, Surface surface) {
        super(client, renderState, mouseX, mouseY, setTooltipDrawer);
        this.surface = surface;
    }

    public static BraidGraphics create(GuiGraphicsExtractor graphics, Surface surface) {
        var braidContext = new BraidGraphics(
            Minecraft.getInstance(),
            graphics.guiRenderState,
            ((GuiGraphicsExtractorAccessor) graphics).owo$getMouseX(),
            ((GuiGraphicsExtractorAccessor) graphics).owo$getMouseY(),
            ((GuiGraphicsExtractorAccessor) graphics)::owo$setDeferredTooltip,
            surface
        );
        ((GuiGraphicsExtractorAccessor) braidContext).owo$setScissorStack(((GuiGraphicsExtractorAccessor) graphics).owo$getScissorStack());
        ((GuiGraphicsExtractorAccessor) braidContext).owo$setPose(new MatrixStack(((GuiGraphicsExtractorAccessor) graphics).owo$getPose()));

        return braidContext;
    }

    @Override
    public int guiWidth() {
        return this.surface.width();
    }

    @Override
    public int guiHeight() {
        return this.surface.height();
    }

    public void buildRectOutline(double x, double y, double width, double height, RectEdgeBuilder builder) {
        builder.edge(x, y, x + width, y);
        builder.edge(x, y + height, x + width, y + height);

        builder.edge(x, y, x, y + height);
        builder.edge(x + width, y, x + width, y + height);
    }

    public void drawDashedLine(RenderPipeline pipeline, double x1, double y1, double x2, double y2, double thiccness, double segmentLength, Color color) {
        this.guiRenderState.addGuiElement(new BraidDashedLineElement(
            color,
            thiccness,
            segmentLength,
            pipeline,
            new Matrix3x2f(this.pose()),
            new ScreenRectangle((int) x1, (int) y1, (int) (x2 - x1), (int) (y2 - y1)),
            this.scissorStack.peek()
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
