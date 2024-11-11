package io.wispforest.owo.mixin.neoforge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.owo.network.neoforge.SidedPacketCodec;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.codec.PacketCodec;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.network.registration.PayloadRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = NetworkRegistry.class, remap = false)
public class NetworkRegistryMixin {

    @WrapOperation(method = "getCodec", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/network/registration/PayloadRegistration;codec()Lnet/minecraft/network/codec/PacketCodec;"))
    private static PacketCodec owo$unpackSidedCodec(PayloadRegistration instance, Operation<PacketCodec> original, @Local(argsOnly = true) NetworkSide flow) {
        var codec = original.call(instance);

        if (codec instanceof SidedPacketCodec<?> sidedPacketCodec) {
            codec = sidedPacketCodec.getCodec(flow);
        }

        return codec;
    }
}
