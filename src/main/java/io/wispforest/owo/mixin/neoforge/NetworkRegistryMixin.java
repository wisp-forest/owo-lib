package io.wispforest.owo.mixin.neoforge;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.owo.network.neoforge.SidedPacketCodec;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = NetworkRegistry.class)
public class NetworkRegistryMixin {

    @ModifyReturnValue(method = "getCodec", at = @At(value = "RETURN", ordinal = 3))
    private static PacketCodec<? super PacketByteBuf, ? extends CustomPayload> owo$unpackSidedCodec(PacketCodec<? super PacketByteBuf, ? extends CustomPayload> original, @Local(argsOnly = true) NetworkSide side) {
        if (original instanceof SidedPacketCodec<?> sidedPacketCodec) {
            original = (PacketCodec<? super PacketByteBuf, ? extends CustomPayload>) sidedPacketCodec.getCodec(side);
        }

        return original;
    }
}
