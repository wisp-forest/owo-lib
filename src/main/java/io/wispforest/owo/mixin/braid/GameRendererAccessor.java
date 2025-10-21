package io.wispforest.owo.mixin.braid;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.render.fog.FogRenderer;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.render.GameRenderer.class)
public interface GameRendererAccessor {
    @Accessor("guiRenderer")
    GuiRenderer owo$getGuiRenderer();

    @Accessor("fogRenderer")
    FogRenderer owo$getFogRenderer();

    @Accessor("guiState")
    GuiRenderState owo$getGuiState();
}
