package io.wispforest.owo.braid.util.layers;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.eventstream.BraidEventSource;
import io.wispforest.owo.braid.widgets.eventstream.StreamListenerState;
import io.wispforest.owo.mixin.ui.layers.AbstractContainerScreenAccessor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class LayerContext extends StatefulWidget {

    public final BraidEventSource<Unit> refreshEvents;
    public final Screen contextScreen;
    public final Widget child;

    public LayerContext(BraidEventSource<Unit> refreshEvents, Screen contextScreen, Widget child) {
        this.refreshEvents = refreshEvents;
        this.contextScreen = contextScreen;
        this.child = child;
    }

    @Override
    public WidgetState<LayerContext> createState() {
        return new State();
    }

    public static class State extends StreamListenerState<LayerContext> {

        @Override
        public void init() {
            this.streamListen(widget -> widget.refreshEvents, unit -> this.setState(() -> {}));
        }

        @Override
        public Widget build(BuildContext context) {
            return new LayerContextScope(
                this.widget().child,
                this.widget().contextScreen
            );
        }
    }

    // ---

    private static LayerContextScope of(BuildContext context) {
        var layerContext = context.dependOnAncestor(LayerContextScope.class);
        if (layerContext == null) {
            throw new IllegalStateException("attempted to look up the ambient LayerContext without one present");
        }

        return layerContext;
    }

    public static AbstractWidget findWidget(BuildContext context, Predicate<AbstractWidget> predicate) {
        var layerContext = of(context);

        var widgets = new ArrayList<AbstractWidget>();
        for (var element : layerContext.contextScreen.children()) {
            collectChildren(element, widgets);
        }

        AbstractWidget widget = null;
        for (var candidate : widgets) {
            if (!predicate.test(candidate)) continue;
            widget = candidate;
            break;
        }

        return widget;
    }

    public static Screen screenOf(BuildContext context) {
        return of(context).contextScreen;
    }

    public static @Nullable Vector2d containerScreenRootOf(BuildContext context) {
        var screen = screenOf(context);
        if (!(screen instanceof AbstractContainerScreenAccessor containerScreen)) return null;

        return new Vector2d(
            containerScreen.owo$getRootX(),
            containerScreen.owo$getRootY()
        );
    }

    private static void collectChildren(GuiEventListener element, List<AbstractWidget> children) {
        if (element instanceof AbstractWidget widget) children.add(widget);
        if (element instanceof Layout layout) {
            layout.visitWidgets(child -> collectChildren(child, children));
        }
    }
}

class LayerContextScope extends InheritedWidget {

    public final Screen contextScreen;

    public LayerContextScope(Widget child, Screen contextScreen) {
        super(child);
        this.contextScreen = contextScreen;
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        return true;
    }
}
