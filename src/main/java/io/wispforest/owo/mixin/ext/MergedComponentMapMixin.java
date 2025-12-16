package io.wispforest.owo.mixin.ext;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.wispforest.owo.ext.DerivedComponentMap;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.MergedComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(MergedComponentMap.class)
public abstract class MergedComponentMapMixin {

    @Shadow
    private ComponentMap baseComponents;

    @Shadow
    public abstract ComponentChanges getChanges();

    @ModifyExpressionValue(method = "copy", at = @At(value = "FIELD", target = "Lnet/minecraft/component/MergedComponentMap;baseComponents:Lnet/minecraft/component/ComponentMap;"))
    private ComponentMap reWrapDerived(ComponentMap original) {
        return DerivedComponentMap.reWrapIfNeeded(original);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MergedComponentMap other)) return false;

        ComponentMap otherBase = ((MergedComponentMapAccessor)(Object) other).owo$getBaseComponents();
        ComponentChanges otherChanges = other.getChanges();

        if (this.baseComponents instanceof DerivedComponentMap derivedBase) {
            if (!derivedBase.equals(otherBase)) return false;
        } else if (otherBase instanceof DerivedComponentMap derivedOther) {
            if (!derivedOther.equals(this.baseComponents)) return false;
        } else {
            if (!Objects.equals(this.baseComponents, otherBase)) return false;
        }

        return Objects.equals(this.getChanges(), otherChanges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseComponents, getChanges());
    }
}
