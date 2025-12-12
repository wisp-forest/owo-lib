package io.wispforest.owo.compat.emi;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import io.wispforest.owo.braid.core.BraidScreen;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.widgets.recipeviewer.RecipeViewerExclusionZone;
import io.wispforest.owo.braid.widgets.recipeviewer.RecipeViewerStack;
import io.wispforest.owo.braid.widgets.recipeviewer.StackDropArea;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.mixin.itemgroup.CreativeModeInventoryScreenAccessor;
import io.wispforest.owo.ui.base.BaseOwoContainerScreen;
import io.wispforest.owo.util.pond.OwoCreativeInventoryScreenExtensions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class OwoEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addExclusionArea(CreativeModeInventoryScreen.class, (screen, consumer) -> {
            var group = CreativeModeInventoryScreenAccessor.owo$getSelectedTab();
            if (!(group instanceof OwoItemGroup owoGroup)) return;
            if (owoGroup.getButtons().isEmpty()) return;

            int x = ((OwoCreativeInventoryScreenExtensions) screen).owo$getRootX();
            int y = ((OwoCreativeInventoryScreenExtensions) screen).owo$getRootY();

            int stackHeight = owoGroup.getButtonStackHeight();
            y -= 13 * (stackHeight - 4);

            for (int i = 0; i < owoGroup.getButtons().size(); i++) {
                int xOffset = x + 198 + (i / stackHeight) * 26;
                int yOffset = y + 10 + (i % stackHeight) * 30;
                consumer.accept(new Bounds(xOffset, yOffset, 24, 24));
            }
        });

        registry.addGenericExclusionArea((screen, consumer) -> {
            if (!(screen instanceof BaseOwoContainerScreen<?, ?> owoHandledScreen)) return;

            owoHandledScreen.componentsForExclusionAreas()
                .map(component -> new Bounds(component.x(), component.y(), component.width(), component.height()))
                .forEach(consumer);
        });

        registry.addGenericExclusionArea((screen, consumer) -> {
            if (!(screen instanceof BraidScreen braid)) return;

            var visitor = new WidgetInstance.Visitor() {
                @Override
                public void visit(WidgetInstance<?> child) {
                    if (child instanceof RecipeViewerExclusionZone.Instance area) {
                        var bounds = area.computeGlobalBounds();

                        consumer.accept(new Bounds((int) bounds.minX, (int) bounds.minY, (int) (bounds.maxX - bounds.minX), (int) (bounds.maxY - bounds.minY)));
                    }

                    child.visitChildren(this);
                }
            };

            braid.state.rootInstance().visitChildren(visitor);
        });

        registry.addGenericStackProvider((screen, x, y) -> {
            if (!(screen instanceof BraidScreen braid)) return EmiStackInteraction.EMPTY;

            var hit = braid.state.hitTest(x, y)
                .firstWhere(i -> i.instance() instanceof RecipeViewerStack.Instance);

            if (hit == null) return EmiStackInteraction.EMPTY;

            var instance = (RecipeViewerStack.Instance) hit.instance();

            return new EmiStackInteraction(EmiStackUtil.toEmi(instance.widget().stackProvider.get()));
        });

        registry.addGenericDragDropHandler(new EmiDragDropHandler<>() {
            @Override
            public boolean dropStack(Screen screen, EmiIngredient stack, int x, int y) {
                if (!(screen instanceof BraidScreen braid)) return false;

                var hit = braid.state.hitTest(x, y)
                    .firstWhere(i -> i.instance() instanceof StackDropArea.Instance);

                if (hit == null) return false;

                var instance = (StackDropArea.Instance) hit.instance();

                var converted = EmiStackUtil.fromEmi(stack.getEmiStacks().get(0));

                if (!instance.widget().stackPredicate.test(converted)) return false;

                instance.widget().stackAcceptor.accept(converted);

                return true;
            }

            @Override
            public void render(Screen screen, EmiIngredient dragged, GuiGraphics draw, int mouseX, int mouseY, float delta) {
                if (!(screen instanceof BraidScreen braid)) return;

                List<AABB> allBounds = new ArrayList<>();

                var converted = EmiStackUtil.fromEmi(dragged.getEmiStacks().get(0));

                var visitor = new WidgetInstance.Visitor() {
                    @Override
                    public void visit(WidgetInstance<?> child) {
                        if (child instanceof StackDropArea.Instance area && area.widget().stackPredicate.test(converted)) {
                            allBounds.add(area.computeGlobalBounds());
                        }

                        child.visitChildren(this);
                    }
                };

                braid.state.rootInstance().visitChildren(visitor);

                for (AABB b : allBounds) {
                    draw.fill((int) b.minX, (int) b.minY, (int) b.maxX, (int) b.maxY, 0x8822BB33);
                }
            }
        });
    }
}
