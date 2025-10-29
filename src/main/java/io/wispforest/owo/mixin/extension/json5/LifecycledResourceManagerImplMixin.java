package io.wispforest.owo.mixin.extension.json5;

import io.wispforest.owo.Owo;
import io.wispforest.owo.util.DataExtensionUtil;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LifecycledResourceManagerImpl.class)
public abstract class LifecycledResourceManagerImplMixin {

    @Inject(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/resource/LifecycledResourceManagerImpl;parseResourceFilter(Lnet/minecraft/resource/ResourcePack;)Lnet/minecraft/resource/metadata/ResourceFilter;"
        )
    )
    private void json5$optInPacks(ResourceType type, List<ResourcePack> packs, CallbackInfo ci) {
        for (var pack : packs) {
            var inputSupplier = pack.openRoot(Owo.MOD_ID + "-json5");
            if (inputSupplier != null) {
                DataExtensionUtil.JSON5_ENABLED_PACKS.add(pack.getId());
            } else {
                DataExtensionUtil.JSON5_ENABLED_PACKS.remove(pack.getId());
            }
        }
    }
}
