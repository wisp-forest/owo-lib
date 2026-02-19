package io.wispforest.owo.samples.braid;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Center;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.Flexible;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.sharedstate.ShareableState;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import net.minecraft.network.chat.Component;

public class SharedCounter extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Center(
            new Panel(
                Panel.VANILLA_DARK,
                new Padding(
                    Insets.all(10),
                    new Sized(
                        70,
                        null,
                        new SharedState<>(
                            CounterState::new,
                            new Column(
                                new Row(
                                    new Flexible(
                                        new CountButton(-1)
                                    ),
                                    new Flexible(
                                        new CountButton(1)
                                    )
                                ),
                                new Padding(
                                    Insets.top(5),
                                    new CountLabel()
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    public static class CountLabel extends StatelessWidget {
        @Override
        public Widget build(BuildContext context) {
            var count = SharedState.get(context, CounterState.class).count;
            return Label.literal("Count: " + count);
        }
    }

    public static class CountButton extends StatelessWidget {

        public final int countBy;

        public CountButton(int countBy) {
            this.countBy = countBy;
        }

        @Override
        public Widget build(BuildContext context) {
            return new MessageButton(
                this.countBy > 0 ? Component.literal("+" + this.countBy) : Component.literal(String.valueOf(this.countBy)),
                () -> SharedState.set(context, CounterState.class, state -> state.count += this.countBy)
            );
        }
    }

    public static class CounterState extends ShareableState {
        public int count = 0;
    }
}
