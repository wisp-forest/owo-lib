package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.RelativePosition;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.combobox.ComboBox;
import io.wispforest.owo.braid.widgets.overlay.Overlay;
import io.wispforest.owo.braid.widgets.overlay.OverlayEntryBuilder;
import io.wispforest.uwu.client.braid.Amogus;
import io.wispforest.uwu.client.braid.TestSelector;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;

public class OverlayTest extends StatefulWidget {
    @Override
    public WidgetState<OverlayTest> createState() {
        return new State();
    }

    public static class State extends WidgetState<OverlayTest> {

        private @Nullable TestSelector.Tests selectedOption = null;

        private void spawn(BuildContext context, double x, double y) {
            Overlay.of(context).add(
                new OverlayEntryBuilder(
                    new Amogus(
                        new Box(Color.randomHue()),
                        new Box(Color.WHITE),
                        8
                    ),
                    new RelativePosition(context, x - 12, y - 12)
                )
            );
        }

        @Override
        public Widget build(BuildContext context) {
            return new Overlay(
                new Builder(innerContext -> {
                    return new Sized(
                        Double.POSITIVE_INFINITY,
                        Double.POSITIVE_INFINITY,
                        new MouseArea(
                            widget -> widget
                                .clickCallback((x, y, button, modifiers) -> {
                                    this.spawn(innerContext, x, y);
                                    return true;
                                })
                                .dragCallback((x, y, dx, dy) -> {
                                    this.spawn(innerContext, x, y);
                                }),
                            new Center(
                                new Panel(
                                    Panel.VANILLA_LIGHT,
                                    new Padding(
                                        Insets.all(10),
                                        new Sized(
                                            120,
                                            null,
                                            new ComboBox<>(
                                                test -> Component.literal(test.name().toLowerCase(Locale.ROOT).replace('_', ' ')),
                                                Arrays.asList(TestSelector.Tests.values()),
                                                this.selectedOption,
                                                option -> this.setState(() -> this.selectedOption = option)
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    );
                })
            );
        }
    }
}
