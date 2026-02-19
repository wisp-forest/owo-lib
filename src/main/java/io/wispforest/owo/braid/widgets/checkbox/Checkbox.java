package io.wispforest.owo.braid.widgets.checkbox;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.basic.Builder;
import io.wispforest.owo.braid.widgets.basic.ControlsOverride;
import io.wispforest.owo.braid.widgets.checkbox.TogglingClickable.CheckboxCallback;
import io.wispforest.owo.braid.widgets.focus.Focusable;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class Checkbox extends StatelessWidget {

    public final @Nullable CheckboxStyle style;
    public final boolean checked;
    public final @Nullable CheckboxCallback onUpdate;

    public Checkbox(@Nullable CheckboxStyle style, boolean checked, @Nullable CheckboxCallback onUpdate) {
        this.checked = checked;
        this.style = style;
        this.onUpdate = onUpdate;
    }

    public Checkbox(boolean checked, @Nullable CheckboxCallback onUpdate) {
        this(null, checked, onUpdate);
    }

    public Checkbox(@Nullable CheckboxStyle style, boolean checked, boolean active, CheckboxCallback onUpdate) {
        this(style, checked, active ? onUpdate : null);
    }

    public Checkbox(boolean checked, boolean active, CheckboxCallback onUpdate) {
        this(null, checked, active, onUpdate);
    }

    @Override
    public Widget build(BuildContext context) {
        var effectiveStyle = this.style != null ? this.style : CheckboxStyle.DEFAULT;
        if (DefaultCheckboxStyle.maybeOf(context) instanceof CheckboxStyle contextStyle) {
            effectiveStyle = effectiveStyle.overriding(contextStyle);
        }

        var disabled = this.onUpdate == null || ControlsOverride.controlsDisabled(context);
        var background = effectiveStyle.backgroundBuilder() != null
            ? effectiveStyle.backgroundBuilder().build(disabled)
            : DEFAULT_BACKGROUND;

        var checkmark = effectiveStyle.checkmark() != null
            ? effectiveStyle.checkmark()
            : DEFAULT_CHECKMARK;

        return new TogglingClickable(
            this.checked,
            this.onUpdate,
            effectiveStyle.clickSound(),
            this.checked
                ? new Stack(new StackBase(background), checkmark)
                : background
        );
    }

    // ---

    public static final Material SELECTED_HIGHLIGHTED_TEXTURE = new Material(
        SpriteWidget.GUI_ATLAS_ID,
        Identifier.withDefaultNamespace("widget/checkbox_selected_highlighted")
    );

    public static final Material SELECTED_TEXTURE = new Material(
        SpriteWidget.GUI_ATLAS_ID,
        Identifier.withDefaultNamespace("widget/checkbox_selected")
    );

    public static final Material HIGHLIGHTED_TEXTURE = new Material(
        SpriteWidget.GUI_ATLAS_ID,
        Identifier.withDefaultNamespace("widget/checkbox_highlighted")
    );

    public static final Material TEXTURE = new Material(
        SpriteWidget.GUI_ATLAS_ID,
        Identifier.withDefaultNamespace("widget/checkbox")
    );

    // ---

    private static final Widget DEFAULT_BACKGROUND = new Builder(context -> {
        return new SpriteWidget(Focusable.shouldShowHighlight(context) ? HIGHLIGHTED_TEXTURE : TEXTURE);
    });

    private static final Widget DEFAULT_CHECKMARK = new Builder(context -> {
        return new SpriteWidget(Focusable.shouldShowHighlight(context) ? SELECTED_HIGHLIGHTED_TEXTURE : SELECTED_TEXTURE);
    });
}
