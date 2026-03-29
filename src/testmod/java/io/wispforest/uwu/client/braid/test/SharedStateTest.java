package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.button.Button;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.Flexible;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.sharedstate.ShareableState;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import io.wispforest.uwu.client.braid.TestSelector;

public class SharedStateTest extends StatefulWidget {
    @Override
    public WidgetState<SharedStateTest> createState() {
        return new State();
    }

    public static class State extends WidgetState<SharedStateTest> {
        @Override
        public Widget build(BuildContext context) {
            return new Sized(
                400,
                250,
                new Column(
                    new Flexible(new State.TheTest(false)),
                    new Flexible(new State.TheTest(true))
                )
            );
        }

        public static class TheTest extends StatelessWidget {

            public final boolean nest;

            public TheTest(boolean nest) {
                this.nest = nest;
            }

            @Override
            public Widget build(BuildContext context) {
                return new SharedState<>(
                    CounterState::new,
                    new Row(
                        new Flexible(new State.LeftBody()),
                        new Flexible(new Center(new State.RightBody())),
                        this.nest ? new Flexible(2, new State.TheTest(false)) : new Padding(Insets.none())
                    )
                );
            }
        }

        public static class LeftBody extends StatelessWidget {
            @Override
            public Widget build(BuildContext context) {
                System.out.println("panel rebuild");
                return new Panel(
                    SharedState.select(context, CounterState.class, state -> state.dark)
                        ? Panel.VANILLA_DARK
                        : Panel.VANILLA_LIGHT,
                    new State.CounterText()
                );
            }
        }

        public static class RightBody extends StatelessWidget {
            @Override
            public Widget build(BuildContext context) {
                return new IntrinsicWidth(
                    new Column(
                        new Button(
                            () -> SharedState.set(context, CounterState.class, state -> state.count += 1),
                            Label.literal("increment")
                        ),
                        new Button(
                            () -> SharedState.set(context, CounterState.class, state -> state.dark = !state.dark),
                            Label.literal("toggle darkness")
                        )
                    )
                );
            }
        }

        public static class CounterText extends StatelessWidget {
            @Override
            public Widget build(BuildContext context) {
                System.out.println("text rebuild");
                return Label.literal("current state: " + SharedState.select(context, CounterState.class, state -> state.count));
            }
        }
    }

    public static class CounterState extends ShareableState {
        public int count = 0;
        public boolean dark = false;
    }
}
