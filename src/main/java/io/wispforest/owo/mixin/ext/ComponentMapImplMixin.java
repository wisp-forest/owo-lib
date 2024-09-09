package io.wispforest.owo.mixin.ext;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.wispforest.owo.ext.DerivedComponentMap;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentMapImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ComponentMapImpl.class)
public class ComponentMapImplMixin {
    @ModifyExpressionValue(method = "copy", at = @At(value = "FIELD", target = "Lnet/minecraft/component/ComponentMapImpl;baseComponents:Lnet/minecraft/component/ComponentMap;"))
    private ComponentMap reWrapDerived(ComponentMap original) {
        return DerivedComponentMap.reWrapIfNeeded(original);
    }
}
