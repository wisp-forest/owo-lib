package io.wispforest.owo.mixin.text;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.wispforest.owo.text.CustomTextRegistry;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.TextContent;
import net.minecraft.util.dynamic.Codecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextCodecs.class)
public abstract class TextCodecsMixin {

    @Inject(method = "createCodec", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/TextCodecs;registerTypes(Lnet/minecraft/util/dynamic/Codecs$IdMapper;)V", shift = At.Shift.AFTER))
    private static void injectOwoCodecs(Codec<Text> selfCodec, CallbackInfoReturnable<Codec<Text>> cir, @Local Codecs.IdMapper<String, MapCodec<? extends TextContent>> mapper) {
        CustomTextRegistry.inject(mapper);
    }

}

