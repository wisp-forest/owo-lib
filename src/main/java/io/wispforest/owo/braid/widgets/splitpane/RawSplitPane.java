package io.wispforest.owo.braid.widgets.splitpane;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.Key;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Constrain;
import io.wispforest.owo.braid.widgets.basic.LayoutBuilder;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Flex;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RawSplitPane extends StatefulWidget {

    public final LayoutAxis axis;
    public final @Nullable SplitController controller;
    public final @Nullable SplitPaneStyle style;
    public final boolean enabled;
    public final List<SplitChild> children;

    public RawSplitPane(
        LayoutAxis axis,
        @Nullable SplitController controller,
        @Nullable SplitPaneStyle style,
        boolean enabled,
        List<? extends Widget> children
    ) {
        this.axis = axis;
        this.controller = controller;
        this.style = style;
        this.enabled = enabled;
        this.children = new ArrayList<>();
        for (var child : children) {
            if (!(child instanceof SplitChild)) child = new SplitChild(child);
            this.children.add((SplitChild) child);
        }
    }

    public RawSplitPane(
        LayoutAxis axis,
        @Nullable SplitPaneStyle style,
        Widget... children
    ) {
        this(axis, null, style, true, Arrays.asList(children));
    }

    public RawSplitPane(
        LayoutAxis axis,
        SplitController controller,
        @Nullable SplitPaneStyle style,
        Widget... children
    ) {
        this(axis, controller, style, true, Arrays.asList(children));
    }

    @Override
    public WidgetState<RawSplitPane> createState() {
        return new State();
    }

    public static class State extends WidgetState<RawSplitPane> {

        private SplitController controller;
        private final Runnable controllerListener = () -> setState(() -> {});

        private int draggingIndex = -1;
        private double contentSpace = 0;
        private double prevContentSpace = -1;
        private double dragRatio = 0;
        private boolean ratiosInitialized = false;
        private double[] paneSizes;
        private SplitPaneStyle resolvedStyle = SplitPaneStyle.DEFAULT.fillDefaults();

        @Override
        public void init() {
            var widget = this.widget();
            this.controller = Objects.requireNonNullElseGet(
                widget.controller,
                () -> new SplitController(computeInitialRatiosNormalized(widget.children))
            );
            this.controller.addListener(this.controllerListener);
        }

        @Override
        public void didUpdateWidget(RawSplitPane oldWidget) {
            var newController = this.widget().controller;
            if (newController != oldWidget.controller) {
                this.controller.removeListener(this.controllerListener);
                this.controller = newController != null
                    ? newController
                    : new SplitController(this.widget().children.size());
                this.controller.addListener(this.controllerListener);
            }
        }

        @Override
        public void dispose() {
            this.controller.removeListener(this.controllerListener);
        }

        @Override
        public Widget build(BuildContext context) {
            var enabled = this.widget().enabled;
            this.resolvedStyle = (this.widget().style != null ? this.widget().style : SplitPaneStyle.DEFAULT).fillDefaults();
            var style = this.resolvedStyle;

            return new LayoutBuilder((innerContext, constraints) -> {
                var axis = this.widget().axis;
                var children = this.widget().children;
                var numDividers = children.size() - 1;
                var dividerThickness = style.dividerThickness();

                this.contentSpace = constraints.maxOnAxis(axis) - numDividers * dividerThickness;
                var crossSize = constraints.maxOnAxis(axis.opposite());

                if (this.ratiosInitialized && this.contentSpace != this.prevContentSpace && this.draggingIndex == -1 && this.widget().controller == null) {
                    this.recomputeRatiosOnResize(children, this.contentSpace, numDividers, style);
                }
                this.prevContentSpace = this.contentSpace;

                double[] ratios;
                if (!this.ratiosInitialized) {
                    ratios = computeInitialRatios(children);
                    for (int i = 0; i < numDividers; i++) {
                        this.controller.ratios[i] = ratios[i];
                    }
                    this.paneSizes = new double[children.size()];
                    var prevRatio = 0d;
                    for (var i = 0; i < children.size(); i++) {
                        var nextRatio = i < numDividers ? ratios[i] : 1.0;
                        this.paneSizes[i] = (nextRatio - prevRatio) * this.contentSpace;
                        prevRatio = nextRatio;
                    }
                    this.ratiosInitialized = true;
                } else {
                    ratios = new double[numDividers];
                    for (int i = 0; i < numDividers; i++) {
                        ratios[i] = this.controller.getRatio(i);
                    }
                }

                var widgets = new ArrayList<Widget>();
                var prevRatio = 0d;

                for (var i = 0; i < children.size(); i++) {
                    var splitChild = children.get(i);
                    var child = splitChild.child;
                    var nextRatio = i < numDividers ? ratios[i] : 1.0;
                    var paneSize = (nextRatio - prevRatio) * this.contentSpace;

                    var childConstraints = Constraints.tight(axis.createSize(paneSize, crossSize));
                    widgets.add(new Constrain(childConstraints, child).key(child.key()));

                    if (i < numDividers) {
                        var dividerIndex = i;
                        var dragging = this.draggingIndex == dividerIndex;
                        var dividerVisual = style.dividerBuilder().build(dragging);
                        var dividerConstraints = Constraints.tight(axis.createSize(dividerThickness, crossSize));

                        Widget divider;
                        if (!enabled) {
                            divider = dividerVisual;
                        } else {
                            divider = new MouseArea(
                                ma -> ma
                                    .dragStartCallback((button, modifiers) -> setState(() -> {
                                        this.draggingIndex = dividerIndex;
                                        this.dragRatio = ratios[dividerIndex];
                                    }))
                                    .dragCallback((x, y, dx, dy) -> this.moveDivider(dividerIndex, axis.choose(dx, dy)))
                                    .dragEndCallback(() -> setState(() -> {
                                        this.draggingIndex = -1;
                                        this.updatePaneSizes(children, numDividers);
                                    }))
                                    .cursorStyleSupplier((x, y) -> CursorStyle.forDraggingAlong(axis, innerContext.instance().computeGlobalTransform())),
                                dividerVisual
                            );
                        }
                        widgets.add(new Constrain(dividerConstraints, divider).key(Key.of("divider-" + dividerIndex)));
                    }
                    prevRatio = nextRatio;
                }
                return new Flex(axis, MainAxisAlignment.START, CrossAxisAlignment.START, null, widgets);
            });
        }

        private void updatePaneSizes(List<SplitChild> children, int numDividers) {
            if (this.paneSizes == null) return;
            var prevRatio = 0d;
            for (var i = 0; i < children.size(); i++) {
                var nextRatio = i < numDividers ? this.controller.getRatio(i) : 1.0;
                this.paneSizes[i] = (nextRatio - prevRatio) * this.contentSpace;
                prevRatio = nextRatio;
            }
        }

        private double[] computeInitialRatiosNormalized(List<SplitChild> children) {
            var numDividers = children.size() - 1;
            var result = new double[numDividers];

            var totalWeight = 0d;
            for (var child : children) {
                if (child.size == null) totalWeight += child.weight;
            }

            var position = 0d;
            for (var i = 0; i < children.size(); i++) {
                var child = children.get(i);
                var ratio = child.size != null ? child.size : (child.weight / totalWeight);
                position += ratio;
                if (i < numDividers) result[i] = position;
            }

            return result;
        }

        private double[] computeInitialRatios(List<SplitChild> children) {
            var numDividers = children.size() - 1;
            var result = new double[numDividers];

            var fixedSpace = 0d;
            var totalWeight = 0d;

            for (var child : children) {
                if (child.size != null) {
                    fixedSpace += child.size;
                } else {
                    totalWeight += child.weight;
                }
            }

            var flexibleSpace = Math.max(0, this.contentSpace - fixedSpace);
            var position = 0d;

            for (var i = 0; i < children.size(); i++) {
                var child = children.get(i);
                var paneSize = child.size != null ? child.size : (child.weight / totalWeight) * flexibleSpace;
                position += paneSize;
                if (i < numDividers) result[i] = position / this.contentSpace;
            }

            return result;
        }

        private void moveDivider(int index, double pixelDelta) {
            this.dragRatio += pixelDelta / this.contentSpace;
            if (Boolean.TRUE.equals(this.resolvedStyle.pushDividers())) {
                var overflow = this.dragRatio - clampDivider(index, this.dragRatio);
                if (overflow != 0) pushDivider(overflow > 0 ? index + 1 : index - 1, overflow);
            }
            this.controller.setRatio(index, clampDivider(index, this.dragRatio));
        }

        private void pushDivider(int startIndex, double ratioOverflow) {
            var numDividers = this.widget().children.size() - 1;
            var step = ratioOverflow > 0 ? 1 : -1;
            var remaining = ratioOverflow;
            for (var i = startIndex; i >= 0 && i < numDividers && remaining != 0; i += step) {
                var target = this.controller.getRatio(i) + remaining;
                var clamped = clampDivider(i, target);
                this.controller.ratios[i] = clamped;
                remaining = target - clamped;
            }
        }

        //I hate that 73% of this method is variable declarations
        private double clampDivider(int index, double target) {
            var children = this.widget().children;
            var numDividers = children.size() - 1;
            var lower = index == 0 ? 0 : this.controller.getRatio(index - 1);
            var upper = index == numDividers - 1 ? 1 : this.controller.getRatio(index + 1);
            var left = children.get(index);
            var right = children.get(index + 1);
            var min = lower + left.minSize / this.contentSpace;
            var max = upper - right.minSize / this.contentSpace;
            if (left.maxSize != Double.POSITIVE_INFINITY) max = Math.min(max, lower + left.maxSize / this.contentSpace);
            if (right.maxSize != Double.POSITIVE_INFINITY) min = Math.max(min, upper - right.maxSize / this.contentSpace);
            return Mth.clamp(target, min, max);
        }

        private void recomputeRatiosOnResize(List<SplitChild> children, double newCS, int numDividers, SplitPaneStyle style) {
            var sizes = this.paneSizes.clone();

            var fixedTotal = 0d;
            var flexTotal = 0d;
            var hasFlex = false;

            for (var i = 0; i < children.size(); i++) {
                if (children.get(i).size != null) {
                    fixedTotal += sizes[i];
                } else {
                    flexTotal += sizes[i];
                    hasFlex = true;
                }
            }

            var availableForFlex = newCS - fixedTotal;

            if (availableForFlex < 0) {
                if (style.overflowPolicy() == ResizeDistribution.DONT) return;
                applyResizeDistribution(sizes, children, true, availableForFlex, style.overflowPolicy());
                fixedTotal = 0;
                for (var i = 0; i < children.size(); i++)
                    if (children.get(i).size != null) fixedTotal += sizes[i];
                availableForFlex = newCS - fixedTotal;
            }

            if (hasFlex) {
                var scale = flexTotal > 0 ? Math.max(0, availableForFlex) / flexTotal : 0;
                for (var i = 0; i < children.size(); i++)
                    if (children.get(i).size == null)
                        sizes[i] = sizes[i] * scale;
                if (scale > 0)
                    for (var i = 0; i < children.size(); i++)
                        if (children.get(i).size == null)
                            this.paneSizes[i] = sizes[i];
            } else if (fixedTotal < newCS) {
                applyResizeDistribution(sizes, children, true, newCS - fixedTotal, style.underflowPolicy());
            }

            for (var i = 0; i < children.size(); i++)
                if (children.get(i).size != null)
                    this.paneSizes[i] = sizes[i];

            var position = 0d;
            for (var i = 0; i < children.size(); i++) {
                position += sizes[i];
                if (i < numDividers) this.controller.ratios[i] = position / newCS;
            }
        }

        private void applyResizeDistribution(double[] sizes, List<SplitChild> children, boolean targetFixed, double delta, ResizeDistribution policy) {
            if (policy == ResizeDistribution.DONT) return;
            if (policy == ResizeDistribution.ALL) {
                var total = 0d;
                for (var i = 0; i < children.size(); i++)
                    if ((children.get(i).size != null) == targetFixed)
                        total += sizes[i];
                if (total > 0) {
                    var scale = (total + delta) / total;
                    for (var i = 0; i < children.size(); i++)
                        if ((children.get(i).size != null) == targetFixed)
                            sizes[i] = Math.max(0, sizes[i] * scale);
                }
            } else if (policy == ResizeDistribution.FIRST) {
                for (var i = 0; i < children.size(); i++) {
                    if ((children.get(i).size != null) == targetFixed) {
                        sizes[i] = Math.max(0, sizes[i] + delta);
                        break;
                    }
                }
            } else {
                for (var i = children.size() - 1; i >= 0; i--) {
                    if ((children.get(i).size != null) == targetFixed) {
                        sizes[i] = Math.max(0, sizes[i] + delta);
                        break;
                    }
                }
            }
        }
    }
}
