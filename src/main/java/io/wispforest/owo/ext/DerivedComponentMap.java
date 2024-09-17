package io.wispforest.owo.ext;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

@ApiStatus.Internal
public class DerivedComponentMap implements ComponentMap {
    private final ComponentMap base;
    private final ComponentMapImpl delegate;

    public DerivedComponentMap(ComponentMap base) {
        this.base = base;
        this.delegate = new ComponentMapImpl(base);
    }

    public static ComponentMap reWrapIfNeeded(ComponentMap original) {
        if (original instanceof DerivedComponentMap derived) {
            return new DerivedComponentMap(derived.base);
        } else {
            return original;
        }
    }

    public void derive(ItemStack owner) {
        delegate.setChanges(ComponentChanges.EMPTY);
        var builder = ComponentChanges.builder();
        owner.getItem().deriveStackComponents(owner.getComponents(), builder);
        delegate.setChanges(builder.build());
    }

    @Nullable
    @Override
    public <T> T get(ComponentType<? extends T> type) {
        return delegate.get(type);
    }

    @Override
    public Set<ComponentType<?>> getTypes() {
        return delegate.getTypes();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DerivedComponentMap that = (DerivedComponentMap) o;
        return Objects.equals(base, that.base);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(base);
    }
}
