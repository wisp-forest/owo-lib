package io.wispforest.owo.braid.widgets.slider;

import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.intents.Intent;
import io.wispforest.owo.braid.widgets.intents.Interactable;
import io.wispforest.owo.braid.widgets.intents.ShortcutTrigger;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleConsumer;

public class Incrementor extends StatelessWidget {

    public final @Nullable DoubleConsumer xCallback, yCallback;
    public final Widget child;

    public Incrementor(
        @Nullable DoubleConsumer xCallback,
        @Nullable DoubleConsumer yCallback,
        Widget child
    ) {
        this.xCallback = xCallback;
        this.yCallback = yCallback;
        this.child = child;
    }

    public Incrementor(@Nullable DoubleConsumer callback, Widget child) {
        this(callback, callback, child);
    }

    public Incrementor(LayoutAxis axis, @Nullable DoubleConsumer callback, Widget child) {
        this(
            axis.choose(callback, null),
            axis.choose(null, callback),
            child
        );
    }

    @Override
    public Widget build(BuildContext context) {
        return new Interactable(
            this.xCallback != null && this.yCallback != null ? BOTH_AXIS_SHORTCUTS : this.xCallback != null ? HORIZONTAL_SHORTCUTS : this.yCallback != null ? VERTICAL_SHORTCUTS : Map.of(),
            interactable -> interactable.addCallbackAction(
                IncrementIntent.class,
                (actionCtx, intent) -> {
                    switch (intent.axis) {
                        case HORIZONTAL -> {
                            if (this.xCallback != null) this.xCallback.accept(intent.amount);
                        }
                        case VERTICAL -> {
                            if (this.yCallback != null) this.yCallback.accept(intent.amount);
                        }
                    }
                }
            ),
            new MouseArea(
                mouseArea -> mouseArea
                    .scrollCallback((baseHorizontal, baseVertical) -> {
                        var handled = false;
                        var modifiers = AppState.of(context).eventBinding.activeModifiers();
                        var horizontal = modifiers.shift() ? baseVertical : baseHorizontal;
                        var vertical = modifiers.shift() ? baseHorizontal : baseVertical;

                        if (horizontal != 0 && this.xCallback != null) {
                            this.xCallback.accept(horizontal);
                            handled = true;
                        }
                        if (vertical != 0 && this.yCallback != null) {
                            this.yCallback.accept(vertical);
                            handled = true;
                        }
                        return handled;
                    }),
                this.child
            )
        );
    }

    // ---

    public static final Map<List<ShortcutTrigger>, Intent> HORIZONTAL_SHORTCUTS = Map.of(
        List.of(ShortcutTrigger.RIGHT), new IncrementIntent(LayoutAxis.HORIZONTAL, 1),
        List.of(ShortcutTrigger.LEFT), new IncrementIntent(LayoutAxis.HORIZONTAL, -1),
        List.of(ShortcutTrigger.HOME), new IncrementIntent(LayoutAxis.HORIZONTAL, Double.NEGATIVE_INFINITY),
        List.of(ShortcutTrigger.END), new IncrementIntent(LayoutAxis.HORIZONTAL, Double.POSITIVE_INFINITY)
    );

    public static final Map<List<ShortcutTrigger>, Intent> VERTICAL_SHORTCUTS = Map.of(
        List.of(ShortcutTrigger.UP), new IncrementIntent(LayoutAxis.VERTICAL, 1),
        List.of(ShortcutTrigger.DOWN), new IncrementIntent(LayoutAxis.VERTICAL, -1),
        List.of(ShortcutTrigger.PAGE_UP), new IncrementIntent(LayoutAxis.VERTICAL, Double.POSITIVE_INFINITY),
        List.of(ShortcutTrigger.PAGE_DOWN), new IncrementIntent(LayoutAxis.VERTICAL, Double.NEGATIVE_INFINITY)
    );

    public static final Map<List<ShortcutTrigger>, Intent> BOTH_AXIS_SHORTCUTS = Util.make(() -> {
        var map = new HashMap<List<ShortcutTrigger>, Intent>();
        map.putAll(HORIZONTAL_SHORTCUTS);
        map.putAll(VERTICAL_SHORTCUTS);
        return Collections.unmodifiableMap(map);
    });

    public record IncrementIntent(LayoutAxis axis, double amount) implements Intent {}
}
