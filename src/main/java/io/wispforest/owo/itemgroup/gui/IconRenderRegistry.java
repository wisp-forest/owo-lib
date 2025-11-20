package io.wispforest.owo.itemgroup.gui;

import com.google.common.collect.MapMaker;
import io.wispforest.owo.itemgroup.base.Icon;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

///
/// Handles rendering of all [Icon] objects by allowing users to register
/// a given [IconRenderer] for such based on its [Identifier]
///
public class IconRenderRegistry {

    private static final Map<Identifier, IconRenderer<?>> TYPE_TO_RENDERER = new HashMap<>();

    //--

    public static <T extends Icon> void addRenderer(Identifier id, IconRenderer<T> renderer) {
        if (TYPE_TO_RENDERER.containsKey(id)) {
            throw new IllegalStateException("Unable to add renderer for the given icon type [" + id + "] due to it already being registered!");
        }

        TYPE_TO_RENDERER.put(id, renderer);
    }

    //--

    public static <T extends Icon> boolean renderIcon(T icon, DrawContext context, int x, int y, int mouseX, int mouseY, float partialTicks) {
        var renderer = TYPE_TO_RENDERER.get(icon.getTypeId());

        if (renderer == null) return false;

        ((IconRenderer<T>) renderer).renderIcon(icon, context, x, y,mouseX, mouseY, partialTicks);

        return true;
    }

    static {
        IconRenderRegistry.<Icon.ItemIcon>addRenderer(Icon.ItemIcon.ID,
            (icon, context, x, y, mouseX, mouseY, partialTicks) -> {
                context.drawItemWithoutEntity(icon.stack(), x, y);
            });

        IconRenderRegistry.<Icon.TextureIcon>addRenderer(Icon.TextureIcon.ID,
            (icon, context, x, y, mouseX, mouseY, partialTicks) -> {
                context.drawTexture(RenderPipelines.GUI_TEXTURED, icon.texture(), x, y, icon.u(), icon.v(), 16, 16, icon.textureWidth(), icon.textureHeight());
            });

        IconRenderRegistry.addRenderer(Icon.AnimatedTextureIcon.ID,
            new IconRenderer<Icon.AnimatedTextureIcon>() {
                private final Map<Icon.AnimatedTextureIcon, AnimatedTextureState> cachedWidgets = new MapMaker().weakKeys().makeMap();

                @Override
                public void renderIcon(Icon.AnimatedTextureIcon icon, DrawContext context, int x, int y, int mouseX, int mouseY, float partialTicks) {
                    cachedWidgets.computeIfAbsent(icon, AnimatedTextureState::new)
                        .renderAndUpdate(context, x, y);
                }
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
