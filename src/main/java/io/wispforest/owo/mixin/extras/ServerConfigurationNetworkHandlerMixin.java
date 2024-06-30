package io.wispforest.owo.mixin.extras;

import io.wispforest.owo.extras.ServerConfigurationConnectionEvents;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerConfigurationNetworkHandler.class)
public abstract class ServerConfigurationNetworkHandlerMixin extends ServerCommonNetworkHandler {

    @Shadow
    public abstract boolean isConnectionOpen();

    public ServerConfigurationNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Inject(method = "sendConfigurations", at = @At("TAIL"))
    private void onClientReady(CallbackInfo ci) {
        ServerConfigurationConnectionEvents.CONFIGURE.invoker().onSendConfiguration((ServerConfigurationNetworkHandler) (Object) this, this.server);
    }
}
