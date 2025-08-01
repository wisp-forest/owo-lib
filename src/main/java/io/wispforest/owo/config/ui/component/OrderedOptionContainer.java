package io.wispforest.owo.config.ui.component;

import io.wispforest.owo.config.options.OptionControlSpec;
import io.wispforest.owo.config.ui.OptionComponentFactory;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ApiStatus.Internal
public abstract class OrderedOptionContainer<C, T> extends CollapsibleContainer implements OptionValueProvider {

    protected final boolean isDetached;

    protected final UIModel uiModel;

    protected final OptionControlSpec<C> backingOption;

    protected final List<T> backingList;

    protected final List<OptionValueProvider> backingProviders;

    protected final ButtonWidget resetButton;
    
    public OrderedOptionContainer(UIModel uiModel, OptionControlSpec<C> option, boolean expanded, boolean isDetached) {
        super(
                Sizing.fill(100), Sizing.content(),
                Text.translatable(option.translationKey()),
                expanded
        );

        this.isDetached = isDetached;

        this.uiModel = uiModel;

        this.backingOption = option;
        this.backingList = convertToList(option.value());
        this.backingProviders = new ArrayList<>();

        this.padding(this.padding.get().add(0, 5, 0, 0));

        this.titleLayout
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(5))
                .horizontalSizing(Sizing.fill(100))
                .verticalSizing(Sizing.fixed(30));

        OptionComponentFactory.addEasyCopyLabel(this.titleLayout, option.translationKey());

        if (!this.isDetached) {
            var addLabel = uiModel.expandTemplate(LabelComponent.class, "collection-add-label", Map.of()).<LabelComponent>configure(label -> {
                label.mouseDown().subscribe((mouseX, mouseY, button) -> {
                    UISounds.playInteractionSound();

                    var index = this.backingList.size();
                    var newEntry = createDefaultValue();

                    this.backingList.add(newEntry);

                    if (!this.expanded) this.toggleExpansion();

                    var provider = createProviderComponent(this.backingList.get(index));

                    this.collapsibleChildren.add(createEntryContainer(index).child(provider));
                    this.backingProviders.add(provider);

                    this.refreshOptions();

                    var lastEntry = (ParentComponent) this.collapsibleChildren.getLast();
                    this.focusHandler().focus(
                            lastEntry.children().get(lastEntry.children().size() - 1),
                            FocusSource.MOUSE_CLICK
                    );

                    return true;
                });
            });

            this.titleLayout.child(addLabel);
        }

        this.resetButton = uiModel.expandTemplate(ButtonComponent.class, "control-reset-button", Map.of())
                .<ButtonComponent>configure(buttonWidget -> {
                    buttonWidget.onPress(btn -> {
                        this.backingList.clear();
                        this.backingList.addAll(convertToList(option.defaultValue()));

                        this.refreshOptions();
                        btn.active = false;
                    }).positioning(Positioning.relative(100, 50));
                });

        this.titleLayout.child(resetButton);

        this.refreshResetButton();

        this.refreshOptions();

        this.titleLayout.child(new SearchAnchorComponent(
                this.titleLayout,
                option.key(),
                () -> I18n.translate(option.translationKey()),
                () -> this.backingList.stream().map(Objects::toString).collect(Collectors.joining())
        ));
    }

    protected boolean tickAtTop() {
        return true;
    }

    protected abstract <P extends OptionValueProvider & Component> P createProviderComponent(T listEntry);

    protected abstract List<T> convertToList(C data);

    protected abstract C convertToCollection(List<T> backingList);

    protected abstract T createDefaultValue();

    protected FlowLayout createEntryContainer(int optionIndex) {
        var tickAtTop = tickAtTop();

        var container = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        container.verticalAlignment(tickAtTop ? VerticalAlignment.TOP : VerticalAlignment.CENTER);

        if (tickAtTop && optionIndex + 1 < this.backingList.size()) container.padding(Insets.bottom(10));

        final var label = this.uiModel.expandTemplate(LabelComponent.class, "collection-entry-tick", Map.of());

        if (this.isDetached) {
            // Remove hoverablity implementation indicators
            label.hoverText(null);
            label.cursorStyle(CursorStyle.NONE);
        } else {
            label.mouseDown().subscribe((mouseX, mouseY, button) -> {
                this.backingList.remove(optionIndex);
                this.collapsibleChildren.remove(optionIndex);
                this.backingProviders.remove(optionIndex);
                this.refreshResetButton();
                this.refreshOptions();
                UISounds.playInteractionSound();

                return true;
            });
        }

        container.child(
                Containers.verticalFlow(Sizing.fixed(19), Sizing.content())
                        .child(label)
                        .margins(tickAtTop ? Insets.top(12) : Insets.none())
        );

        return container;
    }

    protected void refreshOptions() {
        this.collapsibleChildren.clear();

        if (this.backingProviders.isEmpty()) {
            for (int i = 0; i < this.backingList.size(); i++) {
                var provider = createProviderComponent(this.backingList.get(i));

                this.collapsibleChildren.add(createEntryContainer(i).child(provider));
                this.backingProviders.add(provider);
            }
        } else {
            for (int i = 0; i < this.backingProviders.size(); i++) {
                this.collapsibleChildren.add(createEntryContainer(i).child((Component) this.backingProviders.get(i)));
            }
        }

        refreshLayout();
    }

    protected void refreshLayout() {
        this.contentLayout.<FlowLayout>configure(layout -> {
            layout.clearChildren();
            if (this.expanded) layout.children(this.collapsibleChildren);
        });

        this.refreshResetButton();
    }

    protected void refreshResetButton() {
        this.resetButton.active = !this.isDetached && !this.backingList.equals(this.backingOption.defaultValue());
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
        for (int i = 0; i < this.backingProviders.size(); i++) {
            var optionProvider = this.backingProviders.get(i);

            if (optionProvider.isValid()) this.backingList.set(i, (T) optionProvider.parsedValue());
        }
        return this.convertToCollection(this.backingList);
    }
}
