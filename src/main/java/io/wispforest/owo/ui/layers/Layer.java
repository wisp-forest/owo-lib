package io.wispforest.owo.ui.layers;

import io.wispforest.owo.mixin.ui.layers.WrapperWidgetInvoker;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class Layer<S extends Screen, R extends ParentComponent> {

    protected final BiFunction<Sizing, Sizing, R> rootComponentMaker;
    protected final BiConsumer<OwoUIAdapter<R>, Positioner> instanceInitializer;

    protected Layer(BiFunction<Sizing, Sizing, R> rootComponentMaker, BiConsumer<OwoUIAdapter<R>, Positioner> instanceInitializer) {
        this.rootComponentMaker = rootComponentMaker;
        this.instanceInitializer = instanceInitializer;
    }

    public Instance instantiate(S screen, int width, int height) {
        return new Instance(width, height, widgetLocator -> {
            var children = new ArrayDeque(screen.children());
            var widgets = new ArrayList<ClickableWidget>();

            while (!children.isEmpty()) {
                var element = children.poll();
                if (element instanceof ClickableWidget widget) widgets.add(widget);
                if (element instanceof WrapperWidgetInvoker wrapper) children.addAll(wrapper.owo$wrappedWidgets());
            }

            for (var widget : widgets) {
                if (!widgetLocator.test(widget)) continue;
                return Positioning.absolute(widget.getX(), widget.getY());
            }

            return Positioning.absolute(0, 0);
        });
    }

    public class Instance {

        public final OwoUIAdapter<R> adapter;

        protected Instance(int width, int height, Positioner positioner) {
            this.adapter = OwoUIAdapter.createWithoutScreen(0, 0, width, height, Layer.this.rootComponentMaker);
            Layer.this.instanceInitializer.accept(this.adapter, positioner);
        }

        public void resize(int width, int height) {
            this.adapter.moveAndResize(0, 0, width, height);
        }
    }

    public interface Positioner {
        Positioning nextTo(Predicate<ClickableWidget> widgetLocator);
    }

}
