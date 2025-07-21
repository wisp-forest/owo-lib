package io.wispforest.owo.braid.widgets.checkbox;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class Checkbox extends StatelessWidget {

    public final boolean checked;
    public final Consumer<Boolean> onUpdate;

    public Checkbox(boolean checked, Consumer<Boolean> onUpdate) {
        this.checked = checked;
        this.onUpdate = onUpdate;
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
