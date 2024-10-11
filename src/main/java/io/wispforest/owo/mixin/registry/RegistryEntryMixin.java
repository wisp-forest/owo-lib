package io.wispforest.owo.mixin.registry;

import io.wispforest.owo.util.pond.OwoInjectedSupplier;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Supplier;

@Mixin(RegistryEntry.class)
public interface RegistryEntryMixin<T> extends OwoInjectedSupplier<T> {

    @Shadow T value();

    @Override
    default T get() {
        return this.value();
    }
}
