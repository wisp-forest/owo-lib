package io.wispforest.owo.mixin.ext;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.owo.ext.DerivedComponentMap;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.MergedComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MergedComponentMap.class)
public class MergedComponentMapMixin {
    @ModifyExpressionValue(method = "copy", at = @At(value = "FIELD", target = "Lnet/minecraft/component/MergedComponentMap;baseComponents:Lnet/minecraft/component/ComponentMap;"))
    private ComponentMap reWrapDerived(ComponentMap original) {
        return DerivedComponentMap.reWrapIfNeeded(original);
    }

    @WrapOperation(method = "equals", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/ComponentMap;equals(Ljava/lang/Object;)Z"))
    private boolean prioritiseDerivedMap(ComponentMap instance, Object object, Operation<Boolean> original) {
        return (object instanceof DerivedComponentMap derivedComponentMap)
            ? original.call(derivedComponentMap, instance)
            : original.call(instance, object);
    }
}
