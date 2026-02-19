package io.wispforest.owo.braid.widgets.grid;

import io.wispforest.owo.braid.core.*;
import io.wispforest.owo.braid.framework.instance.InspectorProperty;
import io.wispforest.owo.braid.framework.instance.MultiChildWidgetInstance;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.widget.MultiChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

public class Grid extends MultiChildInstanceWidget {

    public final LayoutAxis mainAxis;
    public final int crossAxisCells;
    public final CellFit cellFit;

    public Grid(LayoutAxis mainAxis, int crossAxisCells, CellFit cellFit, List<? extends @Nullable Widget> children) {
        super(children.stream().map(widget -> widget == null ? new Padding(Insets.none()) : widget).toList());
        this.mainAxis = mainAxis;
        this.cellFit = cellFit;
        this.crossAxisCells = crossAxisCells;
    }

    public Grid(LayoutAxis mainAxis, int crossAxisCells, CellFit cellFit, @Nullable Widget... children) {
        this(mainAxis, crossAxisCells, cellFit, Arrays.asList(children));
    }

    public Grid(LayoutAxis mainAxis, int crossAxisCells, CellFit cellFit, Function<Widget, Widget> cellWrapper, List<? extends @Nullable Widget> children) {
        this(mainAxis, crossAxisCells, cellFit, children.stream().map(cellWrapper).toList());
    }

    public Grid(LayoutAxis mainAxis, int crossAxisCells, CellFit cellFit, Function<Widget, Widget> cellWrapper, @Nullable Widget... children) {
        this(mainAxis, crossAxisCells, cellFit, cellWrapper, Arrays.asList(children));
    }

    @Override
    public MultiChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends MultiChildWidgetInstance<Grid> {

        private double[] debugCrossAxisSizes;
        private double[] debugMainAxisSizes;

        public Instance(Grid widget) {
            super(widget);
        }

        @Override
        public void setWidget(Grid widget) {
            if (this.widget.mainAxis == widget.mainAxis
                && this.widget.crossAxisCells == widget.crossAxisCells
                && this.widget.cellFit.equals(widget.cellFit)) {
                return;
            }

            super.setWidget(widget);
            this.markNeedsLayout();
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var mainAxis = this.widget.mainAxis;
            var crossAxis = mainAxis.opposite();

            var crossAxisCells = this.widget.crossAxisCells;
            var mainAxisCells = Mth.ceil(this.children.size() / (double) this.widget.crossAxisCells);

            var mustMeasureCrossAxis = this.widget.cellFit.isTight() && !crossAxis.choose(constraints.hasTightWidth(), constraints.hasTightHeight());
            var mustMeasureMainAxis = this.widget.cellFit.isTight() && !mainAxis.choose(constraints.hasTightWidth(), constraints.hasTightHeight());

            var fixedCrossAxisCellSize = constraints.maxOnAxis(crossAxis) / crossAxisCells;
            var fixedMainAxisCellSize = constraints.maxOnAxis(mainAxis) / mainAxisCells;

            double @Nullable [] dynamicCrossAxisCellSizes = null;
            if (mustMeasureCrossAxis) {
                dynamicCrossAxisCellSizes = this.measureCrossAxis(fixedMainAxisCellSize);
            }

            double @Nullable [] dynamicMainAxisCellSizes = null;
            if (mustMeasureMainAxis) {
                dynamicMainAxisCellSizes = this.measureMainAxis(fixedCrossAxisCellSize);
            }

            for (int mainAxisIdx = 0; mainAxisIdx < mainAxisCells; mainAxisIdx++) {
                var maxMainAxisChildSize = mustMeasureMainAxis
                    ? dynamicMainAxisCellSizes[mainAxisIdx]
                    : fixedMainAxisCellSize;

                var firstChildIdx = mainAxisIdx * this.widget.crossAxisCells;
                var lastChildIdx = Math.min(this.children.size(), firstChildIdx + this.widget.crossAxisCells) - 1;

                for (var childIdx = firstChildIdx; childIdx <= lastChildIdx; childIdx++) {
                    var child = this.children.get(childIdx);

                    var maxCrossAxisChildSize = mustMeasureCrossAxis
                        ? dynamicCrossAxisCellSizes[childIdx % this.widget.crossAxisCells]
                        : fixedCrossAxisCellSize;

                    var maxWidth = mainAxis == LayoutAxis.VERTICAL ? maxCrossAxisChildSize : maxMainAxisChildSize;
                    var maxHeight = mainAxis == LayoutAxis.VERTICAL ? maxMainAxisChildSize : maxCrossAxisChildSize;

                    var childConstraints = this.widget.cellFit.isTight()
                        ? Constraints.tightOnAxis(maxWidth, maxHeight)
                        : Constraints.loose(Size.of(maxWidth, maxHeight));

                    child.layout(childConstraints);
                }
            }

            var actualCrossAxisSizes = new double[crossAxisCells];
            var actualMainAxisSizes = new double[mainAxisCells];

            var minCrossAxisCellSize = constraints.minOnAxis(crossAxis) / crossAxisCells;
            var minMainAxisCellSize = constraints.minOnAxis(mainAxis) / mainAxisCells;

            for (var childIdx = 0; childIdx < this.children.size(); childIdx++) {
                var child = this.children.get(childIdx);

                var mainAxisCell = childIdx / crossAxisCells;
                var crossAxisCell = childIdx % crossAxisCells;

                actualCrossAxisSizes[crossAxisCell] = Math.max(minCrossAxisCellSize, Math.max(actualCrossAxisSizes[crossAxisCell], child.transform.getExtent(crossAxis)));
                actualMainAxisSizes[mainAxisCell] = Math.max(minMainAxisCellSize, Math.max(actualMainAxisSizes[mainAxisCell], child.transform.getExtent(mainAxis)));
            }

            var alignment = this.widget.cellFit instanceof CellFit.Loose loose
                ? loose.alignment
                : Alignment.TOP_LEFT;

            var mainAxisPos = 0d;
            for (var mainAxisCell = 0; mainAxisCell < mainAxisCells; mainAxisCell++) {
                var crossAxisPos = 0d;

                for (var crossAxisCell = 0; crossAxisCell < crossAxisCells; crossAxisCell++) {
                    var childIdx = mainAxisCell * crossAxisCells + crossAxisCell;
                    if (childIdx >= this.children.size()) break;

                    var child = this.children.get(childIdx);

                    child.transform.setCoordinate(crossAxis, crossAxisPos + alignment.alignHorizontal(actualCrossAxisSizes[crossAxisCell], child.transform.getExtent(crossAxis)));
                    child.transform.setCoordinate(mainAxis, mainAxisPos + alignment.alignVertical(actualMainAxisSizes[mainAxisCell], child.transform.getExtent(mainAxis)));

                    crossAxisPos += actualCrossAxisSizes[crossAxisCell];
                }

                mainAxisPos += actualMainAxisSizes[mainAxisCell];
            }

            this.debugCrossAxisSizes = actualCrossAxisSizes;
            this.debugMainAxisSizes = actualMainAxisSizes;

            this.transform.setSize(
                mainAxis == LayoutAxis.VERTICAL
                    ? Size.of(DoubleStream.of(actualCrossAxisSizes).sum(), DoubleStream.of(actualMainAxisSizes).sum())
                    : Size.of(DoubleStream.of(actualMainAxisSizes).sum(), DoubleStream.of(actualCrossAxisSizes).sum())
            );
        }

        @Override
        public List<InspectorProperty> debugListInspectorProperties() {
            return List.of(
                new InspectorProperty(
                    Component.literal("Main Axis"),
                    Component.literal(this.widget.mainAxis.toString())
                )
            );
        }

        @Override
        public boolean debugHasVisualizers() {
            return true;
        }

        @Override
        protected void debugDrawVisualizers(BraidGraphics graphics) {
            var frameColor = Color.rgb(0xFFD65A);
            graphics.drawRectOutline(
                0, 0, (int) this.transform.width(), (int) this.transform.height(), frameColor.argb()
            );

            var verticalSizes = this.widget.mainAxis == LayoutAxis.VERTICAL
                ? this.debugMainAxisSizes
                : this.debugCrossAxisSizes;

            var horizontalSizes = this.widget.mainAxis == LayoutAxis.VERTICAL
                ? this.debugCrossAxisSizes
                : this.debugMainAxisSizes;

            var verticalPos = 0.0;
            for (int i = 0; i < verticalSizes.length; i++) {
                if (i > 0) {
                    graphics.drawDashedLine(
                        RenderPipelines.GUI,
                        0, verticalPos, this.transform.width(), verticalPos,
                        1, 2, frameColor
                    );
                }

                graphics.drawText(
                    Component.literal(verticalSizes[i] + "px").withStyle(style -> style.withFont(new FontDescription.Resource(Minecraft.UNIFORM_FONT))),
                    0, (float) verticalPos, 1f, Color.WHITE.argb(),
                    OwoUIGraphics.TextAnchor.TOP_RIGHT
                );

                verticalPos += verticalSizes[i];
            }

            var horizontalPos = 0.0;
            for (int i = 0; i < horizontalSizes.length; i++) {
                if (i > 0) {
                    graphics.drawDashedLine(
                        RenderPipelines.GUI,
                        horizontalPos, 0, horizontalPos, this.transform.height(),
                        1, 2, frameColor
                    );
                }

                graphics.drawText(
                    Component.literal(horizontalSizes[i] + "px").withStyle(style -> style.withFont(new FontDescription.Resource(Minecraft.UNIFORM_FONT))),
                    (float) horizontalPos, 0, 1f, Color.WHITE.argb(),
                    OwoUIGraphics.TextAnchor.BOTTOM_LEFT
                );

                horizontalPos += horizontalSizes[i];
            }
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return this.widget.mainAxis == LayoutAxis.VERTICAL
                ? DoubleStream.of(this.measureCrossAxis(height)).sum()
                : DoubleStream.of(this.measureMainAxis(height)).sum();
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return this.widget.mainAxis == LayoutAxis.VERTICAL
                ? DoubleStream.of(this.measureMainAxis(width)).sum()
                : DoubleStream.of(this.measureCrossAxis(width)).sum();
        }

        protected double[] measureCrossAxis(double mainAxisCellSize) {
            var crossAxisCells = this.widget.crossAxisCells;
            var measureFunction = this.widget.mainAxis.opposite().<ToDoubleFunction<WidgetInstance<?>>>chooseCompute(
                () -> child -> child.getIntrinsicWidth(mainAxisCellSize),
                () -> child -> child.getIntrinsicHeight(mainAxisCellSize)
            );

            var intrinsics = this.children.stream().mapToDouble(measureFunction).toArray();

            var sizes = new double[crossAxisCells];
            for (var cell = 0; cell < crossAxisCells; cell++) {
                var cellSize = 0d;

                for (var childIdx = cell; childIdx < this.children.size(); childIdx += crossAxisCells) {
                    cellSize = Math.max(cellSize, intrinsics[childIdx]);
                }

                sizes[cell] = cellSize;
            }

            return sizes;
        }

        protected double[] measureMainAxis(double crossAxisCellSize) {
            var crossAxisCells = this.widget.crossAxisCells;
            var mainAxisCells = Mth.ceil(this.children.size() / (double) this.widget.crossAxisCells);

            var measureFunction = this.widget.mainAxis.<ToDoubleFunction<WidgetInstance<?>>>chooseCompute(
                () -> child -> child.getIntrinsicWidth(crossAxisCellSize),
                () -> child -> child.getIntrinsicHeight(crossAxisCellSize)
            );

            var intrinsics = this.children.stream().mapToDouble(measureFunction).toArray();

            var sizes = new double[mainAxisCells];
            for (var cell = 0; cell < mainAxisCells; cell++) {
                var cellSize = 0d;

                var firstChild = cell * crossAxisCells;
                var lastChild = firstChild + (crossAxisCells - 1);

                for (var childIdx = firstChild; childIdx <= lastChild; childIdx++) {
                    if (childIdx >= this.children.size()) break;
                    cellSize = Math.max(cellSize, intrinsics[childIdx]);
                }

                sizes[cell] = cellSize;
            }

            return sizes;
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            return this.computeHighestBaselineOffset();
        }
    }

    public static sealed abstract class CellFit {

        public abstract boolean isTight();

        public static CellFit loose() {
            return loose(Alignment.CENTER);
        }

        public static CellFit loose(Alignment alignment) {
            return new Loose(alignment);
        }

        public static CellFit tight() {
            return Tight.INSTANCE;
        }

        public static final class Tight extends CellFit {
            public static final Tight INSTANCE = new Tight();

            @Override
            public boolean isTight() {
                return true;
            }
        }

        public static final class Loose extends CellFit {
            public final Alignment alignment;
            public Loose(Alignment alignment) {this.alignment = alignment;}

            @Override
            public boolean isTight() {
                return false;
            }
        }
    }
}
