package io.wispforest.owo.mixin.ui.access;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface EntityRendererAccessor<T extends Entity> {
    @Invoker("getNameTag")
    Component owo$getNameTag(T entity);
}
