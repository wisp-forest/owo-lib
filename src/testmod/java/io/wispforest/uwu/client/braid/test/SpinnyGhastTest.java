package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.grid.Grid;
import io.wispforest.owo.braid.widgets.intents.Interactable;
import io.wispforest.owo.braid.widgets.object.Viewer;
import io.wispforest.owo.braid.widgets.object.entity.EntityDisplayMode;
import io.wispforest.owo.braid.widgets.object.entity.EntityWidget;
import io.wispforest.owo.braid.widgets.stack.Stack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.stream.Stream;

public class SpinnyGhastTest extends StatefulWidget {
    @Override
    public WidgetState<SpinnyGhastTest> createState() {
        return new State();
    }

    public static class State extends WidgetState<SpinnyGhastTest> {

        private List<Entity> entities;
        private int selectedEntityIdx = 0;

        @Override
        public void init() {
            this.entities = Stream.of(
                EntityType.HAPPY_GHAST,
                EntityType.ALLAY,
                EntityType.COW,
                EntityType.CREAKING,
                EntityType.BREEZE,
                EntityType.COPPER_GOLEM,
                EntityType.BAMBOO_RAFT,
                EntityType.ITEM_FRAME
            ).<Entity>map(
                entityType -> entityType.create(Minecraft.getInstance().level, EntitySpawnReason.MOB_SUMMONED)
            ).toList();
        }

        @Override
        public Widget build(BuildContext context) {
            return new Row(
                MainAxisAlignment.CENTER,
                CrossAxisAlignment.CENTER,
                new Padding(Insets.all(10)),
                List.of(
                    new VerticalCarouselThing(
                        this.selectedEntityIdx,
                        idx -> this.setState(() -> this.selectedEntityIdx = idx),
                        Size.square(64),
                        this.entities.stream().<Widget>map(entity -> new EntityWidget(.75, entity, widget -> widget.displayMode(EntityDisplayMode.CURSOR))).toList()
                    ),
                    new Viewer(
                        transform ->
                            new Sized(
                                Size.square(400),
                                new EntityWidget(
                                    .75,
                                    this.entities.get(this.selectedEntityIdx),
                                    widget -> widget
                                        .displayMode(EntityDisplayMode.NONE)
                                        .transform(transform)
                                )
                            )
                    )
                )
            );
        }
    }

    public static class VerticalCarouselThing extends StatefulWidget {

        public final int selectedIndex;
        public final IntConsumer onChanged;
        public final Size itemSize;
        public final List<Widget> children;

        public VerticalCarouselThing(int selectedIndex, IntConsumer onChanged, Size itemSize, List<Widget> children) {
            this.selectedIndex = selectedIndex;
            this.onChanged = onChanged;
            this.itemSize = itemSize;
            this.children = children;
        }

        @Override
        public WidgetState<VerticalCarouselThing> createState() {
            return new VerticalCarouselThing.State();
        }

        public static class State extends WidgetState<VerticalCarouselThing> {

            @Override
            public Widget build(BuildContext context) {
                var displayChildren = new ArrayList<Widget>();
                displayChildren.add(new Panel(Panel.VANILLA_INSET));

                var offset = -this.widget().selectedIndex * this.widget().itemSize.height() / 2;
                offset -= this.widget().itemSize.height() / 4;

                for (var i = 0; i < this.widget().children.size(); i++) {
                    var thisOffset = (float) offset;

                    var scale = i == this.widget().selectedIndex ? 1 : .5f;
                    offset += this.widget().itemSize.height() * scale;

                    if (scale == 1) {
                        //noinspection lossy-conversions
                        thisOffset += this.widget().itemSize.height() / 4f;
                    }

                    var elementIndex = i;
                    displayChildren.add(
                        new Transform(
                            new Matrix3x2f()
                                .translate(0f, thisOffset)
                                .scale(scale, scale),
                            Interactable.primary(
                                () -> this.widget().onChanged.accept(elementIndex),
                                this.widget().children.get(i)
                            )
                        ));
                }

                return new MouseArea(
                    widget -> widget
                        .scrollCallback((horizontal, vertical) -> {
                            if (vertical == 0) return false;
                            this.setState(() -> {
                                this.widget().onChanged.accept(Mth.clamp(
                                    this.widget().selectedIndex - (int) Math.signum(vertical),
                                    0,
                                    this.widget().children.size() - 1
                                ));
                            });

                            return true;
                        }),
                    new Row(
                        MainAxisAlignment.CENTER,
                        CrossAxisAlignment.CENTER,
                        new Sized(
                            Size.of(18, this.widget().itemSize.height()),
                            new Grid(
                                LayoutAxis.HORIZONTAL,
                                2,
                                Grid.CellFit.tight(),
                                new MessageButton(
                                    Component.literal("↑"),
                                    this.widget().selectedIndex > 0 ? () -> this.widget().onChanged.accept(this.widget().selectedIndex - 1) : null
                                ),
                                new MessageButton(
                                    Component.literal("↓"),
                                    this.widget().selectedIndex < this.widget().children.size() - 1
                                        ? () -> this.widget().onChanged.accept(this.widget().selectedIndex + 1)
                                        : null
                                )
                            )
                        ),
                        new Sized(
                            this.widget().itemSize,
                            new Stack(
                                displayChildren
                            )
                        )
                    )
                );
            }
        }
    }
}
