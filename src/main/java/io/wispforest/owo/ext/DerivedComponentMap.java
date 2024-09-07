package io.wispforest.owo.ext;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@ApiStatus.Internal
public class DerivedComponentMap implements DataComponentMap {
    private final DataComponentMap base;
    private final PatchedDataComponentMap delegate;

    public DerivedComponentMap(DataComponentMap base) {
        this.base = base;
        this.delegate = new PatchedDataComponentMap(base);
    }

    public static DataComponentMap reWrapIfNeeded(DataComponentMap original) {
        if (original instanceof DerivedComponentMap derived) {
            return new DerivedComponentMap(derived.base);
        } else {
            return original;
        }
    }

    public void derive(ItemStack owner) {
        delegate.restorePatch(DataComponentPatch.EMPTY);
        var builder = DataComponentPatch.builder();
        owner.getItem().deriveStackComponents(owner.getComponents(), builder);
        delegate.restorePatch(builder.build());
    }

    @Nullable
    @Override
    public <T> T get(DataComponentType<? extends T> type) {
        return delegate.get(type);
    }

    @Override
    public Set<DataComponentType<?>> keySet() {
        return delegate.keySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DerivedComponentMap that = (DerivedComponentMap) o;
        return base.equals(that.base) && delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        int result = base.hashCode();
        result = 31 * result + delegate.hashCode();
        return result;
    }
}
