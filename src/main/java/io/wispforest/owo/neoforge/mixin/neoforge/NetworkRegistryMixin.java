package io.wispforest.owo.neoforge.mixin.neoforge;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.owo.neoforge.api.SidedStreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = NetworkRegistry.class)
public abstract class NetworkRegistryMixin {
    @ModifyReturnValue(method = "getCodec", at = @At(value = "RETURN", ordinal = 3))
    private static StreamCodec<? super FriendlyByteBuf, ? extends CustomPacketPayload> owo$unpackSidedCodec(@Nullable StreamCodec<? super FriendlyByteBuf, ? extends CustomPacketPayload> original, @Local(argsOnly = true) PacketFlow flow) {
        return (original instanceof SidedStreamCodec<?> sidedPacketCodec) ? sidedPacketCodec.getCodec(flow) : original;
    }
}
