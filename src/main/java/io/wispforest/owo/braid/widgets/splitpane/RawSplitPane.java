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

import static com.mojang.math.Constants.EPSILON;

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
        private boolean firstDrag = false;
        private double[] paneSizes;
        private SplitPaneStyle resolvedStyle = SplitPaneStyle.DEFAULT.fillDefaults();

        @Override
        public void init() {
            var widget = this.widget();
            this.controller = Objects.requireNonNullElseGet(
                widget.controller,
                () -> {
                    var numDividers = widget.children.size() - 1;
                    var result = new double[numDividers];

                    var totalWeight = 0d;
                    for (var child : widget.children) {
                        if (child.size == null) totalWeight += child.weight;
                    }

                    var position = 0d;
                    for (var i = 0; i < widget.children.size(); i++) {
                        var child = widget.children.get(i);
                        var ratio = child.size != null ? child.size : (child.weight / totalWeight);
                        position += ratio;
                        if (i < numDividers) result[i] = position;
                    }

                    return new SplitController(result);
                }
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

                this.contentSpace = Math.floor(constraints.maxOnAxis(axis) - numDividers * dividerThickness);
                var crossSize = constraints.maxOnAxis(axis.opposite());

                if (this.contentSpace > 0) {
                    if (this.ratiosInitialized && this.contentSpace != this.prevContentSpace && this.draggingIndex == -1 && this.widget().controller == null) {
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

                        var availableForFlex = this.contentSpace - fixedTotal;

                        if (availableForFlex < 0) {
                            applyResizeDistribution(sizes, children, availableForFlex, style.overflowPolicy());
                            fixedTotal = 0;
                            for (var i = 0; i < children.size(); i++)
                                if (children.get(i).size != null) fixedTotal += sizes[i];
                            availableForFlex = this.contentSpace - fixedTotal;
                        }

                        if (hasFlex) {
                            var clampedFlex = Math.max(0, availableForFlex);
                            if (flexTotal > 0) {
                                var scale = clampedFlex / flexTotal;
                                for (var i = 0; i < children.size(); i++)
                                    if (children.get(i).size == null)
                                        sizes[i] = sizes[i] * scale;
                            } else {
                                var flexCount = 0;
                                for (var child : children) if (child.size == null) flexCount++;
                                var equalShare = clampedFlex / flexCount;
                                for (var i = 0; i < children.size(); i++)
                                    if (children.get(i).size == null)
                                        sizes[i] = equalShare;
                            }
                        } else if (fixedTotal < this.contentSpace) {
                            applyResizeDistribution(sizes, children, this.contentSpace - fixedTotal, style.underflowPolicy());
                        }

                        if (!Boolean.TRUE.equals(style.preserveSizes()))
                            for (var i = 0; i < children.size(); i++)
                                this.paneSizes[i] = sizes[i];

                        var position = 0d;
                        for (var i = 0; i < children.size(); i++) {
                            position += sizes[i];
                            if (i < numDividers) this.controller.ratios[i] = position / this.contentSpace;
                        }
                    }
                    this.prevContentSpace = this.contentSpace;
                }

                double[] ratios;
                if (!this.ratiosInitialized && this.contentSpace > 0) {
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

                    ratios = result;
                    System.arraycopy(ratios, 0, this.controller.ratios, 0, numDividers);
                    this.paneSizes = new double[children.size()];
                    var prevRatio = 0d;
                    for (var i = 0; i < children.size(); i++) {
                        var nextRatio = i < numDividers ? ratios[i] : 1.0;
                        this.paneSizes[i] = i < children.size() - 1
                            ? Math.floor(nextRatio * this.contentSpace) - Math.floor(prevRatio * this.contentSpace)
                            : this.contentSpace - Math.floor(prevRatio * this.contentSpace);
                        prevRatio = nextRatio;
                    }
                    this.ratiosInitialized = true;
                } else {
                    ratios = new double[numDividers];
                    for (int i = 0; i < numDividers; i++) {
                        ratios[i] = this.ratiosInitialized ? this.controller.getRatio(i) : (double) (i + 1) / children.size();
                    }
                }

                var widgets = new ArrayList<Widget>();
                var prevRatio = 0d;

                for (var i = 0; i < children.size(); i++) {
                    var child = children.get(i).child;
                    var nextRatio = i < numDividers ? ratios[i] : 1.0;
                    var paneSize = i < children.size() - 1
                        ? Math.floor(nextRatio * this.contentSpace) - Math.floor(prevRatio * this.contentSpace)
                        : this.contentSpace - Math.floor(prevRatio * this.contentSpace);

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
                                        this.firstDrag = true;
                                    }))
                                    .dragCallback((x, y, dx, dy) -> this.moveDivider(dividerIndex, axis.choose(dx, dy)))
                                    .dragEndCallback(() -> setState(() -> updatePaneSizes(children)))
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

        private void updatePaneSizes(List<SplitChild> children) {
            this.draggingIndex = -1;
            if (this.paneSizes == null) return;
            var prevRatio1 = 0d;
            for (var j = 0; j < children.size(); j++) {
                var nextRatio1 = j < children.size() - 1 ? this.controller.getRatio(j) : 1.0;
                this.paneSizes[j] = j < children.size() - 1
                    ? Math.floor(nextRatio1 * this.contentSpace) - Math.floor(prevRatio1 * this.contentSpace)
                    : this.contentSpace - Math.floor(prevRatio1 * this.contentSpace);
                prevRatio1 = nextRatio1;
            }
        }

        private void moveDivider(int clickedIndex, double pixelDelta) {
            var children = this.widget().children;
            var numDividers = children.size() - 1;
            var cs = this.contentSpace;
            var push = Boolean.TRUE.equals(this.resolvedStyle.pushDividers());

            if (this.firstDrag && !push && pixelDelta != 0) {
                this.firstDrag = false;
                var controllerRatios = this.controller.ratios;
                var step = pixelDelta < 0 ? -1 : 1;
                var result = clickedIndex;
                for (var j = clickedIndex; j + step >= 0 && j + step < children.size() - 1; j += step) {
                    if ((controllerRatios[Math.max(j, j + step)] - controllerRatios[Math.min(j, j + step)]) * cs > EPSILON) break;
                    result = j + step;
                }
                this.draggingIndex = result;
                this.dragRatio = this.controller.getRatio(this.draggingIndex) + (clickedIndex - this.draggingIndex) * this.resolvedStyle.dividerThickness() / cs;
            }

            var index = this.draggingIndex;
            this.dragRatio += pixelDelta / cs;

            var lower = push ? 0d : (index == 0 ? 0 : this.controller.getRatio(index - 1));
            var upper = push ? 1d : (index == numDividers - 1 ? 1 : this.controller.getRatio(index + 1));

            if (push) {
                for (var i = 0; i <= index; i++) lower += children.get(i).minSize / cs;
                for (var i = index + 1; i < children.size(); i++) upper -= children.get(i).minSize / cs;
            } else {
                lower += children.get(index).minSize / cs;
                upper -= children.get(index + 1).minSize / cs;
            }

            var left = children.get(index);
            var right = children.get(index + 1);
            if (!push && left.maxSize != Double.POSITIVE_INFINITY)
                upper = Math.min(upper, (index == 0 ? 0 : this.controller.getRatio(index - 1)) + left.maxSize / cs);
            if (!push && right.maxSize != Double.POSITIVE_INFINITY)
                lower = Math.max(lower, (index == numDividers - 1 ? 1 : this.controller.getRatio(index + 1)) - right.maxSize / cs);

            this.controller.setRatio(index, Mth.clamp(this.dragRatio, lower, upper));

            if (push) {
                for (var i = index - 1; i >= 0; i--) {
                    var max = this.controller.ratios[i + 1] - children.get(i + 1).minSize / cs;
                    if (this.controller.ratios[i] <= max) break;
                    this.controller.ratios[i] = max;
                }
                for (var i = index + 1; i < numDividers; i++) {
                    var min = this.controller.ratios[i - 1] + children.get(i).minSize / cs;
                    if (this.controller.ratios[i] >= min) break;
                    this.controller.ratios[i] = min;
                }
            }
        }

        private void applyResizeDistribution(double[] sizes, List<SplitChild> children, double delta, ResizeDistribution policy) {
            var fixedSizes = new double[children.size()];
            for (var i = 0; i < children.size(); i++) if (children.get(i).size != null) fixedSizes[i] = sizes[i];

            var clamped = new boolean[children.size()];
            var remaining = policy.distribute(fixedSizes, delta);

            while (Math.abs(remaining) > EPSILON) {
                for (var i = 0; i < children.size(); i++) {
                    if (children.get(i).size == null) continue;
                    var child = children.get(i);
                    var original = fixedSizes[i];
                    var clampedSize = Mth.clamp(original, child.minSize, child.maxSize);
                    remaining += original - clampedSize;
                    fixedSizes[i] = clampedSize;
                    clamped[i] |= clampedSize != original;
                }

                var unclamped = 0;
                for (var i = 0; i < children.size(); i++)
                    if (children.get(i).size != null && !clamped[i]) unclamped++;
                if (unclamped == 0) break;

                var redistribution = new double[children.size()];
                for (var i = 0; i < children.size(); i++)
                    if (children.get(i).size != null && !clamped[i]) redistribution[i] = fixedSizes[i];
                remaining = policy.distribute(redistribution, remaining);
                for (var i = 0; i < children.size(); i++)
                    if (children.get(i).size != null && !clamped[i]) fixedSizes[i] = redistribution[i];
            }

            for (var i = 0; i < children.size(); i++) if (children.get(i).size != null) sizes[i] = fixedSizes[i];
        }

    }
}
