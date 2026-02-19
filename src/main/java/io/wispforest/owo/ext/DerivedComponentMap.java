package io.wispforest.owo.ext;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
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
        if (this == o) {
            return true;
        } else if (o instanceof DerivedComponentMap thatDerived) {
            return Objects.equals(base, thatDerived.base);
        } else if (o instanceof DataComponentMap.Builder.SimpleMap simpleComponentMap) {
            return Objects.equals(base, simpleComponentMap);
        }

        return o == EMPTY && this.base == EMPTY;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(base);
    }
}
