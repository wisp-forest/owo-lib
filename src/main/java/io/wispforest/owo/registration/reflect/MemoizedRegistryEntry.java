package io.wispforest.owo.registration.reflect;

import com.mojang.datafixers.util.Either;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MemoizedRegistryEntry<T> extends MemoizedEntry<T> implements RegistryEntry<T> {

    @Nullable
    private RegistryEntry<T> registryEntry = null;

    protected MemoizedRegistryEntry(Supplier<T> factory) {
        super(factory);
    }

    protected RegistryEntry<T> setEntry(RegistryEntry<T> registryEntry) {
        this.registryEntry = registryEntry;

        return this;
    }

    public RegistryEntry<T> getEntry() {
        if(this.registryEntry == null) {
            throw new IllegalStateException("Unable to get the wrapped registry entry as it has not been set yet!");
        }

        return registryEntry;
    }

    @Override
    public T value() {
        return this.getEntry().value();
    }

    @Override
    public boolean hasKeyAndValue() {
        return this.getEntry().hasKeyAndValue();
    }

    @Override
    public boolean matchesId(Identifier id) {
        return this.getEntry().matchesId(id);
    }

    @Override
    public boolean matchesKey(RegistryKey<T> key) {
        return this.getEntry().matchesKey(key);
    }

    @Override
    public boolean matches(Predicate<RegistryKey<T>> predicate) {
        return this.getEntry().matches(predicate);
    }

    @Override
    public boolean isIn(TagKey<T> tag) {
        return this.getEntry().isIn(tag);
    }

    @Override
    public boolean matches(RegistryEntry<T> entry) {
        return this.getEntry().matches(entry);
    }

    @Override
    public Stream<TagKey<T>> streamTags() {
        return this.getEntry().streamTags();
    }

    @Override
    public Either<RegistryKey<T>, T> getKeyOrValue() {
        return this.getEntry().getKeyOrValue();
    }

    @Override
    public Optional<RegistryKey<T>> getKey() {
        return this.getEntry().getKey();
    }

    @Override
    public Type getType() {
        return this.getEntry().getType();
    }

    @Override
    public boolean ownerEquals(RegistryEntryOwner<T> owner) {
        return this.getEntry().ownerEquals(owner);
    }
}
