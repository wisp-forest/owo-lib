package io.wispforest.owo.braid.widgets.checkbox;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.checkbox.RawCheckbox.CheckboxCallback;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class BraidCheckbox extends StatelessWidget {

    public final boolean checked;
    public final CheckboxCallback onUpdate;

    public BraidCheckbox(boolean checked, CheckboxCallback onUpdate) {
        this.checked = checked;
        this.onUpdate = onUpdate;
    }

    public BraidCheckbox(boolean checked, CheckboxCallback onUpdate, boolean active) {
        this(checked, active ? onUpdate : null);
    }

    @Override
    public Widget build(BuildContext context) {
        //TODO: visual indication of disabled/hovered states
        return new RawCheckbox(
            this.checked,
            this.onUpdate,
            new SpriteWidget(BACKGROUND_TEXTURE, false),
            new SpriteWidget(CHECKMARK_TEXTURE, false)
        );
    }

    // ---

    public static final SpriteIdentifier BACKGROUND_TEXTURE = new SpriteIdentifier(
        SpriteWidget.GUI_ATLAS_ID,
        Identifier.of("owo", "braid_checkbox")
    );

    public static final SpriteIdentifier CHECKMARK_TEXTURE = new SpriteIdentifier(
        SpriteWidget.GUI_ATLAS_ID,
        Identifier.of("owo", "braid_checkmark")
    );
}
