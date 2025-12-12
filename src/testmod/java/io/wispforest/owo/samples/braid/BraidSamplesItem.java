package io.wispforest.owo.samples.braid;

import io.wispforest.owo.braid.core.BraidScreen;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.Navigator;
import io.wispforest.owo.braid.widgets.basic.Center;
import io.wispforest.owo.braid.widgets.basic.IntrinsicWidth;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;

import java.util.List;

public class BraidSamplesItem extends Item {

    public BraidSamplesItem(Properties settings) {
        super(settings);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        if (!level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Minecraft.getInstance().setScreen(new BraidScreen(SCREEN_SETTINGS, new SampleSelector()));
        return InteractionResult.SUCCESS;
    }

    // ---

    private static final BraidScreen.Settings SCREEN_SETTINGS = Util.make(() -> {
        var settings = new BraidScreen.Settings();
        settings.shouldPause = false;

        return settings;
    });

    private static List<Sample> allSamples() {
        return List.of(
            new Sample(new SimpleCounter(), "Simple Counter"),
            new Sample(new SharedCounter(), "Shared Counter"),
            new Sample(new LayoutWidgetExamples(), "Layout Widget Examples")
        );
    }

    public record Sample(Widget widget, String name) {}

    // ---

    public static class SampleSelector extends StatefulWidget {
        @Override
        public WidgetState<SampleSelector> createState() {
            return new State();
        }

        public static class State extends WidgetState<SampleSelector> {

            @Override
            public Widget build(BuildContext context) {
                return new Center(
                    new Panel(
                        Panel.VANILLA_LIGHT,
                        new Padding(
                            Insets.all(6),
                            new Panel(
                                Panel.VANILLA_INSET,
                                new Padding(
                                    Insets.all(4),
                                    new IntrinsicWidth(
                                        new Column(
                                            MainAxisAlignment.CENTER,
                                            CrossAxisAlignment.CENTER,
                                            new Padding(Insets.vertical(2)),
                                            allSamples().stream()
                                                .map(sample -> new MessageButton(
                                                    Component.literal(sample.name()),
                                                    () -> Navigator.push(context, sample.widget())
                                                ))
                                                .toList()
                                        )
                                    )
                                )
                            )
                        )
                    )
                );
            }
        }
    }
}
