package io.wispforest.owo.braid.widgets;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Builder;
import io.wispforest.owo.braid.widgets.sharedstate.ShareableState;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import io.wispforest.owo.braid.widgets.stack.Stack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Navigator extends StatelessWidget {

    public final @Nullable Widget initialRoute;

    public Navigator(@Nullable Widget initialRoute) {
        this.initialRoute = initialRoute;
    }

    @Override
    public Widget build(BuildContext context) {
        return new SharedState<>(
            () -> new NavigationState(this.initialRoute),
            new Builder(innerContext -> {
                var state = SharedState.get(innerContext, NavigationState.class);
                return new Stack(state.displayedRoutes());
            })
        );
    }

    // ---

    public static void pushOverlay(BuildContext context, Widget route) {
        SharedState.set(context, NavigationState.class, state -> state.push(route, true));
    }

    public static void push(BuildContext context, Widget route) {
        SharedState.set(context, NavigationState.class, state -> state.push(route, false));
    }

    public static void pop(BuildContext context) {
        SharedState.set(context, NavigationState.class, NavigationState::pop);
    }
}

record Route(Widget widget, boolean overlay) {}

class NavigationState extends ShareableState {
    private final List<Route> routes;
    private List<Widget> displayedRoutes = List.of();

    public NavigationState(@Nullable Widget initialRoute) {
        this.routes = initialRoute != null ? new ArrayList<>(List.of(new Route(initialRoute, false))) : new ArrayList<>();
        this.updateDisplayedRoutes();
    }

    public List<Widget> displayedRoutes() {
        return this.displayedRoutes;
    }

    public void push(Widget route, boolean overlay) {
        this.routes.add(new Route(route, overlay));
        this.updateDisplayedRoutes();
    }

    public void pop() {
        this.routes.removeLast();
        this.updateDisplayedRoutes();
    }

    private void updateDisplayedRoutes() {
        int idx;
        for (idx = this.routes.size() - 1; idx >= 0; idx--) {
            if (!this.routes.get(idx).overlay()) {
                break;
            }
        }

        this.displayedRoutes = this.routes.subList(idx, this.routes.size()).stream().map(Route::widget).toList();
    }
}
