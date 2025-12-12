package io.wispforest.owo.braid.util;

import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.braid.core.EventBinding;
import io.wispforest.owo.braid.core.Surface;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Align;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.apache.commons.lang3.mutable.MutableObject;

import java.lang.ref.Cleaner;

public class BraidTooltipComponent implements ClientTooltipComponent {

    private final AppState app;
    private final EmbedderRoot.Instance instance;

    public BraidTooltipComponent(Widget widget) {
        var embedderInstance = new MutableObject<EmbedderRoot.Instance>();
        this.app = new AppState(
            Owo.LOGGER,
            AppState.formatName("BraidTooltipComponent", widget),
            Minecraft.getInstance(),
            new Surface.Default(),
            new EventBinding.Headless(),
            new Align(
                Alignment.TOP_LEFT,
                new EmbedderRoot(
                    embedderInstance::setValue,
                    widget
                )
            )
        );

        this.app.processEvents(0);
        this.instance = embedderInstance.getValue();

        APP_CLEANER.register(this, new CleanCallback(this.app));
    }

    @Override
    public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics context) {
        context.push().translate(x, y);
        this.app.draw(context);
        context.pop();
    }

    @Override
    public int getWidth(Font font) {
        return (int) this.instance.transform.width();
    }

    @Override
    public int getHeight(Font font) {
        return (int) this.instance.transform.height();
    }

    // ---

    private static final Cleaner APP_CLEANER = Cleaner.create();

    private record CleanCallback(AppState app) implements Runnable {
        @Override
        public void run() {
            Minecraft.getInstance().schedule(this.app::dispose);
        }
    }
}
