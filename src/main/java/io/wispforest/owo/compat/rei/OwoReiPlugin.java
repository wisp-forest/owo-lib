package io.wispforest.owo.compat.rei;

import io.wispforest.owo.Owo;
import io.wispforest.owo.itemgroup.base.OwoItemGroupState;
import io.wispforest.owo.itemgroup.util.DisplayContextUtils;
import io.wispforest.owo.itemgroup.gui.OwoItemGroupRendererHandler;
import io.wispforest.owo.mixin.ui.layers.HandledScreenAccessor;
import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;
import me.shedaniel.rei.api.client.registry.screen.OverlayRendererProvider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.SequencedCollection;
import java.util.function.Predicate;

public class OwoReiPlugin implements REIClientPlugin {

    @SuppressWarnings("UnstableApiUsage")
    private static @Nullable OverlayRendererProvider.Sink renderSink = null;

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(CreativeInventoryScreen.class, screen -> {
            int x = ((HandledScreenAccessor) screen).owo$getRootX();
            int y = ((HandledScreenAccessor) screen).owo$getRootY();

            return OwoItemGroupRendererHandler.getSelectedRenderer()
                .getExclusionZones(x, y, rect -> new Rectangle(rect.x(), rect.y(), rect.width(), rect.height()))
                .toList();
        });

        zones.register(BaseOwoHandledScreen.class, screen -> {
            return ((BaseOwoHandledScreen<?, ?>) screen)
                .componentsForExclusionAreas(rect -> new Rectangle(rect.x(), rect.y(), rect.width(), rect.height()))
                .toList();
        });
    }

    @Override
    public void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
        for (var group : ItemGroups.getGroups()) {
            var state = OwoItemGroupState.get(group);

            if (state == null) continue;

            var entries = state.gatherGlobalCondensedEntries(DisplayContextUtils.createClientContext());

            entries.forEach((id, condensedEntry) -> {
                registry.group(condensedEntry.id(), Text.of(condensedEntry.getTranslationKey()), new Predicate<>() {
                    private SequencedCollection<ItemStack> childrenEntries = null;

                    @Override
                    public boolean test(EntryStack<?> entryStack) {
                        if (childrenEntries == null) {
                            childrenEntries = condensedEntry.childrenEntries().get();

                            if (childrenEntries.isEmpty() && Owo.DEBUG) {
                                Owo.LOGGER.warn("A Condensed Entry loaded into REI was found to be empty? Ignore if intentional.");
                            }
                        }

                        if (entryStack.getValue() instanceof ItemStack stack) {
                            for (var childrenEntry : childrenEntries) {
                                // TODO: MAYBE FINE?
                                if (ItemStack.areItemsEqual(childrenEntry, stack)) return true;
                            }
                        }

                        return false;
                    }
                });
            });
        }
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerDecider(new OverlayDecider() {
            @Override
            public <R extends Screen> boolean isHandingScreen(Class<R> screen) {
                return BaseOwoHandledScreen.class.isAssignableFrom(screen);
            }

            @Override
            @SuppressWarnings("UnstableApiUsage")
            public OverlayRendererProvider getRendererProvider() {
                return new OverlayRendererProvider() {
                    @Override
                    public void onApplied(Sink sink) {
                        renderSink = sink;
                    }

                    @Override
                    public void onRemoved() {
                        renderSink = null;
                    }
                };
            }
        });
    }

//    static {
//        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
//            if (!(screen instanceof BaseOwoHandledScreenAccessor accessor)) return;
//
//            ScreenEvents.beforeRender(screen).register(($, context, mouseX, mouseY, tickDelta) -> {
//                var root = accessor.owo$getUIAdapter().rootComponent;
//
//                CallbackSurface surface;
//                if (root.surface() instanceof CallbackSurface wrapped) {
//                    surface = wrapped;
//                } else {
//                    surface = new CallbackSurface(root.surface());
//                    root.surface(surface);
//                }
//
//                surface.callback = () -> {
//                    if (renderSink == null) return;
//                    renderOverlay($, () -> renderSink.render(context, mouseX, mouseY, tickDelta));
//                };
//            });
//
//            ScreenEvents.afterRender(screen).register(($, matrices, mouseX, mouseY, tickDelta) -> {
//                if (renderSink == null) return;
//                renderOverlay($, () -> renderSink.lateRender(matrices, mouseX, mouseY, tickDelta));
//            });
//        });
//    }
//
//    private static void renderOverlay(Screen screen, Runnable renderFunction) {
//        if (REIRuntime.getInstance().getSearchTextField().getText().equals("froge")) {
//            var modelView = RenderSystem.getModelViewStack();
//
//            final var time = System.currentTimeMillis();
//            float scale = .75f + (float) (Math.sin(time / 500d) * .5f);
//            modelView.pushMatrix();
//            modelView.translate(screen.width / 2f - scale / 2f * screen.width, screen.height / 2f - scale / 2f * screen.height, 0);
//            modelView.scale(scale, scale, 1f);
//            modelView.translate((float) (Math.sin(time / 1000d) * .75f) * screen.width, (float) (Math.sin(time / 500d) * .75f) * screen.height, 0);
//
//            modelView.translate(screen.width / 2f, screen.height / 2f, 0);
//            modelView.rotate(RotationAxis.POSITIVE_Z.rotationDegrees((float) (time / 25d % 360d)));
//            modelView.translate(screen.width / -2f, screen.height / -2f, 0);
//
//            for (int i = 0; i < 20; i++) {
//                modelView.pushMatrix();
//                modelView.translate(screen.width / 2f, screen.height / 2f, 0);
//                modelView.rotate(RotationAxis.POSITIVE_Z.rotationDegrees(i * 18));
//                modelView.translate(screen.width / -2f, screen.height / -2f, 0);
//
//                ScissorStack.pushDirect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
//                renderFunction.run();
//                GlStateManager._enableScissorTest();
//                ScissorStack.pop();
//                modelView.popMatrix();
//            }
//
//            modelView.popMatrix();
//        } else {
//            ScissorStack.pushDirect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
//            renderFunction.run();
//            GlStateManager._enableScissorTest();
//            ScissorStack.pop();
//        }
//    }
//
//    private static class CallbackSurface implements Surface {
//        public final Surface inner;
//        public @NotNull Runnable callback = () -> {};
//
//        private CallbackSurface(Surface inner) {
//            this.inner = inner;
//        }
//
//        @Override
//        public void draw(OwoUIDrawContext context, ParentComponent component) {
//            this.inner.draw(context, component);
//            this.callback.run();
//        }
//    }
}
