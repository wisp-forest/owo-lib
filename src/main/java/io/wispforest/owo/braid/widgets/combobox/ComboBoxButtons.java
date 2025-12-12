package io.wispforest.owo.braid.widgets.combobox;

import io.wispforest.owo.braid.core.*;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.button.Clickable;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.scroll.FlatScrollbar;
import io.wispforest.owo.braid.widgets.scroll.Scrollable;
import io.wispforest.owo.braid.widgets.scroll.ScrollableWithBars;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Function;

class ComboBoxButtons<T> extends StatelessWidget {

    public final ListenableValue<ComboBoxButtonsState<T>> state;
    public final double width;
    public final Function<@Nullable T, Component> optionToName;
    public final ComboBox.SelectCallback<T> onSelect;

    public ComboBoxButtons(ListenableValue<ComboBoxButtonsState<T>> state, double width, Function<@Nullable T, Component> optionToName, ComboBox.SelectCallback<T> onSelect) {
        this.state = state;
        this.width = width;
        this.optionToName = optionToName;
        this.onSelect = onSelect;
    }

    @Override
    public Widget build(BuildContext context) {
        return new Sized(
            this.width,
            null,
            new Box(
                Color.WHITE,
                true,
                new Padding(
                    Insets.all(1),
                    new Blur(
                        5,
                        10,
                        false,
                        new Box(
                            Color.BLACK.withA(.6),
                            new ListenableBuilder(
                                this.state,
                                (listenableContext) -> {
                                    var buttons = new ArrayList<Widget>();
                                    var state = this.state.value();

                                    for (var idx = 0; idx < state.options().size(); idx++) {
                                        var option = state.options().get(idx);

                                        buttons.add(new HighlightableButton<>(
                                            this.onSelect,
                                            option,
                                            idx == state.highlightedOptionIdx().orElse(-1),
                                            this.optionToName
                                        ));
                                    }

                                    return new Constrain(
                                        Constraints.ofMaxHeight(13 * 8),
                                        new ScrollableWithBars(
                                            null,
                                            null,
                                            null,
                                            4,
                                            (layoutAxis, scrollController) -> new FlatScrollbar(layoutAxis, scrollController, Color.WHITE, Color.WHITE),
                                            new Column(buttons)
                                        )
                                    );
                                }
                            )
                        )
                    )
                )
            )
        );
    }

    private static class HighlightableButton<T> extends StatefulWidget {

        public final ComboBox.SelectCallback<T> onSelect;
        public final T option;
        public final boolean highlighted;
        public final Function<@Nullable T, Component> optionToName;

        public HighlightableButton(ComboBox.SelectCallback<T> onSelect, T option, boolean highlighted, Function<@Nullable T, Component> optionToName) {
            this.onSelect = onSelect;
            this.option = option;
            this.highlighted = highlighted;
            this.optionToName = optionToName;
        }

        @Override
        public WidgetState<HighlightableButton<T>> createState() {
            return new State<>();
        }

        public static class State<T> extends WidgetState<HighlightableButton<T>> {

            @Override
            public void didUpdateWidget(HighlightableButton<T> oldWidget) {
                if (!oldWidget.highlighted && this.widget().highlighted) {
                    this.schedulePostLayoutCallback(() -> Scrollable.reveal(this.context()));
                }
            }

            @Override
            public Widget build(BuildContext context) {
                return new Clickable(
                    Clickable.alwaysClick(() -> this.widget().onSelect.onSelect(this.widget().option)),
                    new HoverableBuilder(
                        (hoverableContext, hovered) -> {
                            var highlighted = hovered || this.widget().highlighted;

                            return new Box(
                                highlighted ? Color.WHITE.withA(.1f): new Color(0),
                                new Padding(
                                    Insets.all(2).withLeft(3),
                                    new Label(
                                        new LabelStyle(Alignment.LEFT, highlighted
                                            ? Color.rgb(ChatFormatting.YELLOW.getColor()) : null, null, highlighted),
                                        true,
                                        this.widget().optionToName.apply(this.widget().option)
                                    )
                                )
                            );
                        }
                    )
                );
            }
        }
    }
}
