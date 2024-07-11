package io.wispforest.owo.registration.reflect;

import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class MemorizedEntry<T> implements Supplier<T> {

    private final Supplier<T> factory;

    @Nullable
    private T object = null;

    protected MemorizedEntry(Supplier<T> factory) {
        this.factory = factory;
    }

    public static <T> MemorizedEntry<T> of(Supplier<T> supplier) {
        if(supplier instanceof MemorizedEntry<T> memoizedSupplier) return memoizedSupplier;

        return new MemorizedEntry<>(supplier);
    }

    public static <T> RegistryEntry<T> ofEntry(Supplier<T> supplier) {
        if(supplier instanceof MemorizedRegistryEntry<T> memorizedRegistryEntry) {
            return memorizedRegistryEntry;
        } else if(supplier instanceof MemorizedEntry<T> memoizedSupplier) {
            supplier = memoizedSupplier.factory;
        }

        return new MemorizedRegistryEntry<>(supplier);
    }

    @Override
    public T get() {
        if(this.object == null) this.object = factory.get();

        return object;
    }
}
