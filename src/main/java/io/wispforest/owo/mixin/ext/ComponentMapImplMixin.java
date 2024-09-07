package io.wispforest.owo.mixin.ext;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.wispforest.owo.ext.DerivedComponentMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PatchedDataComponentMap.class)
public class ComponentMapImplMixin {
    @ModifyExpressionValue(method = "copy", at = @At(value = "FIELD", target = "Lnet/minecraft/core/component/PatchedDataComponentMap;prototype:Lnet/minecraft/core/component/DataComponentMap;"))
    private DataComponentMap reWrapDerived(DataComponentMap original) {
        return DerivedComponentMap.reWrapIfNeeded(original);
    }
}
