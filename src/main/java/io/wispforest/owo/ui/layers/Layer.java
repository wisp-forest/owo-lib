package io.wispforest.owo.ui.layers;

import io.wispforest.owo.mixin.ui.layers.WrapperWidgetInvoker;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Layer<S extends Screen, R extends ParentComponent> {

    protected final BiFunction<Sizing, Sizing, R> rootComponentMaker;
    protected final Consumer<Layer<S, R>.Instance> instanceInitializer;

    protected Layer(BiFunction<Sizing, Sizing, R> rootComponentMaker, Consumer<Layer<S, R>.Instance> instanceInitializer) {
        this.rootComponentMaker = rootComponentMaker;
        this.instanceInitializer = instanceInitializer;
    }

    public Instance instantiate(S screen) {
        return new Instance(screen);
    }

    public class Instance {

        public final S screen;
        public final OwoUIAdapter<R> adapter;

        public boolean aggressivePositioning = false;

        protected final List<Runnable> layoutUpdaters = new ArrayList<>();

        protected Instance(S screen) {
            this.screen = screen;
            this.adapter = OwoUIAdapter.createWithoutScreen(0, 0, screen.width, screen.height, Layer.this.rootComponentMaker);
            Layer.this.instanceInitializer.accept(this);
        }

        public void resize(int width, int height) {
            this.adapter.moveAndResize(0, 0, width, height);
        }

        public @Nullable ClickableWidget queryWidget(Predicate<ClickableWidget> locator) {
            var widgets = new ArrayList<ClickableWidget>();
            for (var element : this.screen.children()) collectChildren(element, widgets);

            ClickableWidget widget = null;
            for (var candidate : widgets) {
                if (!locator.test(candidate)) continue;
                widget = candidate;
                break;
            }

            return widget;
        }

        public void alignComponentToWidget(Predicate<ClickableWidget> locator, AnchorSide anchor, float justification, Component component) {
            this.layoutUpdaters.add(() -> {
                var widget = this.queryWidget(locator);

                if (widget == null) {
                    component.positioning(Positioning.absolute(0, 0));
                    return;
                }

                var size = component.fullSize();
                switch (anchor) {
                    case TOP -> component.positioning(Positioning.absolute(
                            (int) (widget.getX() + (widget.getWidth() - size.width()) * justification),
                            widget.getY() - size.height()
                    ));
                    case RIGHT -> component.positioning(Positioning.absolute(
                            widget.getX() + widget.getWidth(),
                            (int) (widget.getY() + (widget.getHeight() - size.height()) * justification)
                    ));
                    case BOTTOM -> component.positioning(Positioning.absolute(
                            (int) (widget.getX() + (widget.getWidth() - size.width()) * justification),
                            widget.getY() + widget.getHeight()
                    ));
                    case LEFT -> component.positioning(Positioning.absolute(
                            widget.getX() - size.width(),
                            (int) (widget.getY() + (widget.getHeight() - size.height()) * justification)
                    ));
                }
            });
        }

        public void dispatchLayoutUpdates() {
            this.layoutUpdaters.forEach(Runnable::run);
        }

        private static void collectChildren(Element element, List<ClickableWidget> children) {
            if (element instanceof ClickableWidget widget) children.add(widget);
            if (element instanceof WrapperWidgetInvoker wrapper) {
                for (var widget : wrapper.owo$wrappedWidgets()) {
                    collectChildren(widget, children);
                }
            }
        }

        public enum AnchorSide {
            TOP, BOTTOM, LEFT, RIGHT
        }
    }

}