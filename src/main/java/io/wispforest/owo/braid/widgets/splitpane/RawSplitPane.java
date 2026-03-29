package io.wispforest.owo.braid.widgets.splitpane;

import com.google.common.base.Preconditions;
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
        //FIXME: this error message kinda sucks
        // Idk how to explain the actual problem
        if (controller != null) Preconditions.checkArgument(
            (controller.ratios != null && controller.ratios.length == children.size() - 1),
            "A SplitPane's controller must have exactly one less ratio than the number of panes: (%s ratio(s), %s pane(s))",
            controller.ratios == null ? 0 : controller.ratios.length,
            children.size()
        );
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

        protected SplitController controller;
        protected final Runnable controllerListener = () -> setState(() -> {});

        protected int draggingIndex = -1;
        protected double contentSpace = 0;
        protected double prevContentSpace = -1;
        protected double dragRatio = 0;
        protected boolean ratiosInitialized = false;
        protected boolean firstDrag = false;
        protected double[] paneSizes;
        protected SplitPaneStyle resolvedStyle;
        protected CursorStyle draggingCursorStyle = null;

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
            if (this.widget().style != oldWidget.style) this.prevContentSpace = -1;
        }

        @Override
        public void dispose() {
            this.controller.removeListener(this.controllerListener);
        }

        @Override
        public Widget build(BuildContext outerContext) {
            var widget = this.widget();
            this.resolvedStyle = (widget.style != null ? widget.style : SplitPaneStyle.DEFAULT).fillDefaults();
            var style = this.resolvedStyle;

            return new LayoutBuilder((context, constraints) -> {
                var axis = widget.axis;
                var children = this.widget().children;
                var numDividers = children.size() - 1;
                var dividerThickness = style.dividerThickness();

                //TODO: what do we do about intellij complaining that dividerThickness could be null (it won't because of defaults)
                this.contentSpace = Math.floor(constraints.maxOnAxis(axis) - numDividers * dividerThickness);
                var crossSize = constraints.maxOnAxis(axis.opposite());

                if (this.contentSpace > 0) {
                    if (!this.ratiosInitialized)
                        this.initializeRatios();
                    else if (this.contentSpace != this.prevContentSpace && this.draggingIndex == -1 && this.widget().controller == null)
                        this.recomputeRatios();
                    this.prevContentSpace = this.contentSpace;
                }

                var widgets = new ArrayList<Widget>();
                var prevRatio = 0d;

                for (var i = 0; i < children.size(); i++) {
                    var child = children.get(i).child;
                    var nextRatio = i < numDividers
                        ? (this.ratiosInitialized ? this.controller.getRatio(i) : (i + 1.0) / children.size())
                        : 1;
                    var paneSize = i < children.size() - 1
                        ? Math.floor(nextRatio * this.contentSpace) - Math.floor(prevRatio * this.contentSpace)
                        : this.contentSpace - Math.floor(prevRatio * this.contentSpace);

                    var childConstraints = Constraints.tight(axis.createSize(paneSize, crossSize));
                    widgets.add(new Constrain(childConstraints, child).key(child.key()));

                    if (i < numDividers) {
                        var dividerIndex = i;
                        var dragging = this.draggingIndex == dividerIndex;
                        var divider = style.dividerBuilder().build(dragging);
                        var dividerConstraints = Constraints.tight(axis.createSize(dividerThickness, crossSize));

                        if (widget.enabled) {
                            divider = new MouseArea(
                                w -> w
                                    .dragStartCallback((button, modifiers) -> setState(() -> {
                                        if (button != 0) return;
                                        this.draggingIndex = dividerIndex;
                                        this.dragRatio = this.controller.getRatio(dividerIndex);
                                        this.firstDrag = true;
                                    }))
                                    .dragCallback((_, _, dx, dy) -> this.moveDivider(dividerIndex, axis.choose(dx, dy)))
                                    .dragEndCallback(() -> setState(() -> updatePaneSizes(children)))
                                    .cursorStyleSupplier((_, _) -> {
                                        if (this.draggingCursorStyle == null)
                                            this.draggingCursorStyle = CursorStyle.forDraggingAlong(axis, context.instance().computeGlobalTransform());
                                        return this.draggingCursorStyle;
                                    }),
                                divider
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
            var previous = 0d;
            for (var j = 0; j < children.size(); j++) {
                var next = j < children.size() - 1 ? this.controller.getRatio(j) : 1;
                this.paneSizes[j] = (j < children.size() - 1
                    ? Math.floor(next * this.contentSpace)
                    : this.contentSpace) - Math.floor(previous * this.contentSpace);
                previous = next;
            }
        }

        private void initializeRatios() {
            var children = this.widget().children;
            var numDividers = children.size() - 1;

            if (this.widget().controller != null) {
                this.paneSizes = new double[children.size()];
                updatePaneSizes(children);
                this.ratiosInitialized = true;
                return;
            }

            var totalFixed = 0d;
            var totalWeight = 0d;

            for (var child : children) {
                if (child.size != null) totalFixed += child.size;
                else totalWeight += child.weight;
            }

            var flexSpace = Math.max(0, this.contentSpace - totalFixed);
            var sizes = new double[children.size()];
            for (var i = 0; i < children.size(); i++) {
                var child = children.get(i);
                sizes[i] = child.size != null ? child.size : child.weight / totalWeight * flexSpace;
            }

            adjustSizes(sizes);

            this.paneSizes = new double[children.size()];
            var position = 0d;
            for (var i = 0; i < children.size(); i++) {
                var start = Math.floor(position);
                position += sizes[i];
                this.paneSizes[i] = (i < numDividers ? Math.floor(position) : this.contentSpace) - start;
                if (i < numDividers) this.controller.ratios[i] = position / this.contentSpace;
            }
            this.ratiosInitialized = true;
        }

        private void recomputeRatios() {
            var children = this.widget().children;
            var style = this.resolvedStyle;
            var sizes = this.paneSizes.clone();

            adjustSizes(sizes);

            if (!Boolean.TRUE.equals(style.preserveSizes()))
                System.arraycopy(sizes, 0, this.paneSizes, 0, children.size());

            var position = 0d;
            for (var i = 0; i < children.size(); i++) {
                position += sizes[i];
                if (i < children.size() - 1) this.controller.ratios[i] = position / this.contentSpace;
            }
        }

        private void adjustSizes(double[] sizes) {
            var children = this.widget().children;
            var style = this.resolvedStyle;

            var totalFixed = 0d;
            var totalFlex = 0d;
            var anyFlex = false;

            for (var i = 0; i < children.size(); i++) {
                if (children.get(i).size != null) {
                    totalFixed += sizes[i];
                } else {
                    totalFlex += sizes[i];
                    anyFlex = true;
                }
            }

            var flexSpace = this.contentSpace - totalFixed;

            //If there's not enough space to satisfy fixed sizes, apply overflow policy
            if (flexSpace < 0) {
                applyResizeDistribution(sizes, children, flexSpace, style.overflowPolicy());
                totalFixed = 0;
                for (var i = 0; i < children.size(); i++)
                    if (children.get(i).size != null) totalFixed += sizes[i];
                flexSpace = this.contentSpace - totalFixed;
            }

            //If there's extra space and flexible panes we can give it to, do that
            if (anyFlex) {
                var clampedFlex = Math.max(0, flexSpace);
                if (totalFlex > 0) {
                    var scale = clampedFlex / totalFlex;
                    for (var i = 0; i < children.size(); i++)
                        if (children.get(i).size == null) sizes[i] *= scale;
                } else {
                    var flexCount = 0;
                    for (var child : children) if (child.size == null) flexCount++;
                    var equalShare = clampedFlex / flexCount;
                    for (var i = 0; i < children.size(); i++)
                        if (children.get(i).size == null) sizes[i] = equalShare;
                }
                //If there's more space than fixed panes will take, apply underflow policy
            } else if (totalFixed < this.contentSpace) {
                applyResizeDistribution(sizes, children, this.contentSpace - totalFixed, style.underflowPolicy());
            }
        }

        private void moveDivider(int clickedIndex, double pixelDelta) {
            var children = this.widget().children;
            var numDividers = children.size() - 1;
            var push = Boolean.TRUE.equals(this.resolvedStyle.pushDividers());

            //If its unclear what divider is being dragged, figure it out
            if (this.firstDrag && !push && pixelDelta != 0) {
                this.firstDrag = false;
                var controllerRatios = this.controller.ratios;
                var step = pixelDelta < 0 ? -1 : 1;
                var result = clickedIndex;
                for (var j = clickedIndex; j + step >= 0 && j + step < children.size() - 1; j += step) {
                    if ((controllerRatios[Math.max(j, j + step)] - controllerRatios[Math.min(j, j + step)]) * this.contentSpace > EPSILON) break;
                    result = j + step;
                }
                this.draggingIndex = result;
                this.dragRatio = this.controller.getRatio(this.draggingIndex) + (clickedIndex - this.draggingIndex) * this.resolvedStyle.dividerThickness() / this.contentSpace;
            }

            var index = this.draggingIndex;
            this.dragRatio += pixelDelta / this.contentSpace;

            //Figure out the bounds of where the divider can be moved
            var lower = push ? 0d : (index == 0 ? 0 : this.controller.getRatio(index - 1)) + children.get(index).minSize / this.contentSpace;
            var upper = push ? 1d : (index == numDividers - 1 ? 1 : this.controller.getRatio(index + 1)) - children.get(index + 1).minSize / this.contentSpace;

            //If in push mode, accumulate the minimum sizes of panes on either sides
            if (push) {
                for (var i = 0; i <= index; i++) lower += children.get(i).minSize / this.contentSpace;
                for (var i = index + 1; i < children.size(); i++) upper -= children.get(i).minSize / this.contentSpace;
                // If not in push mode, respect max sizes of adjacent panes
            } else {
                var left = children.get(index);
                var right = children.get(index + 1);
                if (left.maxSize != Double.POSITIVE_INFINITY)
                    upper = Math.min(upper, (index == 0 ? 0 : this.controller.getRatio(index - 1)) + left.maxSize / this.contentSpace);
                if (right.maxSize != Double.POSITIVE_INFINITY)
                    lower = Math.max(lower, (index == numDividers - 1 ? 1 : this.controller.getRatio(index + 1)) - right.maxSize / this.contentSpace);
            }

            this.controller.setRatio(index, Mth.clamp(this.dragRatio, lower, upper));

            //In push mode, push other dividers
            if (push) {
                for (var i = index - 1; i >= 0; i--) {
                    var max = this.controller.ratios[i + 1] - children.get(i + 1).minSize / this.contentSpace;
                    if (this.controller.ratios[i] <= max) break;
                    this.controller.ratios[i] = max;
                }
                for (var i = index + 1; i < numDividers; i++) {
                    var min = this.controller.ratios[i - 1] + children.get(i).minSize / this.contentSpace;
                    if (this.controller.ratios[i] >= min) break;
                    this.controller.ratios[i] = min;
                }
            }
        }

        private void applyResizeDistribution(double[] sizes, List<SplitChild> children, double delta, ResizeDistribution policy) {
            var clamped = new boolean[children.size()];

            var remaining = delta;
            while (Math.abs(remaining) > EPSILON) {
                var indices = buildIndices(sizes, children, clamped, delta);
                if (indices.length == 0) break;

                var compact = new double[indices.length];
                for (var j = 0; j < indices.length; j++) compact[j] = sizes[indices[j]];
                remaining = policy.distribute(compact, remaining);
                for (var j = 0; j < indices.length; j++) sizes[indices[j]] = compact[j];

                for (var i = 0; i < children.size(); i++) {
                    if (children.get(i).size == null) continue;
                    var child = children.get(i);
                    var original = sizes[i];
                    var clampedSize = Mth.clamp(original, child.minSize, child.maxSize);
                    remaining += original - clampedSize;
                    sizes[i] = clampedSize;
                    clamped[i] |= clampedSize != original;
                }
            }
        }

        private int[] buildIndices(double[] sizes, List<SplitChild> children, boolean[] clamped, double delta) {
            var count = 0;
            for (var i = 0; i < children.size(); i++) {
                if (children.get(i).size == null || clamped[i]) continue;
                if (delta < 0 && sizes[i] < EPSILON) continue;
                count++;
            }
            var indices = new int[count];
            var j = 0;
            for (var i = 0; i < children.size(); i++) {
                if (children.get(i).size == null || clamped[i]) continue;
                if (delta < 0 && sizes[i] < EPSILON) continue;
                indices[j++] = i;
            }
            return indices;
        }

    }
}
