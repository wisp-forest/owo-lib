package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.BraidGraphics;
import io.wispforest.owo.braid.framework.instance.OptionalChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.OptionalChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class Panel extends OptionalChildInstanceWidget {

    public static final Identifier VANILLA_LIGHT = Owo.id("panel/default");
    public static final Identifier VANILLA_DARK = Owo.id("panel/dark");
    public static final Identifier VANILLA_INSET = Owo.id("panel/inset");

    // ---

    public final @Nullable Identifier texture;

    public Panel(@Nullable Identifier texture, @Nullable Widget child) {
        super(child);
        this.texture = texture;
    }

    public Panel(Identifier texture) {
        this(texture, null);
    }

    @Override
    public OptionalChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends OptionalChildWidgetInstance.ShrinkWrap<Panel> {

        public Instance(Panel widget) {
            super(widget);
        }

        @Override
        public void draw(BraidGraphics graphics) {
            if (this.widget.texture != null) {
                NinePatchTexture.draw(this.widget.texture, OwoUIGraphics.of(graphics), 0, 0, (int) this.transform.width(), (int) this.transform.height());
            }

            super.draw(graphics);
        }
    }
}
