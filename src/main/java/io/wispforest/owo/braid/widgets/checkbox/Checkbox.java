package io.wispforest.owo.braid.widgets.checkbox;

import blue.endless.jankson.annotation.Nullable;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.checkbox.RawCheckbox.CheckboxCallback;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

public class Checkbox extends StatelessWidget {

    public final boolean checked;
    public final @Nullable CheckboxCallback onUpdate;

    public Checkbox(boolean checked, @Nullable CheckboxCallback onUpdate) {
        this.checked = checked;
        this.onUpdate = onUpdate;
    }

    public Checkbox(boolean checked, boolean active, CheckboxCallback onUpdate) {
        this(checked, active ? onUpdate : null);
    }

    @Override
    public Widget build(BuildContext context) {
        //TODO: visual indication of disabled/hovered states
        return new RawCheckbox(
            this.checked,
            this.onUpdate,
            new SpriteWidget(TEXTURE),
            new SpriteWidget(SELECTED_TEXTURE)
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
