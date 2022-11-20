package io.wispforest.owo.mixin;

import io.wispforest.owo.util.RegistryAccess;
import net.minecraft.util.Identifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.SimpleRegistry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(SimpleRegistry.class)
public class SimpleRegistryMixin<T> implements RegistryAccess.AccessibleRegistry<T> {

    @Shadow
    @Final
    private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;

    @Shadow
    @Final
    private Map<T, RegistryEntry.Reference<T>> valueToEntry;

    @Override
    public @Nullable RegistryEntry<T> getEntry(Identifier id) {
        return this.idToEntry.get(id);
    }

    @Override
    public @Nullable RegistryEntry<T> getEntry(T value) {
        return this.valueToEntry.get(value);
    }
}
