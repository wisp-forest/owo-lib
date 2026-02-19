package io.wispforest.owo.mixin.extension.json5;

import io.wispforest.owo.Owo;
import io.wispforest.owo.util.DataExtensionUtil;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MultiPackResourceManager.class)
public abstract class MultiPackResourceManagerMixin {

    @Inject(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/MultiPackResourceManager;getPackFilterSection(Lnet/minecraft/server/packs/PackResources;)Lnet/minecraft/server/packs/resources/ResourceFilterSection;"
        )
    )
    private static void json5$optInPacks(PackType type, List<PackResources> packs, CallbackInfo ci) {
        for (var pack : packs) {
            var inputSupplier = pack.getRootResource(Owo.MOD_ID + "-json5");
            if (inputSupplier != null) {
                DataExtensionUtil.JSON5_ENABLED_PACKS.add(pack.packId());
            } else {
                DataExtensionUtil.JSON5_ENABLED_PACKS.remove(pack.packId());
            }
        }
    }
}
