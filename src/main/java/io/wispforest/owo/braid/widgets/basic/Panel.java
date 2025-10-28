package io.wispforest.owo.braid.widgets.basic;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.BraidDrawContext;
import io.wispforest.owo.braid.framework.instance.OptionalChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.OptionalChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.minecraft.util.Identifier;
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
        public void draw(BraidDrawContext ctx) {
            if (this.widget.texture != null) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                NinePatchTexture.draw(this.widget.texture, OwoUIDrawContext.of(ctx), 0, 0, (int) this.transform.width(), (int) this.transform.height());
            }

            super.draw(ctx);
        }
    }
}
