package io.wispforest.owo.mixin;

import io.wispforest.owo.serialization.SerializationAttribute;
import io.wispforest.owo.serialization.util.MapCarrier;
import io.wispforest.owo.serialization.endec.KeyedEndec;
import io.wispforest.owo.serialization.format.forwarding.ForwardingDeserializer;
import io.wispforest.owo.serialization.format.forwarding.ForwardingSerializer;
import io.wispforest.owo.serialization.format.nbt.NbtDeserializer;
import io.wispforest.owo.serialization.format.nbt.NbtSerializer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(NbtCompound.class)
public abstract class NbtCompoundMixin implements MapCarrier {

    @Shadow
    public abstract @Nullable NbtElement get(String key);
    @Shadow
    public abstract @Nullable NbtElement put(String key, NbtElement element);
    @Shadow
    public abstract void remove(String key);
    @Shadow
    public abstract boolean contains(String key);

    @Override
    public <T> T getWithErrors(@NotNull KeyedEndec<T> key) {
        if (!this.has(key)) return key.defaultValue();
        return key.endec().decodeFully(e -> NbtDeserializer.of(e).withAttributes(SerializationAttribute.HUMAN_READABLE), this.get(key.key()));
    }

    @Override
    public <T> void put(@NotNull KeyedEndec<T> key, @NotNull T value) {
        this.put(key.key(), key.endec().encodeFully(() -> NbtSerializer.of().withAttributes(SerializationAttribute.HUMAN_READABLE), value));
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
