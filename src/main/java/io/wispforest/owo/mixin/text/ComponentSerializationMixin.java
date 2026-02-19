package io.wispforest.owo.mixin.text;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.wispforest.owo.text.CustomTextRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComponentSerialization.class)
public abstract class ComponentSerializationMixin {

    @Inject(method = "createCodec", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/ComponentSerialization;bootstrap(Lnet/minecraft/util/ExtraCodecs$LateBoundIdMapper;)V", shift = At.Shift.AFTER))
    private static void injectOwoCodecs(Codec<Component> selfCodec, CallbackInfoReturnable<Codec<Component>> cir, @Local ExtraCodecs.LateBoundIdMapper<String, MapCodec<? extends ComponentContents>> mapper) {
        CustomTextRegistry.inject(mapper);
    }

}

