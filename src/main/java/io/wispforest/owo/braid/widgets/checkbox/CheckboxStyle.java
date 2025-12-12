package io.wispforest.owo.braid.widgets.checkbox;

import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.basic.Builder;
import io.wispforest.owo.braid.widgets.basic.Center;
import io.wispforest.owo.braid.widgets.focus.Focusable;
import net.minecraft.client.resources.model.Material;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

public record CheckboxStyle(
    @Nullable BackgroundBuilder backgroundBuilder,
    @Nullable Widget checkmark,
    @Nullable SoundEvent clickSound
) {
    public CheckboxStyle overriding(CheckboxStyle other) {
        return new CheckboxStyle(
            this.backgroundBuilder != null ? this.backgroundBuilder : other.backgroundBuilder,
            this.checkmark != null ? this.checkmark : other.checkmark,
            this.clickSound != null ? this.clickSound : other.clickSound
        );
    }

    public static final CheckboxStyle DEFAULT = new CheckboxStyle(null, null, null);

    @FunctionalInterface
    public interface BackgroundBuilder {
        Widget build(boolean active);
    }

    // ---

    public static final Material BRAID_BACKGROUND_TEXTURE = new Material(
        SpriteWidget.GUI_ATLAS_ID,
        Owo.id("braid_checkbox")
    );

    public static final Material BRAID_BACKGROUND_FOCUSED_TEXTURE = new Material(
        SpriteWidget.GUI_ATLAS_ID,
        Owo.id("braid_checkbox_focused")
    );

    public static final Material BRAID_CHECKMARK_TEXTURE = new Material(
        SpriteWidget.GUI_ATLAS_ID,
        Owo.id("braid_checkmark")
    );

    private static final Widget BRAID_BACKGROUND = new Builder(context -> {
        return new SpriteWidget(Focusable.shouldShowHighlight(context) ? BRAID_BACKGROUND_FOCUSED_TEXTURE : BRAID_BACKGROUND_TEXTURE);
    });

    private static final Widget BRAID_CHECKMARK = new Center(new SpriteWidget(BRAID_CHECKMARK_TEXTURE));

    public static final CheckboxStyle BRAID = new CheckboxStyle(
        active -> BRAID_BACKGROUND,
        BRAID_CHECKMARK,
        null
    );
}
