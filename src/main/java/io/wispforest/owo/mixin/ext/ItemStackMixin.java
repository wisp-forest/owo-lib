package io.wispforest.owo.mixin.ext;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.owo.ext.DerivedComponentMap;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Shadow @Final
    MergedComponentMap components;

    @Unique private DerivedComponentMap derivedMap;

    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/MergedComponentMap;)V", at = @At("TAIL"))
    private void injectDerivedComponentMap(ItemConvertible item, int count, MergedComponentMap components, CallbackInfo ci) {
        var base = ((MergedComponentMapAccessor)(Object) this.components).owo$getBaseComponents();

        if (base instanceof DerivedComponentMap derived) {
            derivedMap = derived;
        } else {
            derivedMap = new DerivedComponentMap(base);
            ((MergedComponentMapAccessor)(Object) this.components).owo$setBaseComponents(derivedMap);
        }
    }

    @Inject(method = "applyChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/MergedComponentMap;applyChanges(Lnet/minecraft/component/ComponentChanges;)V", shift = At.Shift.AFTER))
    private void deriveComponents2(ComponentChanges changes, CallbackInfo ci) {
        if (derivedMap == null) return;
        derivedMap.derive((ItemStack)(Object) this);
    }

    @Inject(method = "applyUnvalidatedChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/MergedComponentMap;applyChanges(Lnet/minecraft/component/ComponentChanges;)V", shift = At.Shift.AFTER))
    private void deriveComponents3(ComponentChanges changes, CallbackInfo ci) {
        if (derivedMap == null) return;
        derivedMap.derive((ItemStack)(Object) this);
    }

    @Inject(method = "applyComponentsFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/MergedComponentMap;setAll(Lnet/minecraft/component/ComponentMap;)V", shift = At.Shift.AFTER))
    private void deriveComponents4(ComponentMap components, CallbackInfo ci) {
        if (derivedMap == null) return;
        derivedMap.derive((ItemStack)(Object) this);
    }
}
