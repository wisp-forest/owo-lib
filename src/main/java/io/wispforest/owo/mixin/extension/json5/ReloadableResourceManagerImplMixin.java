package io.wispforest.owo.mixin.extension.json5;

import io.wispforest.owo.Owo;
import io.wispforest.owo.util.DataExtensionUtil;
import net.minecraft.resource.*;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableResourceManagerImpl.class)
public abstract class ReloadableResourceManagerImplMixin {

    @Inject(method = "reload", at = @At("HEAD"))
    private static void json5$clearOptedPacks(
        Executor prepareExecutor,
        Executor applyExecutor,
        CompletableFuture<Unit> initialStage,
        List<ResourcePack> packs,
        CallbackInfoReturnable<ResourceReload> cir
    ) {
        DataExtensionUtil.JSON5_ENABLED_PACKS.clear();
        for (var pack : packs) {
            var inputSupplier = pack.openRoot(Owo.MOD_ID + "-json5");
            if (inputSupplier != null) DataExtensionUtil.JSON5_ENABLED_PACKS.add(pack);
        }
    }
}
