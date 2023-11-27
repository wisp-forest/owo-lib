package io.wispforest.owo.mixin;

import io.wispforest.owo.nbt.NbtCarrier;
import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.serialization.MapCarrier;
import io.wispforest.owo.serialization.impl.KeyedEndec;
import io.wispforest.owo.serialization.impl.forwarding.ForwardingDeserializer;
import io.wispforest.owo.serialization.impl.forwarding.ForwardingSerializer;
import io.wispforest.owo.serialization.impl.nbt.NbtDeserializer;
import io.wispforest.owo.serialization.impl.nbt.NbtSerializer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(NbtCompound.class)
public abstract class NbtCompoundMixin implements NbtCarrier, MapCarrier {

    @Shadow
    public abstract @Nullable NbtElement get(String key);
    @Shadow
    public abstract @Nullable NbtElement put(String key, NbtElement element);
    @Shadow
    public abstract void remove(String key);
    @Shadow
    public abstract boolean contains(String key);

    // --- NbtCarrier (deprecated) ---

    @Override
    public <T> T get(@NotNull NbtKey<T> key) {
        return key.get((NbtCompound) (Object) this);
    }

    @Override
    public <T> void put(@NotNull NbtKey<T> key, @NotNull T value) {
        key.put((NbtCompound) (Object) this, value);
    }

    @Override
    public <T> void delete(@NotNull NbtKey<T> key) {
        key.delete((NbtCompound) (Object) this);
    }

    @Override
    public <T> boolean has(@NotNull NbtKey<T> key) {
        return key.isIn((NbtCompound) (Object) this);
    }

    // --- MapCarrier ---

    @Override
    public <T> T getWithErrors(@NotNull KeyedEndec<T> key) {
        if(!this.has(key)) return key.defaultValue();
        return key.endec()
                .decodeFully(e -> ForwardingDeserializer.humanReadable(NbtDeserializer.of(e)), this.get(key.key()));
    }

    @Override
    public <T> void put(@NotNull KeyedEndec<T> key, @NotNull T value) {
        this.put(key.key(), key.endec().encodeFully(() -> ForwardingSerializer.humanReadable(NbtSerializer.of()), value));
    }

    @Override
    public <T> void delete(@NotNull KeyedEndec<T> key) {
        this.remove(key.key());
    }

    @Override
    public <T> boolean has(@NotNull KeyedEndec<T> key) {
        return this.contains(key.key());
    }
}
