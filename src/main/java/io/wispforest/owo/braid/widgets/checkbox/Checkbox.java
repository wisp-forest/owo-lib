package io.wispforest.owo.braid.widgets.checkbox;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.checkbox.RawCheckbox.CheckboxCallback;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/// The braid equivalent of [net.minecraft.client.gui.widget.CheckboxWidget]
/// @see RawCheckbox
public class Checkbox extends StatelessWidget {

    public final boolean checked;
    /// The callback to invoke when this widget is toggled.<br>
    /// `null` indicates this widget is inactive
    public final @Nullable CheckboxCallback onUpdate;

    public Checkbox(boolean checked, @Nullable CheckboxCallback onUpdate) {
        this.checked = checked;
        this.onUpdate = onUpdate;
    }

    /// Create a `Checkbox` with an explicit active state
    public Checkbox(boolean checked, CheckboxCallback onUpdate, boolean active) {
        this(checked, active ? onUpdate : null);
    }

    @Override
    public Widget build(BuildContext context) {
        return new RawCheckbox(
            this.checked,
            this.onUpdate,
            new SpriteWidget(TEXTURE, false),
            new SpriteWidget(SELECTED_TEXTURE, false)
        );
    }

    // ---

    public static final SpriteIdentifier SELECTED_HIGHLIGHTED_TEXTURE = new SpriteIdentifier(
        SpriteWidget.GUI_ATLAS_ID,
        Identifier.ofVanilla("widget/checkbox_selected_highlighted")
    );

    public static final SpriteIdentifier SELECTED_TEXTURE = new SpriteIdentifier(
        SpriteWidget.GUI_ATLAS_ID,
        Identifier.ofVanilla("widget/checkbox_selected")
    );

    public static final SpriteIdentifier HIGHLIGHTED_TEXTURE = new SpriteIdentifier(
        SpriteWidget.GUI_ATLAS_ID,
        Identifier.ofVanilla("widget/checkbox_highlighted")
    );

    public static final SpriteIdentifier TEXTURE = new SpriteIdentifier(
        SpriteWidget.GUI_ATLAS_ID,
        Identifier.ofVanilla("widget/checkbox")
    );
}
