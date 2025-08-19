package io.wispforest.owo.braid.widgets.temporal;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.grid.Grid;
import io.wispforest.owo.braid.widgets.label.Label;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.time.*;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class DatePicker extends StatefulWidget {

    public final LocalDate date;
    public final @Nullable Consumer<LocalDate> onChanged;

    public DatePicker(LocalDate date, @Nullable Consumer<LocalDate> onChanged) {
        this.date = date;
        this.onChanged = onChanged;
    }

    @Override
    public WidgetState<DatePicker> createState() {
        return new State();
    }

    public static class State extends WidgetState<DatePicker> {

        private YearMonth page;

        @Override
        public void init() {
            var widget = this.widget();
            this.page = YearMonth.from(widget.date);
        }

        @Override
        public Widget build(BuildContext context) {
            var widget = this.widget();
            return new Sized(
                175, 140,
                new Column(
                    new Row(
                        new Sized(
                            20, 20,
                            new MessageButton(
                                Text.literal("<"),
                                () -> this.setState(() -> this.page = this.page.minusMonths(1))
                            )
                        ),
                        new Sized(
                            135, 20,
                            new MessageButton(
                                Text.literal(page.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + page.getYear()),
                                () -> this.setState(() -> this.page = YearMonth.from(widget.date)),
                                !page.equals(YearMonth.from(widget.date))
                            )
                        ),
                        new Sized(
                            20, 20,
                            new MessageButton(
                                Text.literal(">"),
                                () -> this.setState(() -> this.page = this.page.plusMonths(1))
                            )
                        )
                    ),
                    new Grid(
                        LayoutAxis.VERTICAL,
                        7,
                        Grid.CellFit.tight(),
                        this.getPageContents(this.page)
                    )
                )
            );
        }

        protected List<Widget> getPageContents(YearMonth page) {
            var widget = this.widget();
            List<Widget> widgets = new ArrayList<>();
            LocalDate firstDayOfMonth = page.atDay(1);
            LocalDate lastDayOfMonth = page.atEndOfMonth();
            int firstWeekday = firstDayOfMonth.getDayOfWeek().getValue() % 7;
            int daysInMonth = lastDayOfMonth.getDayOfMonth();

            for (int i = 6; i < 13; i++) {
                widgets.add(
                    new Sized(
                        20, 20,
                        new Label(
                            Text.literal(
                                DayOfWeek.of(i % 7 + 1).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            )
                        )
                    )
                );
            }

            for (int i = 0; i < firstWeekday; i++) {
                widgets.add(null);
            }

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = LocalDate.of(page.getYear(), page.getMonth(), day);
                widgets.add(
                    new MessageButton(
                        Text.literal(day + ""),
                        () -> widget.onChanged.accept(date),
                        !date.equals(widget.date)
                    ));
            }

            return widgets;
        }
    }
}
