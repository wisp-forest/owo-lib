package io.wispforest.owo.mixin;

import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// wtf is this name mojang
@Mixin(ClientHandshakePacketListenerImpl.class)
public interface ClientHandshakePacketListenerImplAccessor {

    @Accessor("connection")
    Connection owo$getConnection();

}
