package io.wispforest.owo.ui.layers;

import io.wispforest.owo.mixin.ui.layers.WrapperWidgetInvoker;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
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

        public final Screen screen;
        public final OwoUIAdapter<R> adapter;

        public boolean aggressivePositioning = false;

        protected final List<Runnable> layoutUpdaters = new ArrayList<>();

        protected Instance(Screen screen) {
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

        public void queryWidgetPosition(Predicate<ClickableWidget> locator, Anchor anchor, Consumer<Positioning> positioner) {
            this.layoutUpdaters.add(() -> {
                var widget = this.queryWidget(locator);

                if (widget == null) {
                    positioner.accept(Positioning.absolute(0, 0));
                    return;
                }

                switch (anchor) {
                    case TOP_LEFT -> positioner.accept(Positioning.absolute(widget.getX(), widget.getY()));
                    case BOTTOM_LEFT -> positioner.accept(Positioning.absolute(widget.getX(), widget.getY() + widget.getHeight()));
                    case TOP_RIGHT -> positioner.accept(Positioning.absolute(widget.getX() + widget.getWidth(), widget.getY()));
                    case BOTTOM_RIGHT -> positioner.accept(Positioning.absolute(widget.getX() + widget.getWidth(), widget.getY() + widget.getHeight()));
                };
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

        public enum Anchor {
            TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT
        }
    }

}