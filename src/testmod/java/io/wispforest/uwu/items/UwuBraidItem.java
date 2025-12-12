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
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.util.CommonColors;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class UwuBraidItem extends Item {

    @Environment(EnvType.CLIENT)
    private static BraidDisplay display;

    public UwuBraidItem(Properties settings) {
        super(settings.rarity(Rarity.EPIC));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (world.isClientSide()) {
            if (user.isShiftKeyDown()) {
                if (display == null) {
                    display = new BraidDisplay(
                        new DisplayQuad(Vec3.ZERO, new Vec3(5, 0, 0), new Vec3(0, -3, 0)),
                        750,
                        450,
                        new DisplayApp()
                    ).renderAutomatically();

                    BraidDisplayBinding.activate(display);
                }

                var right = new Vec3(-5, 0, 0)
                    .xRot((float) Math.toRadians(-user.getXRot()))
                    .yRot((float) Math.toRadians(-user.getYRot()));

                var down = new Vec3(0, -3, 0)
                    .xRot((float) Math.toRadians(-user.getXRot()))
                    .yRot((float) Math.toRadians(-user.getYRot()));

                display.quad = new DisplayQuad(
                    user.getEyePosition()
                        .add(user.getForward().scale(2.5))
                        .subtract(right.scale(.5))
                        .subtract(down.scale(.5)),
                    right, down
                );
            } else {
                openTestSelector();
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new Tooltip());
    }

    public static void openTestSelector() {
        var settings = new BraidScreen.Settings();
        settings.shouldPause = false;

        Minecraft.getInstance().setScreen(new BraidScreen(settings, new TestSelector()));
    }

    public record Tooltip() implements TooltipComponent {}

    public static class DisplayApp extends StatelessWidget {
        @Override
        public Widget build(BuildContext context) {
            return new Stack(
                new StackBase(new Navigator(new DisplayAppRoute())),
                new Padding(
                    Insets.all(1),
                    new HoverableBuilder(
                        (hoverableContext, hovered) -> new Box(
                            hovered ? Color.rgb(CommonColors.BLUE) : Color.WHITE,
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
                                    new Label(Component.translatable("text.uwu.braid").append(Component.literal(" in world real??")))
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
                                            new ItemStackWidget(UwuItems.BRAID.getDefaultInstance())
                                        )
                                    ),
                                    new Button(
                                        () -> Minecraft.getInstance().player.handleCreativeModeItemDrop(UwuItems.BRAID.getDefaultInstance()),
                                        new Label(
                                            LabelStyle.SHADOW,
                                            true,
                                            Component.translatable("text.uwu.braid").append(Component.literal(" button"))
                                        )
                                    )
                                )
                            ),
                            new MessageButton(
                                Component.literal("test selector"),
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
                                Component.literal("x"),
                                () -> Minecraft.getInstance().schedule(() -> {
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
                            Component.literal("back"),
                            () -> Navigator.pop(context)
                        ),
                        new MessageButton(
                            Component.literal("inspector"),
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
                this.cow = new net.minecraft.world.entity.animal.cow.Cow(EntityType.COW, Minecraft.getInstance().level);
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
