package io.wispforest.owo.braid.util.layers;

import io.wispforest.owo.braid.core.RelativePosition;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.EmptyWidget;
import io.wispforest.owo.braid.widgets.overlay.Overlay;
import io.wispforest.owo.braid.widgets.overlay.OverlayEntry;
import io.wispforest.owo.braid.widgets.overlay.OverlayEntryBuilder;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

// TODO:
//  - consider whether we should provide means to render the same
//    widget multiple times if the predicate finds more than one match
//  - perhaps we should also make the miss behavior customizable - hide
//    the widget when no match is found, show it when one is? keep it around
//    once we've found a match at least once?
public class LayerAlignment extends StatefulWidget {

    public final Function<BuildContext, @Nullable Vector2d> offsetGetter;
    public final @Nullable AnchorJustification justification;
    public final Widget widget;

    private LayerAlignment(Function<BuildContext, @Nullable Vector2d> offsetGetter, @Nullable AnchorJustification justification, Widget widget) {
        this.offsetGetter = offsetGetter;
        this.justification = justification;
        this.widget = widget;
    }

    public static LayerAlignment atVanillaWidget(Predicate<AbstractWidget> anchorPredicate, Widget widget) {
        return atVanillaWidget(anchorPredicate, AnchorJustification.TOP_LEFT_TO_TOP_LEFT, widget);
    }

    public static LayerAlignment atVanillaWidget(Predicate<AbstractWidget> anchorPredicate, AnchorJustification justification, Widget widget) {
        return new LayerAlignment(
            context -> {
                var anchor = LayerContext.findWidget(context, anchorPredicate);
                if (anchor == null) return null;

                return new Vector2d(
                    anchor.getX() + justification.anchorX() * anchor.getWidth(),
                    anchor.getY() + justification.anchorY() * anchor.getHeight()
                );
            },
            justification,
            widget
        );
    }

    public static LayerAlignment atContainerScreenCoordinates(double xOffset, double yOffset, Widget widget) {
        return new LayerAlignment(
            context -> {
                var root = LayerContext.containerScreenRootOf(context);
                if (root == null) return null;

                return root.add(xOffset, yOffset);
            },
            null,
            widget
        );
    }

    @Override
    public WidgetState<LayerAlignment> createState() {
        return new State();
    }

    public static class State extends WidgetState<LayerAlignment> {

        private OverlayEntry entry;
        private boolean widgetChanged = false;

        @Override
        public void init() {
            this.scheduleOverlayUpdate();
        }

        @Override
        public void didUpdateWidget(LayerAlignment oldWidget) {
            this.widgetChanged = oldWidget.widget != this.widget().widget
                || !Objects.equals(oldWidget.justification, this.widget().justification);
        }

        @Override
        public void notifyDependenciesChanged() {
            this.scheduleOverlayUpdate();
        }

        private void scheduleOverlayUpdate() {
            this.schedulePostLayoutCallback(() -> {
                var offset = this.widget().offsetGetter.apply(this.context());
                if (offset == null) return;

                if (this.entry == null) {
                    this.entry = Overlay.of(this.context()).add(
                        new OverlayEntryBuilder(
                            this.prepareWidget(),
                            new RelativePosition(this.context(), offset.x, offset.y)
                        )
                    );
                } else if (this.widgetChanged || this.entry.x != offset.x || this.entry.y != offset.y) {
                    this.widgetChanged = false;
                    this.entry.setState(() -> {
                        this.entry.widget = this.prepareWidget();
                        this.entry.x = offset.x;
                        this.entry.y = offset.y;
                    });
                }
            });
        }
        
        private Widget prepareWidget() {
            var justification = this.widget().justification;
            return justification != null
                ? new Justify(justification.widgetX(), justification.widgetY(), this.widget().widget)
                : this.widget().widget;
        }

        @Override
        public Widget build(BuildContext context) {
            return EmptyWidget.INSTANCE;
        }
    }

}
