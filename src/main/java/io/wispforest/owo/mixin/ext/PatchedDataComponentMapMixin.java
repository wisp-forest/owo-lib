package io.wispforest.owo.mixin.ext;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.owo.ext.DerivedComponentMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PatchedDataComponentMap.class)
public class PatchedDataComponentMapMixin {
    @ModifyExpressionValue(method = "copy", at = @At(value = "FIELD", target = "Lnet/minecraft/core/component/PatchedDataComponentMap;prototype:Lnet/minecraft/core/component/DataComponentMap;"))
    private DataComponentMap reWrapDerived(DataComponentMap original) {
        return DerivedComponentMap.reWrapIfNeeded(original);
    }

    @WrapOperation(method = "equals", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/component/DataComponentMap;equals(Ljava/lang/Object;)Z"))
    private boolean prioritiseDerivedMap(DataComponentMap instance, Object object, Operation<Boolean> original) {
        return (object instanceof DerivedComponentMap derivedComponentMap)
            ? original.call(derivedComponentMap, instance)
            : original.call(instance, object);
    }
}
