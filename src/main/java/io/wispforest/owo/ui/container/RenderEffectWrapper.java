package io.wispforest.owo.ui.container;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.ui.util.ScissorStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;

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

    protected static final List<Framebuffer> FRAMEBUFFERS = new ArrayList<>();
    protected static int drawDepth = 0;

    protected final List<RenderEffectSlot> effects = new ArrayList<>();

    protected RenderEffectWrapper(C child) {
        super(Sizing.content(), Sizing.content(), child);
        this.allowOverflow = true;
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);

        try {
            drawDepth++;

            var window = MinecraftClient.getInstance().getWindow();
            while (drawDepth > FRAMEBUFFERS.size()) {
                FRAMEBUFFERS.add(new SimpleFramebuffer(window.getFramebufferWidth(), window.getFramebufferHeight(), true, MinecraftClient.IS_SYSTEM_MAC));
            }

            var previousFramebuffer = GlStateManager.getBoundFramebuffer();
            var framebuffer = FRAMEBUFFERS.get(drawDepth - 1);
            framebuffer.setClearColor(0, 0, 0, 0);
            ScissorStack.drawUnclipped(() -> framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC));
            framebuffer.beginWrite(false);

            this.drawChildren(matrices, mouseX, mouseY, partialTicks, delta, this.childView);

            GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer);

            var iter = this.effects.listIterator();
            while (iter.hasNext()) {
                iter.next().effect.setup(this, matrices, partialTicks, delta);
            }

            RenderSystem.setShaderTexture(0, framebuffer.getColorAttachment());
            Drawer.drawTexture(
                    matrices,
                    0, 0,
                    window.getScaledWidth(), window.getScaledHeight(),
                    0, framebuffer.textureHeight,
                    framebuffer.textureWidth, -framebuffer.textureHeight,
                    framebuffer.textureWidth, framebuffer.textureHeight
            );

            while (iter.hasPrevious()) {
                iter.previous().effect.cleanup(this, matrices, partialTicks, delta);
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
                framebuffer.resize(window.getFramebufferWidth(), window.getFramebufferHeight(), MinecraftClient.IS_SYSTEM_MAC);
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
        void setup(Component component, MatrixStack matrices, float partialTicks, float delta);

        void cleanup(Component component, MatrixStack matrices, float partialTicks, float delta);

        /**
         * Create an effect instance which rotates the
         * component around its center point
         *
         * @param angle The angle to rotate by, in degrees
         */
        static RenderEffect rotate(float angle) {
            return rotate(RotationAxis.POSITIVE_Z, angle);
        }

        /**
         * Create an effect instance which rotates the
         * component around its center point on the given axis
         *
         * @param axis  The axis rotate on
         * @param angle The angle to rotate by, in degrees
         */
        static RenderEffect rotate(RotationAxis axis, float angle) {
            return new RenderEffect() {
                @Override
                public void setup(Component component, MatrixStack matrices, float partialTicks, float delta) {
                    var size = component.fullSize();

                    matrices.push();
                    matrices.translate(component.x() + size.width() / 2f, component.y() + size.height() / 2f, 0);
                    matrices.multiply(axis.rotationDegrees(angle));
                    matrices.translate(-(component.x() + size.width() / 2f), -(component.y() + size.height() / 2f), 0);
                }

                @Override
                public void cleanup(Component component, MatrixStack matrices, float partialTicks, float delta) {
                    matrices.pop();
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
                public void setup(Component component, MatrixStack matrices, float partialTicks, float delta) {
                    this.colors = RenderSystem.getShaderColor().clone();
                    RenderSystem.setShaderColor(colors[0] * color.red(), colors[1] * color.green(), colors[2] * color.blue(), colors[3] * color.alpha());

                    if (color.alpha() != 1) {
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                    }
                }

                @Override
                public void cleanup(Component component, MatrixStack matrices, float partialTicks, float delta) {
                    RenderSystem.setShaderColor(colors[0], colors[1], colors[2], colors[3]);
                }
            };
        }
    }

}
