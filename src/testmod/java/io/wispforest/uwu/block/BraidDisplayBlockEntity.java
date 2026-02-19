package io.wispforest.uwu.block;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.display.BraidDisplay;
import io.wispforest.owo.braid.display.BraidDisplayBinding;
import io.wispforest.owo.braid.display.DisplayQuad;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.object.BlockWidget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.button.Button;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.slider.slider.MessageSlider;
import io.wispforest.uwu.Uwu;
import io.wispforest.uwu.items.UwuItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class BraidDisplayBlockEntity extends BlockEntity {

    @Environment(EnvType.CLIENT)
    public BraidDisplay display;
    @Environment(EnvType.CLIENT)
    public AtomicBoolean disposed;

    // ---

    public BraidDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(Uwu.BRAID_DISPLAY_ENTITY, pos, state);
    }

    public static BraidDisplayBlockEntity of(BuildContext context) {
        return Objects.requireNonNull(context.getAncestor(Provider.class)).display.get();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void setRemoved() {
        super.setRemoved();

        if (this.disposed != null) {
            if (!this.disposed.compareAndSet(false, true)) return;
            this.display.app.dispose();
            BraidDisplayBinding.deactivate(this.display);
        }
    }

    // ---

    public static class App extends StatelessWidget {
        @Override
        public Widget build(BuildContext context) {
            return new Center(
                new Column(
                    MainAxisAlignment.START,
                    CrossAxisAlignment.CENTER,
                    new Padding(Insets.vertical(4)),
                    List.of(
                        new Sized(
                            112,
                            20,
                            new BlockSlider()
                        ),
                        new Panel(
                            Panel.VANILLA_DARK,
                            new Padding(
                                Insets.all(10),
                                new Label(Component.translatable("text.uwu.braid").append(Component.literal(" on block real??")))
                            )
                        ),
                        new Button(
                            () -> Minecraft.getInstance().player.handleCreativeModeItemDrop(UwuItems.BRAID.getDefaultInstance()),
                            new Row(
                                MainAxisAlignment.START,
                                CrossAxisAlignment.CENTER,
                                new Sized(
                                    16,
                                    16,
                                    new BlockWidget(Blocks.OBSERVER.defaultBlockState())
                                ),
                                new Padding(Insets.horizontal(2)),
                                new Label(
                                    LabelStyle.SHADOW,
                                    true,
                                    Component.translatable("text.uwu.braid").append(Component.literal(" button"))
                                )
                            )
                        )
                    )
                )
            );
        }

        public static class BlockSlider extends StatefulWidget {
            @Override
            public WidgetState<BlockSlider> createState() {
                return new State();
            }

            public static class State extends WidgetState<BlockSlider> {

                private double value = 1;

                @Override
                public Widget build(BuildContext context) {
                    return new MessageSlider(
                        this.value,
                        Component.literal("size: " + BigDecimal.valueOf(this.value).setScale(2, RoundingMode.HALF_UP).toPlainString()), slider -> slider
                            .range(1, 3),
                        (newValue) -> {
                            this.setState(() -> this.value = newValue);

                            var display =BraidDisplayBlockEntity.of(context).display;

                            display.quad = new DisplayQuad(
                                display.quad.pos,
                                new Vec3(0, 0, -14 / 16d),
                                new Vec3(14 / 16d + (this.value - 1), 0, 0)
                            );

                            display.surface.resize(128, (int) (146.29 * display.quad.left.x));
                        }
                    );
                }
            }
        }
    }

    public static class Provider extends InheritedWidget {

        public final WeakReference<BraidDisplayBlockEntity> display;

        public Provider(BraidDisplayBlockEntity display, Widget child) {
            super(child);
            this.display = new WeakReference<>(display);
        }

        @Override
        public boolean mustRebuildDependents(InheritedWidget newWidget) {
            return false;
        }
    }
}
