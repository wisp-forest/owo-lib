package io.wispforest.owo.braid.util;

import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.braid.core.EventBinding;
import io.wispforest.owo.braid.core.Surface;
import io.wispforest.owo.braid.framework.widget.Widget;
//import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
//import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

// TODO: ADD MIXIN TO ADD BINARY COMPAT FOR THIS
public class BraidHudElement implements /*HudElement*/ GuiLayer {

    private static final Set<BraidHudElement> activeElements = Collections.newSetFromMap(new WeakHashMap<>());

    public final Widget widget;
    private AppState app;

    public BraidHudElement(Widget widget) {
        this.widget = widget;

        activeElements.add(this);
    }

    static {
        NeoForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggingIn>addListener(event -> {
            for (var activeElement : activeElements) activeElement.setupAppState();
        });

        NeoForge.EVENT_BUS.<ClientPlayerNetworkEvent.LoggingOut>addListener(event -> {
            for (var activeElement : activeElements) activeElement.resetAppState();
        });
    }

    public @Nullable AppState app() {
        return this.app;
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphicsExtractor, DeltaTracker deltaTracker) {
        extractRenderState(guiGraphicsExtractor, deltaTracker);
    }

    /*@Override*/
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (this.app == null) {
            if (!Owo.DEBUG) {
                return;
            }

            throw new IllegalStateException("tried to render a BraidHudElement before it was initialized");
        }

        this.app.processEvents(deltaTracker.getGameTimeDeltaTicks());
        this.app.draw(graphics);
    }

    protected void setupAppState() {
        this.app = new AppState(
            null,
            AppState.formatName("BraidHudElement", widget),
            Minecraft.getInstance(),
            new Surface.Default(),
            new EventBinding.Headless(),
            widget
        );
    }

    protected void resetAppState() {
        this.app.dispose();
        this.app = null;
    }
}
