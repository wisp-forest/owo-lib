package io.wispforest.owo.ui.container;

import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Containers {

    private Containers() {}

    // ------
    // Layout
    // ------

    public static GridLayout grid(Sizing horizontalSizing, Sizing verticalSizing, int rows, int columns) {
        return new GridLayout(horizontalSizing, verticalSizing, rows, columns);
    }

    public static FlowLayout verticalFlow(Sizing horizontalSizing, Sizing verticalSizing) {
        return new FlowLayout(horizontalSizing, verticalSizing, FlowLayout.Algorithm.VERTICAL);
    }

    public static FlowLayout horizontalFlow(Sizing horizontalSizing, Sizing verticalSizing) {
        return new FlowLayout(horizontalSizing, verticalSizing, FlowLayout.Algorithm.HORIZONTAL);
    }

    public static FlowLayout ltrTextFlow(Sizing horizontalSizing, Sizing verticalSizing) {
        return new FlowLayout(horizontalSizing, verticalSizing, FlowLayout.Algorithm.LTR_TEXT);
    }

    public static StackLayout stack(Sizing horizontalSizing, Sizing verticalSizing) {
        return new StackLayout(horizontalSizing, verticalSizing);
    }

    // ------
    // Scroll
    // ------

    public static <C extends Component> ScrollContainer<C> verticalScroll(Sizing horizontalSizing, Sizing verticalSizing, C child) {
        return new ScrollContainer<>(ScrollContainer.ScrollDirection.VERTICAL, horizontalSizing, verticalSizing, child);
    }

    public static <C extends Component> ScrollContainer<C> horizontalScroll(Sizing horizontalSizing, Sizing verticalSizing, C child) {
        return new ScrollContainer<>(ScrollContainer.ScrollDirection.HORIZONTAL, horizontalSizing, verticalSizing, child);
    }

    // ----------------
    // Utility wrappers
    // ----------------

    public static <T, C extends Component> FlowLayout list(Iterable<T> data, Consumer<FlowLayout> layoutConfigurator, Function<T, C> componentMaker, boolean vertical) {
        var layout = vertical ? Containers.verticalFlow(Sizing.content(), Sizing.content()) : Containers.horizontalFlow(Sizing.content(), Sizing.content());
        layoutConfigurator.accept(layout);

        for (var value : data) {
            layout.child(componentMaker.apply(value));
        }

        return layout;
    }

    public static <T, C extends Component> FlowLayout list(Iterable<T> data, Consumer<FlowLayout> layoutConfigurator, Function<T, C> componentMaker) {
        return list(data, layoutConfigurator, componentMaker, true);
    }

    public static <K, V, C extends Component> FlowLayout list(Map<K, V> data, Consumer<FlowLayout> layoutConfigurator, BiFunction<K, V, C> componentMaker, boolean vertical) {
        var layout = vertical ? Containers.verticalFlow(Sizing.content(), Sizing.content()) : Containers.horizontalFlow(Sizing.content(), Sizing.content());
        layoutConfigurator.accept(layout);

        for (Map.Entry<K, V> entry : data.entrySet()) {
            layout.child(componentMaker.apply(entry.getKey(), entry.getValue()));
        }

        return layout;
    }

    public static <K, V, C extends Component> FlowLayout list(Map<K, V> data, Consumer<FlowLayout> layoutConfigurator, BiFunction<K, V, C> componentMaker) {
        return list(data, layoutConfigurator, componentMaker, true);
    }

    public static <C extends Component> DraggableContainer<C> draggable(Sizing horizontalSizing, Sizing verticalSizing, C child) {
        return new DraggableContainer<>(horizontalSizing, verticalSizing, child);
    }

    public static CollapsibleContainer collapsible(Sizing horizontalSizing, Sizing verticalSizing, Text title, boolean expanded) {
        return new CollapsibleContainer(horizontalSizing, verticalSizing, title, expanded);
    }

    public static <C extends Component> OverlayContainer<C> overlay(C child) {
        return new OverlayContainer<>(child);
    }

    public static <C extends Component> RenderEffectWrapper<C> renderEffect(C child) {
        return new RenderEffectWrapper<>(child);
    }

}