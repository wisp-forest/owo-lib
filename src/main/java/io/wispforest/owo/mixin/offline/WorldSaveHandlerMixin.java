package io.wispforest.owo.mixin.offline;

import io.wispforest.owo.offline.DataSavedEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PlayerSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerSaveHandler.class)
public class WorldSaveHandlerMixin {
    @Inject(method = "savePlayerData", at = @At(value = "INVOKE", target = "Ljava/nio/file/Files;createTempFile(Ljava/nio/file/Path;Ljava/lang/String;Ljava/lang/String;[Ljava/nio/file/attribute/FileAttribute;)Ljava/nio/file/Path;"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onPlayerDataSaved(PlayerEntity player, CallbackInfo ci, NbtCompound tag) {
        DataSavedEvents.PLAYER_DATA.invoker().onSaved(player.getUuid(), tag);
    }
}
