package io.wispforest.owo.ui.container;

import I;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MatrixStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import io.wispforest.owo.ui.util.ScissorStack;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;

/**
 * An experimental wrapper component that allows arbitrary rendering
 * effects to be applied to its child. To ensure that all effects apply properly
 * with no surprises, the child is first drawn as normal onto a separate framebuffer
 * which is then drawn back into the primary framebuffer with the effects applied
 * <p>
 * {@link RenderEffect} provides some predefined convenience effects, but the interface
 * is simple to implement for any custom effects that may be desired
 * <p>
 * This wrapper fully supports nesting, in which case multiple framebuffers are
 * maintained in a stack, consecutively drawn to and merged back with the previous buffer
 */
@ApiStatus.Experimental
public class RenderEffectWrapper<C extends Component> extends WrappingParentComponent<C> {

    protected static final List<RenderTarget> FRAMEBUFFERS = new ArrayList<>();
    protected static int drawDepth = 0;

    protected final List<RenderEffectSlot> effects = new ArrayList<>();

    protected RenderEffectWrapper(C child) {
        super(Sizing.content(), Sizing.content(), child);
        this.allowOverflow = true;
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);

        try {
            drawDepth++;

            var window = Minecraft.getInstance().getWindow();
            while (drawDepth > FRAMEBUFFERS.size()) {
                FRAMEBUFFERS.add(new TextureTarget(window.getWidth(), window.getHeight(), true, Minecraft.ON_OSX));
            }

            var previousFramebuffer = GlStateManager.getBoundFramebuffer();
            var framebuffer = FRAMEBUFFERS.get(drawDepth - 1);
            framebuffer.setClearColor(0, 0, 0, 0);
            ScissorStack.drawUnclipped(() -> framebuffer.clear(Minecraft.ON_OSX));
            framebuffer.bindWrite(false);

            this.drawChildren(context, mouseX, mouseY, partialTicks, delta, this.childView);

            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer);

            var iter = this.effects.listIterator();
            while (iter.hasNext()) {
                iter.next().effect.setup(this, context, partialTicks, delta);
            }

            var buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            var matrix = context.matrixStack().peek().model();

            buffer.addVertex(matrix, 0, window.getGuiScaledHeight(), 0).uv(0, 0).color(1f, 1f, 1f, 1f);
            buffer.addVertex(matrix, window.getGuiScaledWidth(), window.getGuiScaledHeight(), 0).uv(1, 0).color(1f, 1f, 1f, 1f);
            buffer.addVertex(matrix, window.getGuiScaledWidth(), 0, 0).uv(1, 1).color(1f, 1f, 1f, 1f);
            buffer.addVertex(matrix, 0, 0, 0).uv(0, 1).color(1f, 1f, 1f, 1f);

            RenderSystem.setShaderTexture(0, framebuffer.getColorTextureId());
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            BufferUploader.drawWithShader(buffer.buildOrThrow());

            while (iter.hasPrevious()) {
                iter.previous().effect.cleanup(this, context, partialTicks, delta);
            }
        } finally {
            drawDepth--;
        }
    }

    /**
     * Add a new rendering effect to this wrapper. Effect setup is executed during
     * drawing in the order that they were added, cleanup is performed opposite
     *
     * @param effect The effect to add
     * @return The new {@link RenderEffectSlot} created to contain the newly added
     * effect. The client may store this slot and, through later calls to
     * {@link RenderEffectSlot#update(RenderEffect)}, replace the effect with
     * a new instance or {@linkplain RenderEffectSlot#remove() remove it} altogether
     */
    public RenderEffectSlot effect(RenderEffect effect) {
        var slot = new RenderEffectSlot(effect);
        this.effects.add(slot);
        return slot;
    }

    /**
     * Remove all rendering effects from this wrapper
     */
    public void clearEffects() {
        this.effects.clear();
    }

    static {
        WindowResizeCallback.EVENT.register((client, window) -> {
            FRAMEBUFFERS.forEach(framebuffer -> {
                framebuffer.resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);
            });
        });
    }

    public class RenderEffectSlot {

        protected RenderEffect effect;

        protected RenderEffectSlot(RenderEffect effect) {
            this.effect = effect;
        }

        /**
         * Replace the effect stored in this slot with
         * the given one
         */
        public void update(RenderEffect newEffect) {
            this.effect = newEffect;
        }

        /**
         * Remove this slot from its containing wrapper. After
         * this method was called, this slot object
         * is no longer valid for use
         */
        public void remove() {
            RenderEffectWrapper.this.effects.remove(this);
        }
    }

    public interface RenderEffect {
        void setup(Component component, GuiGraphics context, float partialTicks, float delta);

        void cleanup(Component component, GuiGraphics context, float partialTicks, float delta);

        /**
         * Create an effect instance which rotates the
         * component around its center point
         *
         * @param angle The angle to rotate by, in degrees
         */
        static RenderEffect rotate(float angle) {
            return rotate(Axis.ZP, angle);
        }

        /**
         * Create an effect instance which rotates the
         * component around its center point on the given axis
         *
         * @param axis  The axis rotate on
         * @param angle The angle to rotate by, in degrees
         */
        static RenderEffect rotate(Axis axis, float angle) {
            return new RenderEffect() {
                @Override
                public void setup(Component component, GuiGraphics context, float partialTicks, float delta) {
                    var size = component.fullSize();
                    var matrices = context.matrixStack();

                    matrices.push();
                    matrices.translate(component.x() + size.width() / 2f, component.y() + size.height() / 2f, 0);
                    matrices.rotate(axis.rotationDegrees(angle));
                    matrices.translate(-(component.x() + size.width() / 2f), -(component.y() + size.height() / 2f), 0);
                }

                @Override
                public void cleanup(Component component, GuiGraphics context, float partialTicks, float delta) {
                    context.matrixStack().pop();
                }
            };
        }

        /**
         * Create an effect instance which filters the component
         * colors with the given color. That is, the current {@link RenderSystem#getShaderColor()}
         * at setup time is multiplied component-wise by the given color and
         * restored to the previous values at cleanup time.
         * <p>
         * If the color's alpha component is != 1, blending is enabled
         *
         * @param color The color to filter with
         */
        static RenderEffect color(Color color) {
            return new RenderEffect() {
                private float[] colors = null;

                @Override
                public void setup(Component component, GuiGraphics context, float partialTicks, float delta) {
                    this.colors = RenderSystem.getShaderColor().clone();
                    RenderSystem.setShaderColor(colors[0] * color.red(), colors[1] * color.green(), colors[2] * color.blue(), colors[3] * color.alpha());

                    if (color.alpha() != 1) {
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                    }
                }

                @Override
                public void cleanup(Component component, GuiGraphics context, float partialTicks, float delta) {
                    RenderSystem.setShaderColor(colors[0], colors[1], colors[2], colors[3]);
                }
            };
        }

        /**
         * Create an effect instance which applies the given transformation
         * matrix before the component is rendered
         *
         * @param transform The transformation matrix to apply
         */
        static RenderEffect transform(Matrix4f transform) {
            return transform(matrices -> {
                matrices.multiply(transform);
            });
        }

        /**
         * Create an effect instance which invokes the given transform
         * function with the matrix stack before the component is rendered
         *
         * @param transform The transform function to apply
         */
        static RenderEffect transform(Consumer<MatrixStack> transform) {
            return new RenderEffect() {
                @Override
                public void setup(Component component, GuiGraphics context, float partialTicks, float delta) {
                    context.matrixStack().push();
                    transform.accept(context.matrixStack());
                }

                @Override
                public void cleanup(Component component, GuiGraphics context, float partialTicks, float delta) {
                    context.matrixStack().pop();
                }
            };
        }
    }

}
