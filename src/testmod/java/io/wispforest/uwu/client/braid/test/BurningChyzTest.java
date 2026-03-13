package io.wispforest.uwu.client.braid.test;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.object.entity.EntityDisplayMode;
import io.wispforest.owo.braid.widgets.object.entity.EntityWidget;
import io.wispforest.owo.braid.widgets.recipeviewer.RecipeViewerExclusionZone;
import io.wispforest.owo.braid.widgets.recipeviewer.RecipeViewerStack;
import io.wispforest.owo.braid.widgets.recipeviewer.StackDropArea;
import io.wispforest.owo.util.ViewerStack;
import io.wispforest.uwu.client.braid.Bikeshed;
import io.wispforest.uwu.client.braid.Amogus;
import io.wispforest.uwu.client.braid.BurningChyz;
import net.minecraft.world.item.Items;

import java.util.List;

public class BurningChyzTest extends StatelessWidget {

    @Override
    public Widget build(BuildContext context) {
        return new Row(
            MainAxisAlignment.START,
            CrossAxisAlignment.CENTER,
            new Padding(Insets.horizontal(10)),
            List.of(
                new Sized(
                    250.0,
                    250.0,
                    new Panel(
                        Panel.VANILLA_LIGHT,
                        new Padding(
                            Insets.all(8),
                            new Panel(
                                Panel.VANILLA_INSET,
                                new RecipeViewerStack(
                                    () -> ViewerStack.OfItem.of(Items.GOLD_BLOCK),
                                    new StackDropArea(
                                        stack -> stack instanceof ViewerStack.OfItem,
                                        stack -> System.out.println("chyz: mmm i ate a " + ((ViewerStack.OfItem) stack).asStack()),
                                        new RecipeViewerExclusionZone(
//                                                new Viewer(
//                                                    transform ->
                                            new EntityWidget(
                                                1,
                                                BurningChyz.of(context),
                                                widget -> widget.displayMode(EntityDisplayMode.CURSOR)
                                            )
                                        )
                                    )
//                                                )
                                )
                            )
                        )
                    )
                ),
                new Amogus(
                    new Box(Color.RED),
                    new Box(Color.WHITE),
                    16
                ),
                new Bikeshed()
            )
        );
    }
}
