package io.wispforest.owo.registration.reflect.entry;

import com.mojang.datafixers.util.Either;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MemoizedRegistryEntry<T extends B, B> extends MemoizedEntry<B> implements TypedRegistryEntry<T, B> {

    @Nullable
    private RegistryEntry<T> registryEntry = null;

    protected MemoizedRegistryEntry(Supplier<T> factory) {
        super(factory::get);
    }

    @ApiStatus.Internal
    public final void setEntry(RegistryEntry<T> registryEntry) {
        this.registryEntry = registryEntry;
    }

    public void setup(RegistryKey<B> registryKey) {
       // NO-OP
    }

    private RegistryEntry<T> getTypedEntry() {
        if(this.registryEntry == null) {
            throw new IllegalStateException("Unable to get the wrapped registry entry as it has not been set yet!");
        }

        return registryEntry;
    }

    public final RegistryEntry<B> getBaseEntry() {
        return (RegistryEntry<B>) getTypedEntry();
    }

    @Override
    public T value() {
        return this.getTypedEntry().value();
    }

    @Override
    public boolean hasKeyAndValue() {
        return this.getBaseEntry().hasKeyAndValue();
    }

    @Override
    public boolean matchesId(Identifier id) {
        return this.getBaseEntry().matchesId(id);
    }

    @Override
    public boolean matchesKey(RegistryKey<B> key) {
        return this.getBaseEntry().matchesKey(key);
    }

    @Override
    public boolean matches(Predicate<RegistryKey<B>> predicate) {
        return this.getBaseEntry().matches(predicate);
    }

    @Override
    public boolean isIn(TagKey<B> tag) {
        return this.getBaseEntry().isIn(tag);
    }

    @Override
    public boolean matches(RegistryEntry<B> entry) {
        return this.getBaseEntry().matches(entry);
    }

    @Override
    public Stream<TagKey<B>> streamTags() {
        return this.getBaseEntry().streamTags();
    }

    @Override
    public Either<RegistryKey<B>, B> getKeyOrValue() {
        return this.getBaseEntry().getKeyOrValue();
    }

    @Override
    public Optional<RegistryKey<B>> getKey() {
        return this.getBaseEntry().getKey();
    }

    @Override
    public Type getType() {
        return this.getBaseEntry().getType();
    }

    @Override
    public boolean ownerEquals(RegistryEntryOwner<B> owner) {
        return this.getBaseEntry().ownerEquals(owner);
    }
}
