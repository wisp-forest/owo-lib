package io.wispforest.uwu.block;

import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Center;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.slider.MessageSlider;
import io.wispforest.owo.braid.widgets.slider.Slider;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.uwu.Uwu;
import io.wispforest.uwu.items.UwuItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.ServerTask;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public class BraidDisplayBlockEntity extends BlockEntity {

    @Environment(EnvType.CLIENT)
    public AppState app;

    @Environment(EnvType.CLIENT)
    public double cursorX, cursorY;

    // ---

    public BraidDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(Uwu.BRAID_DISPLAY_ENTITY, pos, state);
    }

    public static BraidDisplayBlockEntity of(BuildContext context) {
        return Objects.requireNonNull(context.getAncestor(Provider.class)).display.get();
    }

    // ---

    public static class App extends StatelessWidget {
        @Override
        public Widget build(BuildContext context) {
            return new Center(
                new Column(
                    MainAxisAlignment.START,
                    CrossAxisAlignment.CENTER,
                    new Padding(Insets.vertical(2)),
                    List.of(
                        new Panel(
                            OwoUIDrawContext.DARK_PANEL_NINE_PATCH_TEXTURE,
                            new Padding(
                                Insets.all(10),
                                new Label(Text.literal("braid on block real??"))
                            )
                        ),
                        new MessageButton(
                            Text.literal("block button"),
                            () -> MinecraftClient.getInstance().player.dropCreativeStack(UwuItems.BRAID.getDefaultStack())
                        ),
                        new Sized(
                            112,
                            20,
                            new BlockSlider()
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

                private double value;

                @Override
                public Widget build(BuildContext context) {
                    return new MessageSlider(
                        this.value,
                        (newValue) -> this.setState(() -> this.value = newValue),
                        Text.literal("do be sliding doe: " + BigDecimal.valueOf(this.value).setScale(2, RoundingMode.HALF_UP).toPlainString()),
                        LayoutAxis.HORIZONTAL
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

