package io.wispforest.uwu.rei;

import io.wispforest.owo.Owo;
import io.wispforest.owo.compat.rei.ReiUIAdapter;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import io.wispforest.uwu.items.UwuItems;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UiCategory implements DisplayCategory<Display> {

    public static CategoryIdentifier<UiDisplay> ID = CategoryIdentifier.of(Owo.id("ui"));

    @Override
    public List<Widget> setupDisplay(Display display, Rectangle bounds) {
        var adapter = new ReiUIAdapter<>(bounds, UIContainers::verticalFlow);
        var root = adapter.rootComponent();

        root.horizontalAlignment(HorizontalAlignment.CENTER)
                .surface(Surface.DARK_PANEL)
                .padding(Insets.of(8));

        var inner = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
        inner.horizontalAlignment(HorizontalAlignment.CENTER).surface(Surface.flat(0xFF00FFAF));

        inner.child(UIComponents.label(Component.nullToEmpty("A demonstration\ninside REI"))
                .color(Color.BLACK)
                .positioning(Positioning.absolute(3, 3))
        );

        var animation = inner.horizontalSizing().animate(250, Easing.QUADRATIC, Sizing.fill(65));
        inner.child(UIComponents.button(Component.nullToEmpty("shrink"), (ButtonComponent button) -> animation.forwards())
                .margins(Insets.vertical(25))
                .horizontalSizing(Sizing.fixed(60)));
        inner.child(UIComponents.button(Component.nullToEmpty("grow"), (ButtonComponent button) -> animation.backwards())
                .margins(Insets.vertical(25))
                .horizontalSizing(Sizing.fixed(60)));

        inner.child(adapter.wrap(Widgets.createSlot(new Point(0, 0)).entry(EntryStacks.of(Items.ECHO_SHARD))));

        root.child(UIContainers.verticalScroll(Sizing.content(), Sizing.fill(100), inner));

        adapter.prepare();
        return List.of(adapter);
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Items.ECHO_SHARD);
    }

    @Override
    public Component getTitle() {
        return Component.nullToEmpty("yes its gui very epic");
    }

    @Override
    public CategoryIdentifier<? extends Display> getCategoryIdentifier() {
        return ID;
    }

    public static class UiDisplay implements Display {

        @Override
        public List<EntryIngredient> getInputEntries() {
            return List.of(EntryIngredients.of(UwuItems.SCREEN_SHARD));
        }

        @Override
        public List<EntryIngredient> getOutputEntries() {
            return Collections.emptyList();
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return ID;
        }

        @Override
        public Optional<Identifier> getDisplayLocation() {
            return Optional.empty();
        }

        @Override
        public @Nullable DisplaySerializer<? extends Display> getSerializer() {
            return null;
        }
    }
}
