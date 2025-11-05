package io.wispforest.owo.mixin.braid;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = GuiRenderer.class, priority = 1100)
public interface GuiRendererAccessor {
    @Accessor("state")
    GuiRenderState owo$getState();

    @Accessor("specialElementRenderers")
    Map<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRenderer<?>> owo$getSpecialElementRenderers();
}
