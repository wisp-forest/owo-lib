package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.HoverableBuilder;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.button.ButtonStyle;
import io.wispforest.owo.braid.widgets.button.DefaultButtonStyle;
import io.wispforest.owo.braid.widgets.checkbox.Checkbox;
import io.wispforest.owo.braid.widgets.checkbox.CheckboxStyle;
import io.wispforest.owo.braid.widgets.cycle.MessageCyclingButton;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.focus.FocusLevel;
import io.wispforest.owo.braid.widgets.focus.Focusable;
import io.wispforest.owo.braid.widgets.grid.Grid;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.uwu.client.braid.TestSelector;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CyclingTest extends StatefulWidget {
    private static final List<String> coolStrings = List.of(
        "first", "second", "third", "fourth", "fifth"
    );

    private static final List<Integer> coolNumbers = List.of(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
    );

    public enum CoolEnum {
        FIRST, SECOND, THIRD, FOURTH, FIFTH
    }

    @Override
    public WidgetState<CyclingTest> createState() {
        return new State();
    }

    public static class State extends WidgetState<CyclingTest> {
        private CoolEnum selectedEnum = CoolEnum.FIRST;
        private CoolEnum selectedEnumNoWrap = CoolEnum.FIRST;
        private boolean selectedBoolean = false;
        private boolean selectedBooleanNoWrap = false;
        private String selectedString = coolStrings.get(0);
        private String selectedStringNoWrap = coolStrings.get(0);
        private int selectedInt = 0;
        private int selectedIntNoWrap = 0;

        private boolean altButtons = false;

        @Override
        public Widget build(BuildContext context) {
            return new Column(
                MainAxisAlignment.CENTER,
                CrossAxisAlignment.CENTER,
                new DefaultButtonStyle(
                    this.altButtons ? new ButtonStyle(
                        (active, child) -> new HoverableBuilder(
                            (hoverableContext, hovered, hoverableChild) -> {
                                return new Box(
                                    active
                                        ? (hovered || Focusable.levelOf(hoverableContext) == FocusLevel.HIGHLIGHT ? Color.BLUE : Color.WHITE)
                                        : Color.BLACK,
                                    true,
                                    hoverableChild
                                );
                            },
                            child
                        ),
                        Insets.all(10.0),
                        SoundEvents.GENERIC_EXPLODE.value()
                    ) : ButtonStyle.DEFAULT,
                    new Grid(
                        LayoutAxis.VERTICAL,
                        4,
                        Grid.CellFit.tight(),
                        widget -> new Padding(Insets.all(5), widget),
                        null,
                        Label.literal("Cycler"),
                        Label.literal("No Wrap"),
                        Label.literal("Values"),
                        Label.literal("Enum"),
                        MessageCyclingButton.forEnum(
                            this.selectedEnum,
                            Component.literal(selectedEnum.name()),
                            (value, index) -> this.setState(() -> this.selectedEnum = value)
                        ),
                        MessageCyclingButton.forEnum(
                            this.selectedEnumNoWrap,
                            false,
                            Component.literal(selectedEnumNoWrap.name()),
                            (value, index) -> this.setState(() -> this.selectedEnumNoWrap = value)
                        ),
                        Label.literal(String.join(", ", Arrays.stream(CoolEnum.values()).map(Enum::name).collect(Collectors.toList()))),
                        Label.literal("Boolean"),
                        MessageCyclingButton.forBoolean(
                            this.selectedBoolean,
                            Component.literal(this.selectedBoolean ? "true" : "false"),
                            (value, index) -> this.setState(() -> this.selectedBoolean = value)
                        ),
                        new MessageCyclingButton<>(
                            List.of(false, true), this.selectedBooleanNoWrap, false,
                            Component.literal(this.selectedBooleanNoWrap ? "true" : "false"),
                            (value, index) -> this.setState(() -> this.selectedBooleanNoWrap = value)
                        ),
                        Label.literal("false, true"),
                        Label.literal("String"),
                        new MessageCyclingButton<>(
                            coolStrings,
                            this.selectedString,
                            Component.literal(this.selectedString),
                            (value, index) -> this.setState(() -> this.selectedString = value)
                        ),
                        new MessageCyclingButton<>(
                            coolStrings,
                            this.selectedStringNoWrap,
                            false,
                            Component.literal(this.selectedStringNoWrap),
                            (value, index) -> this.setState(() -> this.selectedStringNoWrap = value)
                        ),
                        Label.literal(String.join(", ", coolStrings)),
                        Label.literal("Int"),
                        new MessageCyclingButton<>(
                            coolNumbers,
                            this.selectedInt,
                            Component.literal(coolNumbers.get(this.selectedInt).toString()),
                            (value, index) -> this.setState(() -> this.selectedInt = index)
                        ),
                        new MessageCyclingButton<>(
                            coolNumbers,
                            this.selectedIntNoWrap,
                            false,
                            Component.literal(coolNumbers.get(this.selectedIntNoWrap).toString()),
                            (value, index) -> this.setState(() -> this.selectedIntNoWrap = index)
                        ),
                        Label.literal(coolNumbers.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(", ")))
                    )
                ),
                new Row(
                    MainAxisAlignment.CENTER,
                    CrossAxisAlignment.CENTER,
                    new Checkbox(CheckboxStyle.BRAID, this.altButtons, nowChecked -> this.setState(() -> this.altButtons = nowChecked)),
                    new Padding(
                        Insets.left(5),
                        Label.literal("alternate buttons")
                    )
                )
            );
        }
    }
}
