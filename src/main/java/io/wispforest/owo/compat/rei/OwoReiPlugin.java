package io.wispforest.owo.compat.rei;

import dev.architectury.event.CompoundEventResult;
import io.wispforest.owo.braid.core.BraidScreen;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.widgets.recipeviewer.RecipeViewerExclusionZone;
import io.wispforest.owo.braid.widgets.recipeviewer.RecipeViewerStack;
import io.wispforest.owo.braid.widgets.recipeviewer.StackDropArea;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.mixin.itemgroup.CreativeModeInventoryScreenAccessor;
import io.wispforest.owo.ui.base.BaseOwoContainerScreen;
import io.wispforest.owo.util.pond.OwoCreativeInventoryScreenExtensions;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.OverlayDecider;
import me.shedaniel.rei.api.client.registry.screen.OverlayRendererProvider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class OwoReiPlugin implements REIClientPlugin {

    @SuppressWarnings("UnstableApiUsage")
    private static @Nullable OverlayRendererProvider.Sink renderSink = null;

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(CreativeModeInventoryScreen.class, screen -> {
            var group = CreativeModeInventoryScreenAccessor.owo$getSelectedTab();
            if (!(group instanceof OwoItemGroup owoGroup)) return Collections.emptySet();
            if (owoGroup.getButtons().isEmpty()) return Collections.emptySet();

            int x = ((OwoCreativeInventoryScreenExtensions) screen).owo$getRootX();
            int y = ((OwoCreativeInventoryScreenExtensions) screen).owo$getRootY();

            int stackHeight = owoGroup.getButtonStackHeight();
            y -= 13 * (stackHeight - 4);

            final var rectangles = new ArrayList<Rectangle>();
            for (int i = 0; i < owoGroup.getButtons().size(); i++) {
                int xOffset = x + 198 + (i / stackHeight) * 26;
                int yOffset = y + 10 + (i % stackHeight) * 30;
                rectangles.add(new Rectangle(xOffset, yOffset, 24, 24));
            }

            return rectangles;
        });

        zones.register(BaseOwoContainerScreen.class, screen -> {
            return ((BaseOwoContainerScreen<?, ?>) screen).componentsForExclusionAreas()
                .map(rect -> new Rectangle(rect.x(), rect.y(), rect.width(), rect.height()))
                .toList();
        });

        zones.register(BraidScreen.class, screen -> {
            List<Rectangle> rectangles = new ArrayList<>();

            var visitor = new WidgetInstance.Visitor() {
                @Override
                public void visit(WidgetInstance<?> child) {
                    if (child instanceof RecipeViewerExclusionZone.Instance area) {
                        var bounds = area.computeGlobalBounds();

                        rectangles.add(new Rectangle(bounds.minX, bounds.minY, bounds.maxX - bounds.minX, bounds.maxY - bounds.minY));
                    }

                    child.visitChildren(this);
                }
            };

            screen.state.rootInstance().visitChildren(visitor);

            return rectangles;
        });
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerDecider(new OverlayDecider() {
            @Override
            public <R extends Screen> boolean isHandingScreen(Class<R> screen) {
                return BaseOwoContainerScreen.class.isAssignableFrom(screen);
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

        registry.registerFocusedStack((screen, mouse) -> {
            if (!(screen instanceof BraidScreen braid)) return CompoundEventResult.pass();

            var hit = braid.state.hitTest(mouse.x, mouse.y)
                .firstWhere(x -> x.instance() instanceof RecipeViewerStack.Instance);

            if (hit == null) return CompoundEventResult.pass();

            var instance = (RecipeViewerStack.Instance) hit.instance();

            return CompoundEventResult.interruptTrue(ReiStackUtil.toRei(instance.widget().stackProvider.get()));
        });

        registry.registerDraggableStackVisitor(new DraggableStackVisitor<Screen>() {
            @Override
            public <R extends Screen> boolean isHandingScreen(R screen) {
                return screen instanceof BraidScreen;
            }

            @Override
            public Stream<BoundsProvider> getDraggableAcceptingBounds(DraggingContext<Screen> context, DraggableStack stack) {
                if (!(context.getScreen() instanceof BraidScreen braid)) return Stream.empty();

                List<BoundsProvider> allBounds = new ArrayList<>();

                var converted = ReiStackUtil.fromRei(stack.getStack());

                var visitor = new WidgetInstance.Visitor() {
                    @Override
                    public void visit(WidgetInstance<?> child) {
                        if (child instanceof StackDropArea.Instance area && area.widget().stackPredicate.test(converted)) {
                            var bounds = area.computeGlobalBounds();

                            allBounds.add(BoundsProvider.ofRectangle(new Rectangle(bounds.minX, bounds.minY, bounds.maxX - bounds.minX, bounds.maxY - bounds.minY)));
                        }

                        child.visitChildren(this);
                    }
                };

                braid.state.rootInstance().visitChildren(visitor);

                return allBounds.stream();
            }

            @Override
            public DraggedAcceptorResult acceptDraggedStack(DraggingContext<Screen> context, DraggableStack stack) {
                if (!(context.getScreen() instanceof BraidScreen braid)) return DraggedAcceptorResult.PASS;

                var hit = braid.state.hitTest(context.getCurrentPosition().x, context.getCurrentPosition().y)
                    .firstWhere(x -> x.instance() instanceof StackDropArea.Instance);

                if (hit == null) return DraggedAcceptorResult.PASS;

                var instance = (StackDropArea.Instance) hit.instance();

                var converted = ReiStackUtil.fromRei(stack.getStack());

                if (!instance.widget().stackPredicate.test(converted)) return DraggedAcceptorResult.PASS;

                instance.widget().stackAcceptor.accept(converted);

                return DraggedAcceptorResult.ACCEPTED;
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
