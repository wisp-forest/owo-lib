package io.wispforest.owo.itemgroup.gui;

import com.google.common.collect.MapMaker;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.itemgroup.base.Icon;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.OwoUIPipelines;
import io.wispforest.owo.ui.core.PositionedRectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;

import java.util.HashMap;
import java.util.Map;

public class IconRenderRegistry {

    private static final Map<Class<?>, IconRenderer<?>> TYPE_TO_RENDERER = new HashMap<>();

    public static <T extends Icon> void addRenderer(Class<T> clazz, IconRenderer<T> renderer) {
        if (TYPE_TO_RENDERER.containsKey(clazz)) {
            throw new IllegalStateException("Unable to add renderer for the given icon type [" + clazz + "] due to it already being registered!");
        }

        TYPE_TO_RENDERER.put(clazz, renderer);
    }

    public static <T extends Icon> boolean renderIcon(T icon, DrawContext context, int x, int y, int mouseX, int mouseY, float partialTicks) {
        var renderer = TYPE_TO_RENDERER.get(icon.getClass());

        if (renderer == null) return false;

        ((IconRenderer<T>) renderer).renderIcon(icon, context, x, y,mouseX, mouseY, partialTicks);

        return true;
    }

    public interface IconRenderer<T extends Icon> {
        void renderIcon(T icon, DrawContext context, int x, int y, int mouseX, int mouseY, float partialTicks);
    }

    static {
        addRenderer(Icon.ItemIcon.class, (icon, context, x, y, mouseX, mouseY, partialTicks) -> {
            context.drawItemWithoutEntity(icon.stack(), x, y);
        });

        addRenderer(Icon.TextureIcon.class, (icon, context, x, y, mouseX, mouseY, partialTicks) -> {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, icon.texture(), x, y, icon.u(), icon.v(), 16, 16, icon.textureWidth(), icon.textureHeight());
        });

        Map<Icon.AnimatedTextureIcon, AnimatedTextureState> cachedWidgets = new MapMaker().weakKeys().makeMap();

        addRenderer(Icon.AnimatedTextureIcon.class, (icon, context, x, y, mouseX, mouseY, partialTicks) -> {
            cachedWidgets.computeIfAbsent(icon, AnimatedTextureState::new)
                .renderAndUpdate(context, x, y);
        });
    }

    private static class AnimatedTextureState {
        private final int rowCount;
        private final int columnCount;

        private final Icon.AnimatedTextureIcon icon;

        private double counter = 0;
        private int index = 0;
        private int incrementValue = 1;

        public AnimatedTextureState(Icon.AnimatedTextureIcon icon) {
            this.rowCount = icon.textureHeight() / 16;
            this.columnCount = icon.textureWidth() / 16;

            this.icon = icon;
        }

        void renderAndUpdate(DrawContext initialContext, int x, int y) {
            final var delta = MinecraftClient.getInstance().getRenderTickCounter().getDynamicDeltaTicks();

            var maxFrames = this.columnCount * this.rowCount;

            if (this.icon.loop() || this.index + 1 < maxFrames) {
                this.counter += delta * 50;

                if (this.counter >= this.icon.frameDelay()) {
                    this.counter = 0;

                    var nextIndex = this.index + 1;

                    if (nextIndex >= maxFrames || nextIndex < 0) {
                        if(this.icon.loop()) {
                            if (this.icon.reversible()) {
                                this.incrementValue = -incrementValue;

                                this.index += incrementValue;
                            } else {
                                this.index = 0;
                            }
                        }
                    } else {
                        this.index += incrementValue;
                    }
                }
            }

            //--

            final var context = OwoUIDrawContext.of(initialContext);

            int xPos = (this.index % this.columnCount) * 16;
            int yPos = (this.index / this.rowCount) * 16;

            context.drawTexture(RenderPipelines.GUI_TEXTURED,
                this.icon.texture(),
                x, y,
                xPos, yPos,
                16, 16,
                icon.textureWidth(), icon.textureHeight()
            );
        }
    }
}
