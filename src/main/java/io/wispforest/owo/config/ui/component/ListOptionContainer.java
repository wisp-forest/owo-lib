package io.wispforest.owo.config.ui.component;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.Expanded;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.owo.util.NumberReflection;
import io.wispforest.owo.util.ReflectionUtils;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public class ListOptionContainer<T> extends CollapsibleContainer implements OptionComponent {

    protected final Option<List<T>> backingOption;
    protected final List<T> backingList;

    protected final List<Component> optionContainers = new ArrayList<>();
    protected final ButtonWidget resetButton;

    @SuppressWarnings("unchecked")
    public ListOptionContainer(Option<List<T>> option) {
        super(
                Sizing.fill(100), Sizing.content(),
                Text.translatable("text.config." + option.configName() + ".option." + option.key().asString()),
                option.backingField().field().isAnnotationPresent(Expanded.class)
        );

        this.backingOption = option;
        this.backingList = new ArrayList<>(option.value());

        this.padding(this.padding.get().add(0, 5, 0, 0));
        this.titleLayout.padding(Insets.top(5));

        this.titleLayout.horizontalSizing(Sizing.fill(100));
        this.titleLayout.verticalSizing(Sizing.fixed(30));
        this.titleLayout.verticalAlignment(VerticalAlignment.CENTER);

        var addButton = Components.label(Text.literal("Add entry").formatted(Formatting.GRAY));
        addButton.cursorStyle(CursorStyle.HAND);
        addButton.mouseEnter().subscribe(() -> addButton.text(addButton.text().copy().styled(style -> style.withColor(Formatting.YELLOW))));
        addButton.mouseLeave().subscribe(() -> addButton.text(addButton.text().copy().styled(style -> style.withColor(Formatting.GRAY))));
        addButton.mouseDown().subscribe((mouseX, mouseY, button) -> {
            this.backingList.add((T) "");
            this.refreshOptions();
            UISounds.playInteractionSound();

            return true;
        });
        this.titleLayout.child(addButton.margins(Insets.of(5)));

        this.resetButton = Components.button(Text.literal("⇄"), button -> {
            this.backingList.clear();
            this.backingList.addAll(option.defaultValue());

            this.refreshOptions();
            button.active = false;
        });
        this.resetButton.margins(Insets.right(10));
        this.resetButton.positioning(Positioning.relative(100, 50));
        this.refreshResetButton();
        this.titleLayout.child(resetButton);

        this.refreshOptions();
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    protected void refreshOptions() {
        this.collapsibleChildren.removeAll(this.optionContainers);
        this.children.removeAll(this.optionContainers);
        this.optionContainers.clear();

        var listType = ReflectionUtils.getTypeArgument(this.backingOption.backingField().field().getGenericType(), 0);
        for (int i = 0; i < this.backingList.size(); i++) {
            var container = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
            container.verticalAlignment(VerticalAlignment.CENTER);

            int optionIndex = i;
            final var label = Components.label(Text.literal("- ").formatted(Formatting.GRAY));
            label.margins(Insets.left(10));
            label.cursorStyle(CursorStyle.HAND);
            label.mouseEnter().subscribe(() -> label.text(Text.literal("x ").formatted(Formatting.GRAY)));
            label.mouseLeave().subscribe(() -> label.text(Text.literal("- ").formatted(Formatting.GRAY)));
            label.mouseDown().subscribe((mouseX, mouseY, button) -> {
                this.backingList.remove(optionIndex);
                this.refreshResetButton();
                this.refreshOptions();
                UISounds.playInteractionSound();

                return true;
            });
            container.child(label);

            final var box = new ConfigTextBox();
            box.setText(this.backingList.get(i).toString());
            box.setCursorToStart();
            box.setDrawsBackground(false);
            box.margins(Insets.vertical(2));
            box.horizontalSizing(Sizing.fill(95));
            box.verticalSizing(Sizing.fixed(8));

            box.setChangedListener(s -> {
                if (!box.isValid()) return;

                this.backingList.set(optionIndex, (T) box.parsedValue());
                this.refreshResetButton();
            });

            if (NumberReflection.isNumberType(listType)) {
                box.configureForNumber((Class<? extends Number>) listType);
            } else if (this.backingOption.constraint() != null) {
                box.applyPredicate(this.backingOption.constraint().predicate());
            }

            container.child(box);

            this.child(container);
            this.optionContainers.add(container);
        }

        this.updateLayout();
        this.refreshResetButton();
    }

    protected void refreshResetButton() {
        this.resetButton.active = !this.backingList.equals(this.backingOption.defaultValue());
    }

    @Override
    public boolean shouldDrawTooltip(double mouseX, double mouseY) {
        return ((mouseY - this.y) <= this.titleLayout.height()) && super.shouldDrawTooltip(mouseX, mouseY);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Object parsedValue() {
        return this.backingList;
    }
}
