package io.wispforest.owo.mixin.serialization;

import io.wispforest.endec.SerializationAttributes;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.endec.util.MapCarrier;
import io.wispforest.owo.serialization.format.nbt.NbtDeserializer;
import io.wispforest.owo.serialization.format.nbt.NbtSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CompoundTag.class)
public abstract class CompoundTagMixin implements MapCarrier {

    @Shadow
    public abstract @Nullable Tag get(String key);
    @Shadow
    public abstract @Nullable Tag put(String key, Tag element);
    @Shadow
    public abstract @org.jspecify.annotations.Nullable Tag remove(String key);
    @Shadow
    public abstract boolean contains(String key);

    @Override
    public <T> T getWithErrors(SerializationContext ctx, @NotNull KeyedEndec<T> key) {
        if (!this.has(key)) return key.defaultValue();
        return key.endec().decodeFully(ctx.withAttributes(SerializationAttributes.HUMAN_READABLE), NbtDeserializer::of, this.get(key.key()));
    }

    @Override
    public <T> void put(SerializationContext ctx, @NotNull KeyedEndec<T> key, @NotNull T value) {
        this.put(key.key(), key.endec().encodeFully(ctx.withAttributes(SerializationAttributes.HUMAN_READABLE), NbtSerializer::of, value));
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
