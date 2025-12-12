package io.wispforest.owo.braid.widgets.cycle;

import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.intents.*;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public class CyclingClickable extends StatelessWidget {

    public final @Nullable Cycler.CycleFunction cycle;
    public final @Nullable SoundEvent clickSound;
    public final boolean skipFocusTraversal;
    public final Widget child;

    public CyclingClickable(@Nullable Cycler.CycleFunction cycle, @Nullable SoundEvent clickSound, boolean skipFocusTraversal, Widget child) {
        this.cycle = cycle;
        this.clickSound = clickSound;
        this.skipFocusTraversal = skipFocusTraversal;
        this.child = child;
    }

    @Override
    public Widget build(BuildContext context) {
        if (this.cycle == null) {
            return this.child;
        }

        return new MouseArea(
            widget ->
                widget.scrollCallback((horizontal, vertical) -> this.cycle.forScroll(vertical)),
            new Interactable(
                SHORTCUTS,
                widget -> widget
                    .cursorStyle(CursorStyle.HAND)
                    .skipTraversal(this.skipFocusTraversal)
                    .addCallbackAction(
                        AdjustIntent.class,
                        this.cycleCallback(this.cycle, intent -> intent.direction().offset())
                    ).addCallbackAction(
                        PrimaryActionIntent.class,
                        this.cycleCallback(this.cycle, intent -> 1)
                    ).addCallbackAction(
                        SecondaryActionIntent.class,
                        this.cycleCallback(this.cycle, intent -> -1)
                    ),
                child
            )
        );
    }

    private <I extends Intent> Action.Callback<I> cycleCallback(Cycler.CycleFunction cycle, ToIntFunction<I> offset) {
        var sound = this.clickSound != null ? this.clickSound : SoundEvents.UI_BUTTON_CLICK.value();
        return (actionCtx, intent) -> {
            if (cycle.cycle(offset.applyAsInt(intent))) {
                UISounds.play(sound);
            }
        };
    }

    // ---

    private static final Map<List<ShortcutTrigger>, Intent> SHORTCUTS = Map.of(
        List.of(ShortcutTrigger.of(ShortcutTrigger.UP, ShortcutTrigger.RIGHT)), new AdjustIntent(AdjustIntent.Direction.INCREMENT),
        List.of(ShortcutTrigger.of(ShortcutTrigger.RIGHT_CLICK, ShortcutTrigger.DOWN, ShortcutTrigger.LEFT)), new AdjustIntent(AdjustIntent.Direction.DECREMENT)
    );
}
