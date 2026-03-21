package io.wispforest.owo.braid.widgets.splitpane;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import org.jetbrains.annotations.Nullable;

public record SplitPaneStyle(
    @Nullable Double dividerThickness,
    @Nullable DividerBuilder dividerBuilder,
    @Nullable ResizeDistribution overflowPolicy,
    @Nullable ResizeDistribution underflowPolicy,
    @Nullable Boolean preserveSizes,
    @Nullable Boolean pushDividers
) {
    public static final SplitPaneStyle DEFAULT = new SplitPaneStyle(null, null, null, null, null, null);

    public SplitPaneStyle overriding(SplitPaneStyle other) {
        return new SplitPaneStyle(
            this.dividerThickness != null ? this.dividerThickness : other.dividerThickness,
            this.dividerBuilder != null ? this.dividerBuilder : other.dividerBuilder,
            this.overflowPolicy != null ? this.overflowPolicy : other.overflowPolicy,
            this.underflowPolicy != null ? this.underflowPolicy : other.underflowPolicy,
            this.preserveSizes != null ? this.preserveSizes : other.preserveSizes,
            this.pushDividers != null ? this.pushDividers : other.pushDividers
        );
    }

    //TODO: decide defaults for over/under flow, thickness and push
    public SplitPaneStyle fillDefaults() {
        return new SplitPaneStyle(
            this.dividerThickness != null ? this.dividerThickness : 2,
            this.dividerBuilder != null ? this.dividerBuilder : dragging -> new Box(Color.WHITE),
            this.overflowPolicy != null ? this.overflowPolicy : ResizeDistribution.LARGEST,
            this.underflowPolicy != null ? this.underflowPolicy : ResizeDistribution.SMALLEST,
            this.preserveSizes != null ? this.preserveSizes : true,
            this.pushDividers != null ? this.pushDividers : false
        );
    }

    @FunctionalInterface
    public interface DividerBuilder {
        Widget build(boolean dragging);
    }
}
