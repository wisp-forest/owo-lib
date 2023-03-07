package io.wispforest.owo.mixin;

import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ServerPlayNetworkHandler.class, ServerLoginNetworkHandler.class, ClientLoginNetworkHandler.class})
public interface NetworkHandlerAccessor {

    @Accessor("connection")
    ClientConnection owo$getConnection();

}
