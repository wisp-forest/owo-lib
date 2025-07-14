package io.wispforest.owo.braid.widgets.slider;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.*;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class RawRangeSlider extends StatefulWidget {

    public final double minValue, maxValue;
    public final double min;
    public final double max;
    public final @Nullable Double step;
    public final LayoutAxis axis;

    public final RangeSliderCallback onChanged;
    public final Widget track;
    public final Widget handle;
    public final double handleSize;
    public final Widget rangeIndicator;

    public RawRangeSlider(
        double minValue,
        double maxValue,
        double min,
        double max,
        @Nullable Double step,
        LayoutAxis axis,
        RangeSliderCallback onChanged,
        Widget track,
        Widget handle,
        double handleSize,
        Widget rangeIndicator
    ) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.min = min;
        this.max = max;
        this.step = step;
        this.axis = axis;
        this.onChanged = onChanged;
        this.track = track;
        this.handle = handle;
        this.handleSize = handleSize;
        this.rangeIndicator = rangeIndicator;
    }

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class State extends WidgetState<RawRangeSlider> {

        protected @Nullable Handle grabbedHandle = null;

        @Override
        public Widget build(BuildContext context) {
            return new LayoutBuilder((innerContext, constraints) -> {
                var normalizedMin = (this.discretize(this.widget().minValue) - this.widget().min) / (this.widget().max - this.widget().min);
                var normalizedMax = (this.discretize(this.widget().maxValue) - this.widget().min) / (this.widget().max - this.widget().min);

                var rangeExtent = Math.ceil((constraints.maxOnAxis(this.widget().axis) - this.widget().handleSize * 2) * (normalizedMax - normalizedMin));

                return new Center(
                    new MouseArea(
                        widget -> widget
                            //TODO: decide what to do with buttons here
                            .clickCallback((x, y, button) -> {
                                this.grabbedHandle = this.handleAt(constraints, x, y);
                                this.setAbsolute(constraints, x, y);
                                return true;
                            })
                            .dragCallback((x, y, dx, dy) -> {
                                if (this.grabbedHandle == Handle.BOTH) {
                                    this.dragRange(constraints, dx, dy);
                                } else {
                                    this.setAbsolute(constraints, x, y);
                                }
                            })
                            .dragEndCallback(() -> {
                                this.grabbedHandle = null;
                                this.accumulatedDx = 0;
                                this.accumulatedDy = 0;
                            })
                            .cursorStyleSupplier((x, y) -> {
                                if (this.handleAt(constraints, x, y) == Handle.BOTH) {
                                    return CursorStyle.MOVE;
                                } else {
                                    return CursorStyle.HAND;
                                }
                            }),
                        new Stack(
                            this.widget().axis.choose(Alignment.LEFT, Alignment.TOP),
                            new Sized(
                                constraints.maxWidth(),
                                constraints.maxHeight(),
                                this.widget().track
                            ),
                            new Padding(
                                this.widget().axis.chooseCompute(
                                    () -> Insets.left(this.widget().handleSize + Math.floor((constraints.maxWidth() - this.widget().handleSize * 2) * normalizedMin) - 1),
                                    () -> Insets.top(this.widget().handleSize + Math.floor((constraints.maxHeight() - this.widget().handleSize * 2) * normalizedMin) - 1)
                                ),
                                new Center(
                                    1.0, null,
                                    this.widget().axis.chooseCompute(
                                        () -> new Sized(
                                            rangeExtent + 2,
                                            constraints.maxHeight(),
                                            this.widget().rangeIndicator
                                        ),
                                        () -> new Sized(
                                            constraints.maxWidth(),
                                            rangeExtent + 2,
                                            this.widget().rangeIndicator
                                        )
                                    )
                                )
                            ),
                            new Padding(
                                this.widget().axis.chooseCompute(
                                    () -> Insets.left(Math.floor((constraints.maxWidth() - this.widget().handleSize * 2) * normalizedMin)),
                                    () -> Insets.top(Math.floor((constraints.maxHeight() - this.widget().handleSize * 2) * normalizedMin))
                                ),
                                this.widget().axis.chooseCompute(
                                    () -> new Sized(
                                        this.widget().handleSize,
                                        constraints.maxHeight(),
                                        this.widget().handle
                                    ),
                                    () -> new Sized(
                                        constraints.maxWidth(),
                                        this.widget().handleSize,
                                        this.widget().handle
                                    )
                                )
                            ),
                            new Padding(
                                this.widget().axis.chooseCompute(
                                    () -> Insets.left(this.widget().handleSize + Math.floor((constraints.maxWidth() - this.widget().handleSize * 2) * normalizedMax)),
                                    () -> Insets.top(this.widget().handleSize + Math.floor((constraints.maxHeight() - this.widget().handleSize * 2) * normalizedMax))
                                ),
                                this.widget().axis.chooseCompute(
                                    () -> new Sized(
                                        this.widget().handleSize,
                                        constraints.maxHeight(),
                                        this.widget().handle
                                    ),
                                    () -> new Sized(
                                        constraints.maxWidth(),
                                        this.widget().handleSize,
                                        this.widget().handle
                                    )
                                )
                            )
                        )
                    )
                );
            });
        }

        protected double normalizedValueAt(Constraints constraints, double x, double y, @Nullable Handle grabbedHandle) {
            var offset = switch (grabbedHandle) {
                case MAX -> -this.widget().handleSize;
                case null -> -this.widget().handleSize / 2;
                default -> 0;
            };

            return MathHelper.clamp(
                (this.widget().axis.choose(x, y) - (this.widget().handleSize / 2) + offset) / (constraints.maxOnAxis(this.widget().axis) - this.widget().handleSize * 2),
                0,
                1
            );
        }

        protected Handle handleAt(Constraints constraints, double x, double y) {
            var normalizedValue = this.widget().min + this.normalizedValueAt(constraints, x, y, null) * (this.widget().max - this.widget().min);

            if (normalizedValue <= this.widget().minValue) {
                return Handle.MIN;
            }

            if (normalizedValue >= this.widget().maxValue) {
                return Handle.MAX;
            }

            return Handle.BOTH;
        }

        protected void setAbsolute(Constraints constraints, double x, double y) {
            var normalizedValue = this.normalizedValueAt(constraints, x, y, this.grabbedHandle);

            var newMinValue = this.grabbedHandle == Handle.MIN
                ? Math.min(this.discretize(this.widget().min + normalizedValue * (this.widget().max - this.widget().min)), this.widget().maxValue)
                : this.widget().minValue;

            var newMaxValue = this.grabbedHandle == Handle.MAX
                ? Math.max(this.discretize(this.widget().min + normalizedValue * (this.widget().max - this.widget().min)), this.widget().minValue)
                : this.widget().maxValue;

            this.widget().onChanged.accept(newMinValue, newMaxValue);
        }

        private double accumulatedDx = 0, accumulatedDy = 0;

        protected void dragRange(Constraints constraints, double dx, double dy) {
            var effectiveDx = dx < 0 ? Math.min(0, dx + accumulatedDx) : Math.max(0, dx + accumulatedDx);
            var effectiveDy = dy < 0 ? Math.min(0, dy + accumulatedDy) : Math.max(0, dy + accumulatedDy);

            var delta = this.widget().axis.choose(effectiveDx, effectiveDy) / (constraints.maxOnAxis(this.widget().axis) - this.widget().handleSize * 2)
                * (this.widget().max - this.widget().min);

            if (this.widget().minValue + delta < this.widget().min) {
                delta = this.widget().min - this.widget().minValue;
            }

            if (this.widget().maxValue + delta > this.widget().max) {
                delta = this.widget().max - this.widget().maxValue;
            }

            if (this.widget().step != null) {
                if (this.discretize(this.widget().minValue + delta) == this.widget().minValue) {
                    this.accumulatedDx += dx;
                    this.accumulatedDy += dy;

                    return;
                } else {
                    var normalizedStepSize = this.widget().step / (this.widget().max - this.widget().min);
                    var consumed = normalizedStepSize * (constraints.maxOnAxis(this.widget().axis) - this.widget().handleSize * 2);

                    this.accumulatedDx = dx > 0 ? this.accumulatedDx + dx - consumed : this.accumulatedDx + dx + consumed;
                    this.accumulatedDy = dx > 0 ? this.accumulatedDy + dy - consumed : this.accumulatedDy + dy + consumed;
                }
            }

            this.widget().onChanged.accept(
                this.discretize(this.widget().minValue + delta),
                this.discretize(this.widget().maxValue + delta)
            );
        }

        protected double discretize(double value) {
            var step = this.widget().step;
            if (step == null) return value;

            return Math.round(value / step) * step;
        }

        protected enum Handle {
            MIN, MAX, BOTH;
        }
    }

    @FunctionalInterface
    public interface RangeSliderCallback {
        void accept(double min, double max);
    }
}
