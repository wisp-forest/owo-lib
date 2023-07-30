package io.wispforest.owo.ui.window;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentComponent;
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

    private int mouseX = -1;
    private int mouseY = -1;

    public OwoWindow(int width, int height, String name, long parentContext) {
        super(width, height, name, parentContext);
        recalculateScale();

        this.adapter = createAdapter();
        build(this.adapter.rootComponent);
        this.adapter.inflateAndMount();

        windowClosed().subscribe(this::close);
        windowResized().subscribe((newWidth, newHeight) -> {
            recalculateScale();
        });
        mouseMoved().subscribe((x, y) -> {
            this.mouseX = (int) (x / scaleFactor);
            this.mouseY = (int) (y / scaleFactor);
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
                        && factor < this.width()
                        && factor < this.height()
                        && this.width() / (factor + 1) >= 320
                        && this.height() / (factor + 1) >= 240
        ) {
            ++factor;
        }

        if (forceUnicodeFont && factor % 2 != 0) {
            ++factor;
        }

        this.scaleFactor = factor;
        this.scaledWidth = (int) Math.ceil((double) this.width() / scaleFactor);
        this.scaledHeight = (int) Math.ceil((double) this.height() / scaleFactor);
    }

    public void render() {
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
        RenderSystem.setProjectionMatrix(matrix4f, VertexSorter.BY_Z);
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.loadIdentity();
        matrixStack.translate(0.0F, 0.0F, -11000.0F);
        RenderSystem.applyModelViewMatrix();
        DiffuseLighting.enableGuiDepthLighting();

        var consumers = client.getBufferBuilders().getEntityVertexConsumers();
        adapter.render(new DrawContext(client, consumers), mouseX, mouseY, client.getTickDelta());
        consumers.draw();

        framebuffer().endWrite();

        present();
    }

    public int scaleFactor() {
        return scaleFactor;
    }

    public int scaledWidth() {
        return scaledWidth;
    }

    public int scaledHeight() {
        return scaledHeight;
    }

    @Override
    public void close() {
        adapter.dispose();
        super.close();
    }
}
