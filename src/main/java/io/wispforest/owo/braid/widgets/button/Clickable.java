package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.ControlsOverride;
import io.wispforest.owo.braid.widgets.intents.Interactable;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class Clickable extends StatelessWidget {

    public final @Nullable BooleanSupplier onClick;
    public final @Nullable SoundEvent clickSound;
    public final Widget child;

    public Clickable(@Nullable BooleanSupplier onClick, @Nullable SoundEvent clickSound, Widget child) {
        this.onClick = onClick;
        this.clickSound = clickSound;
        this.child = child;
    }

    public Clickable(@Nullable BooleanSupplier onClick, Widget child) {
        this(onClick, null, child);
    }

    public Clickable(boolean active, BooleanSupplier onClick, @Nullable SoundEvent clickSound, Widget child) {
        this(active ? onClick : null, clickSound, child);
    }

    public Clickable(boolean active, BooleanSupplier onClick, Widget child) {
        this(active, onClick, null, child);
    }

    @Override
    public Widget build(BuildContext context) {
        if (this.onClick == null || ControlsOverride.controlsDisabled(context)) {
            return this.child;
        }

        var effectiveSound = this.clickSound != null ? this.clickSound : SoundEvents.UI_BUTTON_CLICK.value();
        return Interactable.primary(
            () -> {
                if (this.onClick.getAsBoolean()) {
                    UISounds.play(effectiveSound);
                }
            },
            this.child
        );
    }

    // ---

    public static @Nullable BooleanSupplier alwaysClick(@Nullable Runnable onClick) {
        if (onClick == null) {
            return null;
        }

        return () -> {
            onClick.run();
            return true;
        };
    }
}
