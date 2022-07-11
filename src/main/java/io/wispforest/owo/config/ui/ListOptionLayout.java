package io.wispforest.owo.config.ui;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.Expanded;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Layouts;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.util.NumberReflection;
import io.wispforest.owo.util.ReflectionUtils;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.List;

public class ListOptionLayout<T> extends OptionContainerLayout implements OptionComponent {

    protected final Option<List<T>> backingOption;
    protected final List<T> backingList;

    protected final List<Component> optionContainers = new ArrayList<>();
    protected final ButtonWidget resetButton;

    //TODO move most of this to xml
    @SuppressWarnings("unchecked")
    protected ListOptionLayout(Option<List<T>> option) {
        super(
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

        var addLabel = Components.label(Text.literal("Add entry").formatted(Formatting.GRAY));
        addLabel.cursorStyle(CursorStyle.HAND);
        addLabel.mouseEnter().subscribe(() -> addLabel.text(addLabel.text().copy().styled(style -> style.withColor(Formatting.YELLOW))));
        addLabel.mouseLeave().subscribe(() -> addLabel.text(addLabel.text().copy().styled(style -> style.withColor(Formatting.GRAY))));
        addLabel.mouseDown().subscribe((mouseX, mouseY, button) -> {
            this.backingList.add((T) "");
            this.refreshOptions();
            return true;
        });
        this.titleLayout.child(addLabel.margins(Insets.of(5)));

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
        this.collapsingChildren.removeAll(this.optionContainers);
        this.children.removeAll(this.optionContainers);
        this.optionContainers.clear();

        var listType = ReflectionUtils.getTypeArgument(this.backingOption.backingField().field(), 0);
        for (int i = 0; i < this.backingList.size(); i++) {
            var container = Layouts.horizontalFlow(Sizing.content(), Sizing.content());
            container.verticalAlignment(VerticalAlignment.CENTER);
            container.padding(Insets.left(10));

            int idx = i;
            final var label = Components.label(Text.literal("- ").formatted(Formatting.GRAY));
            label.cursorStyle(CursorStyle.HAND);
            label.mouseEnter().subscribe(() -> label.text(Text.literal("x ").formatted(Formatting.GRAY)));
            label.mouseLeave().subscribe(() -> label.text(Text.literal("- ").formatted(Formatting.GRAY)));
            label.mouseDown().subscribe((mouseX, mouseY, button) -> {
                this.backingList.remove(idx);
                this.refreshResetButton();
                this.refreshOptions();
                return true;
            });
            container.child(label);

            final var box = new ConfigTextBox();
            box.setText(this.backingList.get(i).toString());
            box.setCursorToStart();
            box.setDrawsBackground(false);
            box.margins(Insets.vertical(2));
            box.horizontalSizing(Sizing.fill(100));
            box.verticalSizing(Sizing.fixed(8));
            box.setMaxLength(Integer.MAX_VALUE);

            box.setChangedListener(s -> {
                if (!box.isValid()) return;

                this.backingList.set(idx, (T) box.parsedValue());
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
    public boolean isValid() {
        return true;
    }

    @Override
    public Object parsedValue() {
        return this.backingList;
    }
}
