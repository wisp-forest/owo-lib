package io.wispforest.owo.mixin.ext;

import io.wispforest.owo.ext.DerivedComponentMap;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Shadow @Final ComponentMapImpl components;

    @Unique private DerivedComponentMap owo$derivedMap;

    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/ComponentMapImpl;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;postProcessComponents(Lnet/minecraft/item/ItemStack;)V"))
    private void injectDerivedComponentMap(ItemConvertible item, int count, ComponentMapImpl components, CallbackInfo ci) {
        var base = ((ComponentMapImplAccessor)(Object) this.components).owo$getBaseComponents();

        if (base instanceof DerivedComponentMap derived) {
            owo$derivedMap = derived;
        } else {
            owo$derivedMap = new DerivedComponentMap(base);
            ((ComponentMapImplAccessor)(Object) this.components).owo$setBaseComponents(owo$derivedMap);
        }
    }

    // TODO: for some reason mixin doesn't like it if I put all the injects in one method.
    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/ComponentMapImpl;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;postProcessComponents(Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER))
    private void deriveComponents1(ItemConvertible item, int count, ComponentMapImpl components, CallbackInfo ci) {
        if (owo$derivedMap == null) return;
        owo$derivedMap.derive((ItemStack)(Object) this);
    }

    @Inject(method = "applyChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;postProcessComponents(Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER))
    private void deriveComponents2(ComponentChanges changes, CallbackInfo ci) {
        if (owo$derivedMap == null) return;
        owo$derivedMap.derive((ItemStack)(Object) this);
    }

    @Inject(method = "applyUnvalidatedChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;postProcessComponents(Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER))
    private void deriveComponents3(ComponentChanges changes, CallbackInfo ci) {
        if (owo$derivedMap == null) return;
        owo$derivedMap.derive((ItemStack)(Object) this);
    }

    @Inject(method = "applyComponentsFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;postProcessComponents(Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER))
    private void deriveComponents4(ComponentMap components, CallbackInfo ci) {
        if (owo$derivedMap == null) return;
        owo$derivedMap.derive((ItemStack)(Object) this);
    }
}
