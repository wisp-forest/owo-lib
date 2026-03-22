package io.wispforest.owo.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

// TODO: more EMI stuff to fix
public class OwoEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
//        registry.addExclusionArea(CreativeModeInventoryScreen.class, (screen, consumer) -> {
//            var group = CreativeModeInventoryScreenAccessor.owo$getSelectedTab();
//            if (!(group instanceof OwoItemGroup owoGroup)) return;
//            if (owoGroup.getButtons().isEmpty()) return;
//
//            int x = ((OwoCreativeInventoryScreenExtensions) screen).owo$getRootX();
//            int y = ((OwoCreativeInventoryScreenExtensions) screen).owo$getRootY();
//
//            int stackHeight = owoGroup.getButtonStackHeight();
//            y -= 13 * (stackHeight - 4);
//
//            for (int i = 0; i < owoGroup.getButtons().size(); i++) {
//                int xOffset = x + 198 + (i / stackHeight) * 26;
//                int yOffset = y + 10 + (i % stackHeight) * 30;
//                consumer.accept(new Bounds(xOffset, yOffset, 24, 24));
//            }
//        });
//
//        registry.addGenericExclusionArea((screen, consumer) -> {
//            if (!(screen instanceof BaseOwoContainerScreen<?, ?> owoHandledScreen)) return;
//
//            owoHandledScreen.componentsForExclusionAreas()
//                .map(component -> new Bounds(component.x(), component.y(), component.width(), component.height()))
//                .forEach(consumer);
//        });
//
//        registry.addGenericExclusionArea((screen, consumer) -> {
//            if (!(screen instanceof BraidScreen braid)) return;
//
//            var visitor = new WidgetInstance.Visitor() {
//                @Override
//                public void visit(WidgetInstance<?> child) {
//                    if (child instanceof RecipeViewerExclusionZone.Instance area) {
//                        var bounds = area.computeGlobalBounds();
//
//                        consumer.accept(new Bounds((int) bounds.minX, (int) bounds.minY, (int) (bounds.maxX - bounds.minX), (int) (bounds.maxY - bounds.minY)));
//                    }
//
//                    child.visitChildren(this);
//                }
//            };
//
//            braid.state.rootInstance().visitChildren(visitor);
//        });
//
//        registry.addGenericStackProvider((screen, x, y) -> {
//            if (!(screen instanceof BraidScreen braid)) return EmiStackInteraction.EMPTY;
//
//            var hit = braid.state.hitTest(x, y)
//                .firstWhere(i -> i.instance() instanceof RecipeViewerStack.Instance);
//
//            if (hit == null) return EmiStackInteraction.EMPTY;
//
//            var instance = (RecipeViewerStack.Instance) hit.instance();
//
//            return new EmiStackInteraction(EmiStackUtil.toEmi(instance.widget().stackProvider.get()));
//        });
//
//        registry.addGenericDragDropHandler(new EmiDragDropHandler<>() {
//            @Override
//            public boolean dropStack(Screen screen, EmiIngredient stack, int x, int y) {
//                if (!(screen instanceof BraidScreen braid)) return false;
//
//                var hit = braid.state.hitTest(x, y)
//                    .firstWhere(i -> i.instance() instanceof StackDropArea.Instance);
//
//                if (hit == null) return false;
//
//                var instance = (StackDropArea.Instance) hit.instance();
//
//                var converted = EmiStackUtil.fromEmi(stack.getEmiStacks().get(0));
//
//                if (!instance.widget().stackPredicate.test(converted)) return false;
//
//                instance.widget().stackAcceptor.accept(converted);
//
//                return true;
//            }
//
//            @Override
//            public void render(Screen screen, EmiIngredient dragged, GuiGraphics draw, int mouseX, int mouseY, float delta) {
//                if (!(screen instanceof BraidScreen braid)) return;
//
//                List<AABB> allBounds = new ArrayList<>();
//
//                var converted = EmiStackUtil.fromEmi(dragged.getEmiStacks().get(0));
//
//                var visitor = new WidgetInstance.Visitor() {
//                    @Override
//                    public void visit(WidgetInstance<?> child) {
//                        if (child instanceof StackDropArea.Instance area && area.widget().stackPredicate.test(converted)) {
//                            allBounds.add(area.computeGlobalBounds());
//                        }
//
//                        child.visitChildren(this);
//                    }
//                };
//
//                braid.state.rootInstance().visitChildren(visitor);
//
//                for (AABB b : allBounds) {
//                    draw.fill((int) b.minX, (int) b.minY, (int) b.maxX, (int) b.maxY, 0x8822BB33);
//                }
//            }
//        });
    }
}
