package io.wispforest.owo.samples.braid.layout;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.Center;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.grid.Grid;

public class Checkerboard extends StatelessWidget {
    @Override
    public Widget build(BuildContext context) {
        return new Center(
            new Sized(                     // since the boxes we'll be using are unsized,
                40.0, 60.0,         // we set the size of the grid explicitly
                new Grid(
                    LayoutAxis.VERTICAL,  // main axis is vertical:
                    2,                    // thus, 2 cross-axis cells means two columns

                    Grid.CellFit.tight(), // we force all children to fill their cells
                                          // and since the grid is tightly constrained, each
                                          // cell will be exactly 20x20 pixels

                    new Box(Color.WHITE), // arrange some white and black boxes such that
                    new Box(Color.BLACK), // they create a checkerboard
                    new Box(Color.BLACK),
                    new Box(Color.WHITE),
                    new Box(Color.WHITE),
                    new Box(Color.BLACK)
                )
            )
        );
    }
}
