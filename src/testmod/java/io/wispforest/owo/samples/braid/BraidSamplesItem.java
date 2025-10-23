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
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.world.World;

import java.util.List;

public class BraidSamplesItem extends Item {

    public BraidSamplesItem(Settings settings) {
        super(settings);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            return ActionResult.SUCCESS;
        }

        MinecraftClient.getInstance().setScreen(new BraidScreen(SCREEN_SETTINGS, new SampleSelector()));
        return ActionResult.SUCCESS;
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
            new Sample(new SharedCounter(), "Shared Counter")
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
                                                    Text.literal(sample.name()),
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
