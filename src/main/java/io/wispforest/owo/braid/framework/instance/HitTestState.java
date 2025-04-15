package io.wispforest.owo.braid.framework.instance;

import com.google.common.collect.FluentIterable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.Predicate;

public class HitTestState {
    private final Deque<Hit> hits = new ArrayDeque<>();

    public boolean anyHit() {
        return !this.hits.isEmpty();
    }

    public Hit firstHit() {
        return this.hits.getFirst();
    }

    public Iterable<Hit> trace() {
        return this.hits;
    }

    public Iterable<Hit> occludedTrace() {
        return new Iterable<>() {
            @Override
            public @NotNull Iterator<Hit> iterator() {
                var inner = HitTestState.this.hits.iterator();

                return new Iterator<>() {
                    private boolean encounteredBoundary = false;

                    @Override
                    public boolean hasNext() {
                        return inner.hasNext() && !this.encounteredBoundary;
                    }

                    @Override
                    public Hit next() {
                        var next = inner.next();
                        if ((next.instance().flags & WidgetInstance.FLAG_HIT_TEST_BOUNDARY) != 0) {
                            this.encounteredBoundary = true;
                        }

                        return next;
                    }
                };
            }
        };
    }

    public @Nullable Hit firstWhere(Predicate<Hit> predicate) {
        return FluentIterable.from(this.occludedTrace()).firstMatch(predicate::test).orNull();
    }

    public void addHit(WidgetInstance<?> instance, double x, double y) {
        this.hits.addFirst(new Hit(instance, x, y));
    }
}
