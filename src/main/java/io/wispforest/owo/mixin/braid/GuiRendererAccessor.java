package io.wispforest.owo.mixin.braid;

import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.neoforged.neoforge.client.gui.PictureInPictureRendererPool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = GuiRenderer.class, priority = 1100)
public interface GuiRendererAccessor {
    @Accessor("renderState")
    GuiRenderState owo$getRenderState();

    @Accessor("cubeMap")
    CubeMap owo$getCubeMap();

    @Accessor("pictureInPictureRendererPools")
    Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererPool<?>> owo$getPictureInPictureRenderers();
}
