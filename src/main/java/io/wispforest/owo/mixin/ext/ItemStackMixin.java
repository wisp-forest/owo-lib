package io.wispforest.owo.mixin.ext;

import io.wispforest.owo.ext.DerivedComponentMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Shadow @Final
    PatchedDataComponentMap components;

    @Unique private DerivedComponentMap derivedMap;

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("TAIL"))
    private void injectDerivedComponentMap(ItemLike item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        var base = ((PatchedDataComponentMapAccessor)(Object) this.components).owo$getPrototype();

        if (base instanceof DerivedComponentMap derived) {
            derivedMap = derived;
        } else {
            derivedMap = new DerivedComponentMap(base);
            ((PatchedDataComponentMapAccessor)(Object) this.components).owo$setPrototype(derivedMap);
        }
    }

    @Inject(method = "applyComponentsAndValidate", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/component/PatchedDataComponentMap;applyPatch(Lnet/minecraft/core/component/DataComponentPatch;)V", shift = At.Shift.AFTER))
    private void deriveComponents2(DataComponentPatch changes, CallbackInfo ci) {
        if (derivedMap == null) return;
        derivedMap.derive((ItemStack)(Object) this);
    }

    @Inject(method = "applyComponents(Lnet/minecraft/core/component/DataComponentPatch;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/component/PatchedDataComponentMap;applyPatch(Lnet/minecraft/core/component/DataComponentPatch;)V", shift = At.Shift.AFTER))
    private void deriveComponents3(DataComponentPatch changes, CallbackInfo ci) {
        if (derivedMap == null) return;
        derivedMap.derive((ItemStack)(Object) this);
    }

    @Inject(method = "applyComponents(Lnet/minecraft/core/component/DataComponentMap;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/component/PatchedDataComponentMap;setAll(Lnet/minecraft/core/component/DataComponentMap;)V", shift = At.Shift.AFTER))
    private void deriveComponents4(DataComponentMap components, CallbackInfo ci) {
        if (derivedMap == null) return;
        derivedMap.derive((ItemStack)(Object) this);
    }
}
