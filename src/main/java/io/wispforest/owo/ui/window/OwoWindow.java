package io.wispforest.owo.ui.window;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.util.GlDebugUtils;
import io.wispforest.owo.ui.util.OwoGlUtil;
import io.wispforest.owo.ui.window.context.CurrentWindowContext;
import io.wispforest.owo.util.InfallibleCloseable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL32;

public abstract class OwoWindow<R extends ParentComponent> extends FramebufferWindow {
    private int scaleFactor;
    private int scaledWidth;
    private int scaledHeight;
    private final OwoUIAdapter<R> adapter;
    private final InfallibleCloseable registration;

    private int mouseX = -1;
    private int mouseY = -1;

    public OwoWindow(int width, int height, String name, long parentContext) {
        super(width, height, name, parentContext);
        recalculateScale();

        try (var ignored = CurrentWindowContext.setCurrent(this)) {
            this.adapter = createAdapter();
            build(this.adapter.rootComponent);
            this.adapter.inflateAndMount();
        }

        this.registration = OpenWindows.add(this);

        windowClosed().subscribe(this::close);
        framebufferResized().subscribe((newWidth, newHeight) -> {
            recalculateScale();
            adapter.moveAndResize(0, 0, scaledWidth(), scaledHeight());
        });
        mouseMoved().subscribe((x, y) -> {
            this.mouseX = (int) (x / scaleFactor);
            this.mouseY = (int) (y / scaleFactor);
        });
        mouseButton().subscribe((button, released) -> {
            if (released) {
                adapter.mouseReleased(mouseX, mouseY, button);
            } else {
                adapter.mouseClicked(mouseX, mouseY, button);
            }
        });
        mouseScrolled().subscribe((xOffset, yOffset) -> {
            double yAmount = (client.options.getDiscreteMouseScroll().getValue() ? Math.signum(yOffset) : yOffset)
                * client.options.getMouseWheelSensitivity().getValue();
            double xAmount = (client.options.getDiscreteMouseScroll().getValue() ? Math.signum(xOffset) : xOffset)
                * client.options.getMouseWheelSensitivity().getValue();
            adapter.mouseScrolled(mouseX, mouseY, xAmount, yAmount);
        });
        keyPressed().subscribe((keyCode, scanCode, modifiers, released) -> {
            if (released) return;

            adapter.keyPressed(keyCode, scanCode, modifiers);
        });
        charTyped().subscribe((chr, modifiers) -> {
            return adapter.charTyped(chr, modifiers);
        });
    }

    protected abstract OwoUIAdapter<R> createAdapter();

    protected abstract void build(R rootComponent);

    public void recalculateScale() {
        int guiScale = MinecraftClient.getInstance().options.getGuiScale().getValue();
        boolean forceUnicodeFont = MinecraftClient.getInstance().options.getForceUnicodeFont().getValue();

        int factor = 1;

        while (
                factor != guiScale
                        && factor < this.framebufferWidth()
                        && factor < this.framebufferHeight()
                        && this.framebufferWidth() / (factor + 1) >= 320
                        && this.framebufferHeight() / (factor + 1) >= 240
        ) {
            ++factor;
        }

        if (forceUnicodeFont && factor % 2 != 0) {
            ++factor;
        }

        this.scaleFactor = factor;
        this.scaledWidth = (int) Math.ceil((double) this.framebufferWidth() / scaleFactor);
        this.scaledHeight = (int) Math.ceil((double) this.framebufferHeight() / scaleFactor);
    }

    public void render() {
        if (closed()) return;

        try (var ignored = CurrentWindowContext.setCurrent(this);
             var ignored1 = GlDebugUtils.pushGroup("Rendering " + this)) {
            framebuffer().beginWrite(true);

            RenderSystem.clearColor(0, 0, 0, 1);
            RenderSystem.clear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);

            Matrix4f matrix4f = new Matrix4f()
                    .setOrtho(
                            0.0F,
                            scaledWidth(),
                            scaledHeight(),
                            0.0F,
                            1000.0F,
                            21000.0F
                    );

            try (var ignored2 = OwoGlUtil.setProjectionMatrix(matrix4f, VertexSorter.BY_Z)) {
                MatrixStack matrixStack = RenderSystem.getModelViewStack();
                matrixStack.push();
                matrixStack.loadIdentity();
                matrixStack.translate(0.0F, 0.0F, -11000.0F);
                RenderSystem.applyModelViewMatrix();
                DiffuseLighting.enableGuiDepthLighting();

                var consumers = client.getBufferBuilders().getEntityVertexConsumers();
                adapter.render(new DrawContext(client, consumers), mouseX, mouseY, client.getTickDelta());
                consumers.draw();

                RenderSystem.getModelViewStack().pop();
                RenderSystem.applyModelViewMatrix();
            }

            framebuffer().endWrite();
        }

        present();
    }

    @Override
    public double scaleFactor() {
        return scaleFactor;
    }

    @Override
    public int scaledWidth() {
        return scaledWidth;
    }

    @Override
    public int scaledHeight() {
        return scaledHeight;
    }

    @Override
    public void close() {
        adapter.dispose();
        registration.close();
        super.close();
    }
}
