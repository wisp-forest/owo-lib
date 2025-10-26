package io.wispforest.owo.braid.widgets.scroll;

import com.google.common.base.Preconditions;
import io.wispforest.owo.braid.core.Aabb2d;
import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.braid.core.CompoundListenable;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Clip;
import io.wispforest.owo.braid.widgets.basic.ListenableBuilder;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import java.util.Objects;

public class Scrollable extends StatefulWidget {

    public final boolean horizontal;
    public final boolean vertical;
    public final @Nullable ScrollController horizontalController;
    public final @Nullable ScrollController verticalController;
    public final @Nullable ScrollAnimationSettings animationSettings;
    public final Widget child;

    public Scrollable(
        boolean horizontal,
        boolean vertical,
        @Nullable ScrollController horizontalController,
        @Nullable ScrollController verticalController,
        @Nullable ScrollAnimationSettings animationSettings,
        Widget child
    ) {
        this.horizontal = horizontal;
        this.vertical = vertical;
        this.horizontalController = horizontalController;
        this.verticalController = verticalController;
        this.animationSettings = animationSettings;
        this.child = child;
    }

    @Override
    public WidgetState<Scrollable> createState() {
        return new State();
    }

    // ---

    public static void reveal(BuildContext context) {
        reveal(context, Insets.none());
    }

    public static void reveal(BuildContext context, Insets padding) {
        of(context).reveal(context, padding);
    }

    public static void revealAabb(BuildContext context, Aabb2d box) {
        of(context).revealAabb(context, box);
    }

    public static @Nullable State maybeOf(BuildContext context) {
        var provider = context.getAncestor(ScrollableProvider.class);
        return provider != null ? provider.state : null;
    }

    public static State of(BuildContext context) {
        var state = maybeOf(context);
        Preconditions.checkNotNull(state, "attempted to look up the enclosing scrollable state without one being present");

        return state;
    }

    // ---

    public static class State extends WidgetState<Scrollable> {

        protected final CompoundListenable listenable = new CompoundListenable();

        protected ScrollController horizontalController;
        protected ScrollController verticalController;

        private void reveal(BuildContext context, Insets padding) {
            var transform = context.instance().transform;

            var matrix = new Matrix3x2f();
            transform.transformToWidget(matrix);

            var box = new Aabb2d(
                transform.x() - padding.left(),
                transform.y() - padding.top(),
                transform.width() + padding.horizontal(),
                transform.height() + padding.vertical()
            ).transform(matrix);

            revealAabb(context, box);
        }

        // TODO: support animations
        private void revealAabb(BuildContext context, Aabb2d box) {
            var scrollInstance = this.context().instance();
            var revealInstance = context.instance();

            var transform = revealInstance.computeTransformFrom(scrollInstance).invert().translate(
                this.horizontalController != null ? (float) this.horizontalController.offset : 0,
                this.verticalController != null ? (float) this.verticalController.offset : 0
            );

            box.transform(transform);

            if (this.horizontalController != null) {
                if (box.minX() < this.horizontalController.offset) {
                    this.horizontalController.jumpTo(box.minX());
                }

                if (box.maxX() > scrollInstance.transform.width() + this.horizontalController.offset) {
                    this.horizontalController.jumpTo(box.maxX() - scrollInstance.transform.width());
                }
            }

            if (this.verticalController != null) {
                if (box.minY() < this.verticalController.offset) {
                    this.verticalController.jumpTo(box.minY());
                }

                if (box.maxY() > scrollInstance.transform.height() + this.verticalController.offset) {
                    this.verticalController.jumpTo(box.maxY() - scrollInstance.transform.height());
                }
            }
        }

        @Override
        public void init() {
            this.horizontalController = this.widget().horizontal ? Objects.requireNonNullElse(this.widget().horizontalController, new ScrollController(this)) : null;
            this.verticalController = this.widget().vertical ? Objects.requireNonNullElse(this.widget().verticalController, new ScrollController(this)) : null;

            if (this.horizontalController != null) this.listenable.addChild(this.horizontalController);
            if (this.verticalController != null) this.listenable.addChild(this.verticalController);
        }

        @Override
        public void didUpdateWidget(Scrollable oldWidget) {
            this.listenable.clear();

            if (this.widget().horizontal) {
                if (this.widget().horizontalController != null) {
                    this.horizontalController = this.widget().horizontalController;
                } else if (this.horizontalController == null || this.horizontalController == oldWidget.horizontalController) {
                    this.horizontalController = new ScrollController(this);
                }

                this.listenable.addChild(this.horizontalController);
            } else {
                this.horizontalController = null;
            }

            if (this.widget().vertical) {
                if (this.widget().verticalController != null) {
                    this.verticalController = this.widget().verticalController;
                } else if (this.verticalController == null || this.verticalController == oldWidget.verticalController) {
                    this.verticalController = new ScrollController(this);
                }

                this.listenable.addChild(this.verticalController);
            } else {
                this.verticalController = null;
            }
        }

        @Override
        public Widget build(BuildContext context) {
            var widgetSettings = this.widget().animationSettings;
            var animationSettings = widgetSettings != null
                ? (widgetSettings != ScrollAnimationSettings.NO_ANIMATION ? widgetSettings : null)
                : DefaultScrollAnimationSettings.maybeOf(context);

            return new Clip(
                new MouseArea(
                    widget -> widget
                        .scrollCallback((horizontal, vertical) -> {
                            var verticalDelta = vertical * -15;
                            var horizontalDelta = horizontal * -15;

                            if (AppState.of(context).eventBinding.activeModifiers().shift()) {
                                if (this.widget().horizontal) {
                                    if (animationSettings != null) {
                                        this.horizontalController.animateBy(verticalDelta, animationSettings.duration(), animationSettings.easing());
                                    } else {
                                        this.horizontalController.jumpBy(verticalDelta);
                                    }
                                }
                            } else {
                                if (this.widget().vertical) {
                                    if (animationSettings != null) {
                                        this.verticalController.animateBy(verticalDelta, animationSettings.duration(), animationSettings.easing());
                                    } else {
                                        this.verticalController.jumpBy(verticalDelta);
                                    }
                                }
                            }

                            if (this.widget().horizontal) {
                                if (animationSettings != null) {
                                    this.horizontalController.animateBy(horizontalDelta, animationSettings.duration(), animationSettings.easing());
                                } else {
                                    this.horizontalController.jumpBy(horizontalDelta);
                                }
                            }
                            return true;
                        }),
                    new ListenableBuilder(
                        this.listenable,
                        (innerContext, child) -> new RawScrollView(
                            this.horizontalController,
                            this.verticalController,
                            new ScrollableProvider(this, child)
                        ),
                        this.widget().child
                    )
                )
            );
        }
    }
}

class ScrollableProvider extends InheritedWidget {

    public final Scrollable.State state;

    public ScrollableProvider(Scrollable.State state, Widget child) {
        super(child);
        this.state = state;
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        return false;
    }
}
