package io.wispforest.owo.mixin;

import io.wispforest.owo.nbt.NbtCarrier;
import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.serialization.MapCarrier;
import io.wispforest.owo.serialization.impl.KeyedEndec;
import io.wispforest.owo.serialization.impl.forwarding.ForwardingDeserializer;
import io.wispforest.owo.serialization.impl.forwarding.ForwardingSerializer;
import io.wispforest.owo.serialization.impl.nbt.NbtDeserializer;
import io.wispforest.owo.serialization.impl.nbt.NbtSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements NbtCarrier, MapCarrier {

    @Shadow
    private @Nullable NbtCompound nbt;

    @Shadow
    public abstract NbtCompound getOrCreateNbt();

    // --- NbtCarrier (deprecated) ---

    @Override
    public <T> T get(@NotNull NbtKey<T> key) {
        return key.get(this.getOrCreateNbt());
    }

    @Override
    public <T> void put(@NotNull NbtKey<T> key, @NotNull T value) {
        key.put(this.getOrCreateNbt(), value);
    }

    @Override
    public <T> void delete(@NotNull NbtKey<T> key) {
        if (this.nbt == null) return;
        key.delete(this.nbt);
    }

    @Override
    public <T> boolean has(@NotNull NbtKey<T> key) {
        return this.nbt != null && key.isIn(this.nbt);
    }

    // --- MapCarrier ---

    @Override
    public <T> T getWithErrors(@NotNull KeyedEndec<T> key) {
        if(!this.has(key)) return key.defaultValue();
        return key.endec()
                .decodeFully(e -> ForwardingDeserializer.humanReadable(NbtDeserializer.of(e)), this.nbt.get(key.key()));
    }

    @Override
    public <T> void put(@NotNull KeyedEndec<T> key, @NotNull T value) {
        this.getOrCreateNbt()
                .put(key.key(), key.endec().encodeFully(() -> ForwardingSerializer.humanReadable(NbtSerializer.of()), value));
    }

    @Override
    public <T> void delete(@NotNull KeyedEndec<T> key) {
        if (this.nbt == null) return;
        this.nbt.remove(key.key());
    }

    @Override
    public <T> boolean has(@NotNull KeyedEndec<T> key) {
        return this.nbt != null && this.nbt.contains(key.key());
    }
}
