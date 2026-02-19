package io.wispforest.owo.braid.widgets.sharedstate;

import com.google.common.collect.Iterables;
import io.wispforest.owo.braid.framework.proxy.InheritedProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public final class SharedStateProvider<T extends ShareableState> extends InheritedWidget {
    public final SharedState.State<T> state;
    public final int generation;

    private final InheritedKey inheritedKey;

    public SharedStateProvider(SharedState.State<T> state, int generation, Widget child) {
        super(child);
        this.state = state;
        this.generation = generation;

        this.inheritedKey = new InheritedKey(state.state.getClass());
    }

    @Override
    public WidgetProxy proxy() {
        return new Proxy<>(this);
    }

    @Override
    public Object inheritedKey() {
        return this.inheritedKey;
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        return generation != ((SharedStateProvider<?>) newWidget).generation;
    }

    public static Object keyOf(Class<? extends ShareableState> stateClass) {
        return new InheritedKey(stateClass);
    }

    public static <T> Object dependencyOf(Class<T> stateClass, @Nullable Object capturedValue, Function<T, ? extends @Nullable Object> selector) {
        return new StateAspect<>(stateClass, capturedValue, selector);
    }

    public static class Proxy<T extends ShareableState> extends InheritedProxy {

        private static final Object COMPLETE_DEPENDENCY_SENTINEL = new Object();
        private final Map<WidgetProxy, Object> dependenciesByDependent = new HashMap<>();

        public Proxy(SharedStateProvider<T> widget) {
            super(widget);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void addDependency(WidgetProxy dependent, @Nullable Object dependency) {
            super.addDependency(dependent, dependency);

            var existingDependency = this.dependenciesByDependent.get(dependent);
            if (existingDependency != null && !(existingDependency instanceof List<?>)) {
                return;
            }

            if (!(dependency instanceof StateAspect<?> aspect) || aspect.stateClass() != ((SharedStateProvider<T>) this.widget()).state.state.getClass()) {
                this.dependenciesByDependent.put(dependent, COMPLETE_DEPENDENCY_SENTINEL);
                return;
            }

            List<StateAspect<T>> aspects;
            if (existingDependency != null) {
                aspects = (List<StateAspect<T>>) existingDependency;
            } else {
                aspects = new ArrayList<>();
                this.dependenciesByDependent.put(dependent, aspects);
            }

            aspects.add((StateAspect<T>) dependency);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected boolean mustRebuildDependent(WidgetProxy dependent) {
            var dependency = this.dependenciesByDependent.get(dependent);
            if (dependency instanceof List<?>) {
                return Iterables.any(
                    (List<StateAspect<T>>) dependency,
                    element -> !Objects.equals(element.capturedValue(), element.selector().apply(((SharedStateProvider<T>) this.widget()).state.state))
                );
            } else {
                return true;
            }
        }

        @Override
        public void notifyDependent(WidgetProxy dependent) {
            super.notifyDependent(dependent);
            this.dependenciesByDependent.remove(dependent);
        }
    }
}

record InheritedKey(Class<?> stateClass) {}

record StateAspect<T>(Class<T> stateClass, @Nullable Object capturedValue, Function<T, ? extends @Nullable Object> selector) {}