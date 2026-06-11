package io.wispforest.owo.neoforge.mixin.neoforge;

import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.neoforged.neoforge.client.gui.PictureInPictureRendererPool;
import net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PictureInPictureRendererPool.class)
public interface PictureInPictureRendererPoolAccessor<T extends PictureInPictureRenderState> {
    @Accessor("factory")
    PictureInPictureRendererRegistration<T> owo$factory();
}
