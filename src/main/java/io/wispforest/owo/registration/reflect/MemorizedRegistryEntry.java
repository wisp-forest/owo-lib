package io.wispforest.owo.registration.reflect;

import com.mojang.datafixers.util.Either;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MemorizedRegistryEntry<T> extends MemorizedEntry<T> implements RegistryEntry<T> {

    @Nullable
    private RegistryEntry<T> registryEntry = null;

    protected MemorizedRegistryEntry(Supplier<T> factory) {
        super(factory);
    }

    protected RegistryEntry<T> setEntry(RegistryEntry<T> registryEntry) {
        this.registryEntry = registryEntry;

        return this;
    }

    @Override
    public T value() {
        return this.registryEntry.value();
    }

    @Override
    public boolean hasKeyAndValue() {
        return this.registryEntry.hasKeyAndValue();
    }

    @Override
    public boolean matchesId(Identifier id) {
        return this.registryEntry.matchesId(id);
    }

    @Override
    public boolean matchesKey(RegistryKey<T> key) {
        return this.registryEntry.matchesKey(key);
    }

    @Override
    public boolean matches(Predicate<RegistryKey<T>> predicate) {
        return this.registryEntry.matches(predicate);
    }

    @Override
    public boolean isIn(TagKey<T> tag) {
        return this.registryEntry.isIn(tag);
    }

    @Override
    public boolean matches(RegistryEntry<T> entry) {
        return this.registryEntry.matches(entry);
    }

    @Override
    public Stream<TagKey<T>> streamTags() {
        return this.registryEntry.streamTags();
    }

    @Override
    public Either<RegistryKey<T>, T> getKeyOrValue() {
        return this.registryEntry.getKeyOrValue();
    }

    @Override
    public Optional<RegistryKey<T>> getKey() {
        return this.registryEntry.getKey();
    }

    @Override
    public Type getType() {
        return this.registryEntry.getType();
    }

    @Override
    public boolean ownerEquals(RegistryEntryOwner<T> owner) {
        return this.registryEntry.ownerEquals(owner);
    }

    @Override
    public boolean equals(Object obj) {
        return this.registryEntry.equals(obj);
    }
}
