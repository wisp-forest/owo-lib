package io.wispforest.owo.braid.util;

import io.wispforest.owo.Owo;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import java.util.ArrayList;
import java.util.List;

public class BraidHudBinding {

    public static void activate(BraidHudElement element) {
        if (Owo.DEBUG && ELEMENTS.contains(element)) {
            throw new IllegalStateException("attempted to activate the same BraidHudElement twice");
        }

        ELEMENTS.add(element);
    }

    private static final List<BraidHudElement> ELEMENTS = new ArrayList<>();

    // ---

    static {
        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            for (var element : ELEMENTS) {
                element.render(drawContext, renderTickCounter);
            }
        });
    }
}
