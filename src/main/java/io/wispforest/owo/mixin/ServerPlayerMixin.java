package io.wispforest.owo.mixin;

import io.wispforest.owo.util.pond.OwoAbstractContainerMenuExtension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "initMenu", at = @At("HEAD"))
    private void attachScreenHandler(AbstractContainerMenu screenHandler, CallbackInfo ci) {
        ((OwoAbstractContainerMenuExtension) screenHandler).owo$attachToPlayer((ServerPlayer) (Object) this);
    }
}
