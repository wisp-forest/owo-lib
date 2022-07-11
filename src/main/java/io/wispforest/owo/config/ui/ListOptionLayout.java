package io.wispforest.owo.config.ui;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import io.wispforest.owo.util.NumberReflection;
import io.wispforest.owo.util.ReflectionUtils;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ListOptionLayout<T> extends OptionContainerLayout implements OptionComponent {

    protected final Option<List<T>> backingOption;
    protected final List<T> backingList;
    protected final List<OptionComponent> options = new ArrayList<>();

    protected ListOptionLayout(Option<List<T>> option) {
        super(Text.translatable("text.config." + option.configName() + ".option." + option.key().asString()));

        this.backingOption = option;
        this.backingList = option.value();

        var padding = this.padding.get();
        this.padding(Insets.of(padding.top(), padding.bottom(), padding.left(), padding.right() + 3));

        this.titleLayout.horizontalSizing(Sizing.fill(100));
        this.titleLayout.verticalSizing(Sizing.fixed(30));
        this.titleLayout.verticalAlignment(VerticalAlignment.CENTER);

        final var resetButton = Components.button(Text.literal("⇄"), button -> {
            this.backingList.clear();
            this.backingList.addAll(option.defaultValue());

            this.refreshOptions();
            button.active = false;
        });
        resetButton.positioning(Positioning.relative(100, 50));
        resetButton.active = !this.backingList.equals(this.backingOption.defaultValue());
        this.titleLayout.child(resetButton);

        this.refreshOptions();
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    protected void refreshOptions() {
        this.collapsingChildren.removeAll(this.options);
        this.children.removeAll(this.options);
        this.options.clear();

        var listType = ReflectionUtils.getFirstTypeArgument(this.backingOption.backingField().field());
        for (var element : this.backingList) {
            final var box = new ConfigTextBox();
            box.setText(element.toString());
            box.setCursorToStart();
            box.setDrawsBackground(false);
            box.margins(Insets.left(15));
            box.horizontalSizing(Sizing.fixed(120));

            if (NumberReflection.isNumberType(listType)) {
                box.configureForNumber((Class<? extends Number>) listType);
            } else if (this.backingOption.constraint() != null) {
                box.applyPredicate(this.backingOption.constraint().predicate());
            }

            this.child(box);
            this.options.add(box);
        }

        this.updateLayout();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object parsedValue() {
        for (int i = 0; i < this.options.size(); i++) {
            this.backingList.set(i, (T) this.options.get(i).parsedValue());
        }

        return this.backingList;
    }
}
