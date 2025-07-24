package io.wispforest.owo.braid.widgets.scroll;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;

public class RawScrollView extends SingleChildInstanceWidget {

    public final ScrollController horizontalController;
    public final ScrollController verticalController;

    public RawScrollView(
        @Nullable ScrollController horizontalController,
        @Nullable ScrollController verticalController,
        Widget child
    ) {
        super(child);
        this.horizontalController = horizontalController;
        this.verticalController = verticalController;
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance<RawScrollView> {

        protected double horizontalOffset, maxHorizontalOffset;
        protected double verticalOffset, maxVerticalOffset;

        public Instance(RawScrollView widget) {
            super(widget);

            this.horizontalOffset = this.widget.horizontalController != null ? this.widget.horizontalController.offset() : 0;
            this.maxHorizontalOffset = this.widget.horizontalController != null ? this.widget.horizontalController.maxOffset() : 0;
            this.verticalOffset = this.widget.verticalController != null ? this.widget.verticalController.offset() : 0;
            this.maxVerticalOffset = this.widget.verticalController != null ? this.widget.verticalController.maxOffset() : 0;
        }

        @Override
        public void setWidget(RawScrollView widget) {
            var horizontalOffset = this.widget.horizontalController != null ? this.widget.horizontalController.offset() : 0;
            var maxHorizontalOffset = this.widget.horizontalController != null ? this.widget.horizontalController.maxOffset() : 0;
            var verticalOffset = this.widget.verticalController != null ? this.widget.verticalController.offset() : 0;
            var maxVerticalOffset = this.widget.verticalController != null ? this.widget.verticalController.maxOffset() : 0;

            if (!(this.horizontalOffset == horizontalOffset
                && this.maxHorizontalOffset == maxHorizontalOffset
                && this.verticalOffset == verticalOffset
                && this.maxVerticalOffset == maxVerticalOffset)) {

                this.horizontalOffset = horizontalOffset;
                this.maxHorizontalOffset = maxHorizontalOffset;
                this.verticalOffset = verticalOffset;
                this.maxVerticalOffset = maxVerticalOffset;

                this.markNeedsLayout();
            }

            super.setWidget(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            var childSize = this.child.layout(
                Constraints.of(
                    constraints.minWidth(),
                    constraints.minHeight(),
                    this.widget.horizontalController != null ? Double.POSITIVE_INFINITY : constraints.maxWidth(),
                    this.widget.verticalController != null ? Double.POSITIVE_INFINITY : constraints.maxHeight()
                )
            );

            var selfSize = childSize.constrained(constraints);

            this.updateMaxOffset(this.widget.horizontalController, Math.max(0, childSize.width() - selfSize.width()));
            this.updateMaxOffset(this.widget.verticalController, Math.max(0, childSize.height() - selfSize.height()));

            this.child.transform.setX(-this.horizontalOffset);
            this.child.transform.setY(-this.verticalOffset);

            this.transform.setSize(selfSize);
        }

        /// Delay the actual invocation of scroll controller listeners until
        /// after the current layout cycle.
        ///
        /// This is important, because for one nobody could react to it anyways
        /// (since we are in the layout phase, the build phase for this frame
        /// is over) but *also* it actually breaks instances which descend from
        /// a layout builder. This happens because such a descendant would now
        /// mark itself dirty during the layout phase, but before the layout builder
        /// instance is marked clean. Thus, the `markNeedsLayout()` invocation on
        /// that layout builder instance gets swallowed and the widget is now stuck
        /// in improperly-rebuilt limbo until the layout builder happens to re-layout
        /// for other reasons. That is especially problematic because there is
        /// potential for this effect to mask legitimate rebuilds said descendant
        /// requires - it won't mark itself as needing a rebuild again because it
        /// is still dutifully waiting for such a rebuild to occur.
        private void updateMaxOffset(@Nullable ScrollController controller, double offset) {
            if (controller == null) return;

            if (controller.setMaxOffset(offset) && !controller.maxOffsetNotificationScheduled) {
                controller.maxOffsetNotificationScheduled = true;
                this.host().schedulePostLayoutCallback(controller::sendMaxOffsetNotification);
            }
        }

        @Override
        protected double measureIntrinsicWidth(double height) {
            return this.widget.horizontalController == null ? this.child.getIntrinsicWidth(height) : 0;
        }

        @Override
        protected double measureIntrinsicHeight(double width) {
            return this.widget.verticalController == null ? this.child.getIntrinsicHeight(width) : 0;
        }

        @Override
        protected OptionalDouble measureBaselineOffset() {
            var childBaseline = this.child != null ? this.child.getBaselineOffset() : OptionalDouble.empty();
            if (childBaseline.isEmpty()) return OptionalDouble.empty();

            return OptionalDouble.of(childBaseline.getAsDouble() + this.child.transform.y());
        }
    }
}
