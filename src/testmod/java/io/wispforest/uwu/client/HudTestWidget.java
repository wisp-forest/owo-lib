package io.wispforest.uwu.client;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.ListenableValue;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.object.ItemStackWidget;
import io.wispforest.uwu.items.UwuItems;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;

public class HudTestWidget extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new ListenableBuilder(
            SHOW_TEST_HUD,
            (listenableContext, child) -> new Visibility(
                SHOW_TEST_HUD.value(),
                child
            ),
            new Align(
                Alignment.TOP_RIGHT,
                new Padding(
                    Insets.all(5),
                    new Column(
                        MainAxisAlignment.CENTER,
                        CrossAxisAlignment.CENTER,
                        new Sized(
                            100, null,
                            new Label(LabelStyle.SHADOW, true, Component.translatable("compliance.playtime.message"))
                        ),
                        new Padding(
                            Insets.all(3),
                            new ItemStackWidget(UwuItems.BRAID.getDefaultInstance())
                        ),
                        new Timer()
                    )
                )
            )
        );
    }

    public static class Timer extends StatefulWidget {
        @Override
        public WidgetState<Timer> createState() {
            return new State();
        }

        public static class State extends WidgetState<Timer> {

            private int seconds = -1;

            @Override
            public void init() {
                this.count();
            }

            private void count() {
                this.scheduleDelayedCallback(Duration.ofSeconds(1), this::count);
                this.setState(() -> this.seconds++);
            }

            @Override
            public Widget build(BuildContext context) {
                return new Label(
                    LabelStyle.SHADOW,
                    true,
                    Component.literal("time in session: " + DurationFormatUtils.formatDuration(this.seconds * 1000L, "HH:mm:ss"))
                );
            }
        }
    }

    // ---

    public static final ListenableValue<Boolean> SHOW_TEST_HUD = new ListenableValue<>(false);
}