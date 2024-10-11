package io.wispforest.owo.registration.reflect.entry;

import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class MemoizedEntry<T> implements Supplier<T> {

    private final Supplier<T> factory;

    @Nullable
    private T object = null;

    protected MemoizedEntry(Supplier<T> factory) {
        this.factory = factory;
    }

    public static <T> MemoizedEntry<T> of(Supplier<T> supplier) {
        if(supplier instanceof MemoizedEntry<T> memoizedSupplier) return memoizedSupplier;

        return new MemoizedEntry<>(supplier);
    }

    public static <T extends B, B> TypedRegistryEntry<T, B> ofTypedEntry(Supplier<T> supplier) {
        if(supplier instanceof MemoizedRegistryEntry memorizedRegistryEntry) {
            return memorizedRegistryEntry;
        } else if(supplier instanceof MemoizedEntry<T> memoizedSupplier) {
            supplier = memoizedSupplier.factory;
        }

        return new MemoizedRegistryEntry<>(supplier);
    }

    public static <T> RegistryEntry<T> ofEntry(Supplier<T> supplier) {
        if(supplier instanceof MemoizedRegistryEntry memorizedRegistryEntry) {
            return memorizedRegistryEntry;
        } else if(supplier instanceof MemoizedEntry<T> memoizedSupplier) {
            supplier = memoizedSupplier.factory;
        }

        return new MemoizedRegistryEntry<>(supplier);
    }

    @Override
    public T get() {
        if(this.object == null) this.object = factory.get();

        return object;
    }
}
