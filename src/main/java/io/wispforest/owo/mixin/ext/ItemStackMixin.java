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

    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/MergedComponentMap;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;postProcessComponents(Lnet/minecraft/item/ItemStack;)V"))
    private void injectDerivedComponentMap(ItemConvertible item, int count, MergedComponentMap components, CallbackInfo ci) {
        var base = ((MergedComponentMapAccessor)(Object) this.components).owo$getBaseComponents();

        if (base instanceof DerivedComponentMap derived) {
            derivedMap = derived;
        } else {
            derivedMap = new DerivedComponentMap(base);
            ((MergedComponentMapAccessor)(Object) this.components).owo$setBaseComponents(derivedMap);
        }
    }

    @WrapOperation(
        method = {
            "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/MergedComponentMap;)V",
            "applyChanges",
            "applyUnvalidatedChanges",
            "applyComponentsFrom"
        }, at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;postProcessComponents(Lnet/minecraft/item/ItemStack;)V"))
    private void deriveComponents1(Item instance, ItemStack stack, Operation<Void> original) {
        original.call(instance, stack);
        if (derivedMap == null) return;
        derivedMap.derive((ItemStack)(Object) this);
    }
}
