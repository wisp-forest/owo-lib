package io.wispforest.owo.braid.widgets.overlay;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.Key;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.EmptyWidget;
import io.wispforest.owo.braid.widgets.basic.HitTestTrap;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Overlay extends StatefulWidget {

    public final Widget child;

    public Overlay(Widget child) {
        this.child = child;
    }

    @Override
    public WidgetState<Overlay> createState() {
        return new State();
    }

    // ---

    public static @Nullable State maybeOf(BuildContext context) {
        var provider = context.getAncestor(OverlayProvider.class);
        return provider != null ? provider.state : null;
    }

    public static State of(BuildContext context) {
        var state = maybeOf(context);
        Preconditions.checkNotNull(state, "attempted to look up the enclosing overlay state without one present");

        return state;
    }

    // ---

    public static class State extends WidgetState<Overlay> {

        public OverlayEntry add(OverlayEntryBuilder builder) {
            var entryPosition = builder.position.convertTo(this.context());

            var entry = new OverlayEntry(
                this,
                builder.onRemove,
                builder.widget,
                builder.dismissOverlayOnClick,
                builder.occludeHitTest,
                entryPosition.x,
                entryPosition.y
            );

            this.setState(() -> {
                this.entries.add(entry);
            });

            return entry;
        }

        // ---

        final List<OverlayEntry> entries = new ArrayList<>();

        @SuppressWarnings("DataFlowIssue")
        @Override
        public Widget build(BuildContext context) {
            return new OverlayProvider(
                this,
                new Stack(
                    this.widget().child,
                    new HitTestTrap(
                        Iterables.any(this.entries, entry -> entry.occludeHitTest),
                        new MouseArea(
                            widget -> widget
                                .clickCallback((x, y, button, modifiers) -> {
                                    if (!Iterables.any(this.entries, entry -> entry.dismissOnOverlayClick)) return false;

                                    for (var entry : Iterables.filter(this.entries, entry -> entry.dismissOnOverlayClick)) {
                                        if (entry.onRemove != null) entry.onRemove.run();
                                    }

                                    this.setState(() -> {
                                        this.entries.removeIf(entry -> entry.dismissOnOverlayClick);
                                    });

                                    return false;
                                }),
                            EmptyWidget.INSTANCE
                        )
                    ),
                    new StackBase(
                        new RawOverlay(
                            this.entries.stream()
                                .map(entry -> (RawOverlayElement) new RawOverlayElement(entry.x, entry.y, entry.widget).key(Key.of(entry.uuid.toString())))
                                .toList()
                        )
                    )
                )
            );
        }
    }
}
