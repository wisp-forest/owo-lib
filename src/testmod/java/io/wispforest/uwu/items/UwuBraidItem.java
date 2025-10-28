package io.wispforest.uwu.items;

import io.wispforest.owo.braid.core.*;
import io.wispforest.owo.braid.display.BraidDisplay;
import io.wispforest.owo.braid.display.BraidDisplayBinding;
import io.wispforest.owo.braid.display.DisplayQuad;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.object.EntityWidget;
import io.wispforest.owo.braid.widgets.object.ItemStackWidget;
import io.wispforest.owo.braid.widgets.Navigator;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.button.Button;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.flex.*;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import io.wispforest.uwu.client.braid.TestSelector;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class UwuBraidItem extends Item {

    @Environment(EnvType.CLIENT)
    private static BraidDisplay display;

    public UwuBraidItem(Settings settings) {
        super(settings.rarity(Rarity.EPIC));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) {
            if (user.isSneaking()) {
                if (display == null) {
                    display = new BraidDisplay(
                        new DisplayQuad(Vec3d.ZERO, new Vec3d(5, 0, 0), new Vec3d(0, -3, 0)),
                        750,
                        450,
                        new DisplayApp()
                    ).renderAutomatically();

                    BraidDisplayBinding.activate(display);
                }

                var right = new Vec3d(-5, 0, 0)
                    .rotateX((float) Math.toRadians(-user.getPitch()))
                    .rotateY((float) Math.toRadians(-user.getYaw()));

                var down = new Vec3d(0, -3, 0)
                    .rotateX((float) Math.toRadians(-user.getPitch()))
                    .rotateY((float) Math.toRadians(-user.getYaw()));

                display.quad = new DisplayQuad(
                    user.getEyePos()
                        .add(user.getRotationVecClient().multiply(2.5))
                        .subtract(right.multiply(.5))
                        .subtract(down.multiply(.5)),
                    right, down
                );
            } else {
                openTestSelector();
            }
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return Optional.of(new Tooltip());
    }

    public static void openTestSelector() {
        var settings = new BraidScreen.Settings();
        settings.shouldPause = false;

        MinecraftClient.getInstance().setScreen(new BraidScreen(settings, new TestSelector()));
    }

    public record Tooltip() implements TooltipData {}

    public static class DisplayApp extends StatelessWidget {
        @Override
        public Widget build(BuildContext context) {
            return new Stack(
                new StackBase(new Navigator(new DisplayAppRoute())),
                new Padding(
                    Insets.all(1),
                    new HoverableBuilder(
                        (hoverableContext, hovered) -> new Box(
                            hovered ? Color.rgb(Colors.BLUE) : Color.WHITE,
                            true
                        )
                    )
                )
            );
        }
    }

    public static class DisplayAppRoute extends StatelessWidget {
        @Override
        public Widget build(BuildContext context) {
            return new Center(
                new Stack(
                    new Column(
                        MainAxisAlignment.START,
                        CrossAxisAlignment.CENTER,
                        new Padding(Insets.vertical(4)),
                        List.of(
                            new Sized(
                                Size.square(96),
                                new Cow()
                            ),
                            new Panel(
                                Panel.VANILLA_DARK,
                                new Padding(
                                    Insets.all(10),
                                    new Label(Text.translatable("text.uwu.braid").append(Text.literal(" in world real??")))
                                )
                            ),
                            new IntrinsicHeight(
                                new Row(
                                    MainAxisAlignment.START,
                                    CrossAxisAlignment.CENTER,
                                    new Button(
                                        UwuBraidItem::openTestSelector,
                                        new Sized(
                                            16,
                                            16,
                                            new ItemStackWidget(UwuItems.BRAID.getDefaultStack())
                                        )
                                    ),
                                    new Button(
                                        () -> MinecraftClient.getInstance().interactionManager.dropCreativeStack(UwuItems.BRAID.getDefaultStack()),
                                        new Label(
                                            LabelStyle.SHADOW,
                                            true,
                                            Text.translatable("text.uwu.braid").append(Text.literal(" button"))
                                        )
                                    )
                                )
                            ),
                            new MessageButton(
                                Text.literal("test selector"),
                                () -> {
                                    Navigator.push(context, new TestSelectorRoute());
                                }
                            )
                        )
                    ),
                    new Align(
                        Alignment.TOP_RIGHT,
                        new Padding(
                            Insets.all(4),
                            new MessageButton(
                                Text.literal("x"),
                                () -> MinecraftClient.getInstance().send(() -> {
                                    BraidDisplayBinding.deactivate(display);

                                    display.app.dispose();
                                    display = null;
                                })
                            )
                        )
                    )
                )
            );
        }
    }

    public static class TestSelectorRoute extends StatelessWidget {
        @Override
        public Widget build(BuildContext context) {
            return new Column(
                MainAxisAlignment.START,
                CrossAxisAlignment.CENTER,
                new Flexible(
                    new TestSelector()
                ),
                new Align(
                    Alignment.BOTTOM,
                    new Row(
                        new MessageButton(
                            Text.literal("back"),
                            () -> Navigator.pop(context)
                        ),
                        new MessageButton(
                            Text.literal("inspector"),
                            () -> AppState.of(context).activateInspector()
                        )
                    )
                )
            );
        }
    }

    public static class Cow extends StatefulWidget {
        @Override
        public WidgetState<Cow> createState() {
            return new State();
        }

        public static class State extends WidgetState<Cow> {

            private Entity cow;

            @Override
            public void init() {
                this.cow = new CowEntity(EntityType.COW, MinecraftClient.getInstance().world);
            }

            @Override
            public Widget build(BuildContext context) {
                return new EntityWidget(
                    1.35,
                    this.cow,
                    widget -> widget.displayMode(EntityWidget.DisplayMode.CURSOR)
                );
            }
        }
    }
}
