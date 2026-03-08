package io.wispforest.uwu.client.braid;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.HoverableBuilder;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.button.ButtonPanel;
import io.wispforest.owo.braid.widgets.button.ButtonStyle;
import io.wispforest.owo.braid.widgets.button.DefaultButtonStyle;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.focus.Focusable;
import io.wispforest.owo.braid.widgets.slider.SliderStyle;
import io.wispforest.owo.braid.widgets.slider.slider.Slider;
import io.wispforest.owo.ui.component.ButtonComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public class PlumTests extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Panel(
            TestSelector.PLUM_PANEL,
            new Padding(
                Insets.all(10),
                new Panel(
                    TestSelector.PLUM_PANEL_INSET,
                    new Padding(
                        Insets.all(10),
                        new Column(
                            new Padding(Insets.vertical(6)),
                            List.of(
                                new Sized(
                                    150, 12,
                                    new PlumSlider()
                                ),
                                DefaultButtonStyle.merge(
                                    new ButtonStyle(
                                        (active, child) -> new HoverableBuilder(
                                            (innerContext, hovered) -> {
                                                return new Panel(
                                                    active
                                                        ? (hovered || Focusable.shouldShowHighlight(innerContext))
                                                        ? Identifier.fromNamespaceAndPath("uwu", "plum_slider_handle_selected")
                                                        : Identifier.fromNamespaceAndPath("uwu", "plum_slider_handle")
                                                        : Identifier.fromNamespaceAndPath("uwu", "plum_slider_handle"),
                                                    child
                                                );
                                            }
                                        ),
                                        null, null
                                    ),
                                    new MessageButton(Component.literal("a plum button"), () -> {})
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    public static class PlumSlider extends StatefulWidget {

        @Override
        public WidgetState<PlumSlider> createState() {
            return new State();
        }

        public static class State extends WidgetState<PlumSlider> {

            private double value = .5;

            @Override
            public Widget build(BuildContext context) {
                return new Slider(
                    this.value,
                    widget -> widget
                        .style(new SliderStyle<>(
                            new Panel(
                                Identifier.fromNamespaceAndPath("uwu", "plum_slider_track")
                            ),
                            active -> new HoverableBuilder((hoverableContext, hovered) -> new Panel(
                                hovered
                                    ? Identifier.fromNamespaceAndPath("uwu", "plum_slider_handle_selected")
                                    : Identifier.fromNamespaceAndPath("uwu", "plum_slider_handle")
                            )),
                            8.0,
                            Optional.empty()
                        )),
                    newValue -> this.setState(() -> this.value = newValue)
                );
            }
        }
    }
}
