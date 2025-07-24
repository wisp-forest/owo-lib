package io.wispforest.owo.braid.widgets.scroll;

import com.google.common.base.Preconditions;
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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.Objects;

public class Scrollable extends StatefulWidget {

    public final boolean horizontal;
    public final boolean vertical;
    public final @Nullable ScrollController horizontalController;
    public final @Nullable ScrollController verticalController;
    public final Widget child;

    public Scrollable(boolean horizontal, boolean vertical, @Nullable ScrollController horizontalController, @Nullable ScrollController verticalController, Widget child) {
        this.horizontal = horizontal;
        this.vertical = vertical;
        this.horizontalController = horizontalController;
        this.verticalController = verticalController;
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

    public static void revealAabb(BuildContext context, Box box) {
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
            var box = context.instance().transform.aabb();
            box = new Box(
                box.minX - padding.left(),
                box.minY - padding.top(),
                box.minZ,
                box.maxX + padding.right(),
                box.maxY + padding.bottom(),
                box.maxZ
            );

            revealAabb(context, box);
        }

        private void revealAabb(BuildContext context, Box box) {
            var scrollInstance = this.context().instance();
            var revealInstance = context.instance();

            var transform = revealInstance.computeTransformFrom(scrollInstance).invert().translate(
                this.horizontalController != null ? (float) this.horizontalController.offset : 0,
                this.verticalController != null ? (float) this.verticalController.offset : 0, 0
            );

            var min = new Vector4f((float) box.minX, (float) box.minY, (float) box.minZ, 1f).mul(transform);
            var max = new Vector4f((float) box.maxX, (float) box.maxY, (float) box.maxZ, 1f).mul(transform);

            var revealBox = new Box(min.x, min.y, min.z, max.x, max.y, max.z);

            if (this.horizontalController != null) {
                if (revealBox.minX < this.horizontalController.offset) {
                    this.horizontalController.setOffset(revealBox.minX);
                }

                if (revealBox.maxX > scrollInstance.transform.width() + this.horizontalController.offset) {
                    this.horizontalController.setOffset(revealBox.maxX - scrollInstance.transform.width());
                }
            }

            if (this.verticalController != null) {
                if (revealBox.minY < this.verticalController.offset) {
                    this.verticalController.setOffset(revealBox.minY);
                }

                if (revealBox.maxY > scrollInstance.transform.height() + this.verticalController.offset) {
                    this.verticalController.setOffset(revealBox.maxY - scrollInstance.transform.height());
                }
            }
        }

        @Override
        public void init() {
            this.horizontalController = this.widget().horizontal ? Objects.requireNonNullElse(this.widget().horizontalController, new ScrollController()) : null;
            this.verticalController = this.widget().vertical ? Objects.requireNonNullElse(this.widget().verticalController, new ScrollController()) : null;

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
                    this.horizontalController = new ScrollController();
                }

                this.listenable.addChild(this.horizontalController);
            } else {
                this.horizontalController = null;
            }

            if (this.widget().vertical) {
                if (this.widget().verticalController != null) {
                    this.verticalController = this.widget().verticalController;
                } else if (this.verticalController == null || this.verticalController == oldWidget.verticalController) {
                    this.verticalController = new ScrollController();
                }

                this.listenable.addChild(this.verticalController);
            } else {
                this.verticalController = null;
            }
        }

        @Override
        public Widget build(BuildContext context) {
            return new Clip(
                new MouseArea(
                    widget -> widget
                        .scrollCallback((horizontal, vertical) -> {
                            if (Screen.hasShiftDown()) {
                                if (this.widget().horizontal) this.horizontalController.setOffset(this.horizontalController.offset() + vertical * -15);
                            } else {
                                if (this.widget().vertical) this.verticalController.setOffset(this.verticalController.offset() + vertical * -15);
                            }

                            if (this.widget().horizontal) this.horizontalController.setOffset(this.horizontalController.offset() + horizontal * -15);
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