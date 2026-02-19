package io.wispforest.owo.braid.widgets.checkbox;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.button.Clickable;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class TogglingClickable extends StatelessWidget {

    public final boolean checked;
    public final @Nullable CheckboxCallback onUpdate;
    public final @Nullable SoundEvent clickSound;
    public final Widget child;

    public TogglingClickable(boolean checked, @Nullable CheckboxCallback onUpdate, @Nullable SoundEvent clickSound, Widget child) {
        this.checked = checked;
        this.onUpdate = onUpdate;
        this.clickSound = clickSound;
        this.child = child;
    }

    public TogglingClickable(boolean checked, @Nullable CheckboxCallback onUpdate, Widget child) {
        this(checked, onUpdate, null, child);
    }

    public TogglingClickable(boolean checked, boolean active, @Nullable SoundEvent clickSound, CheckboxCallback onUpdate, Widget child) {
        this(checked, active ? onUpdate : null, clickSound, child);
    }

    public TogglingClickable(boolean checked, boolean active, CheckboxCallback onUpdate, Widget child) {
        this(checked, active, null, onUpdate, child);
    }

    @Override
    public Widget build(BuildContext context) {
        return new Clickable(
            this.onUpdate != null ? Clickable.alwaysClick(() -> this.onUpdate.accept(!this.checked)) : null,
            this.clickSound,
            this.child
        );
    }

    @FunctionalInterface
    public interface CheckboxCallback {
        void accept(boolean nowChecked);
    }
}
