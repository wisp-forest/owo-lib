package io.wispforest.owo.mixin;

import io.wispforest.owo.serialization.util.MapCarrier;
import io.wispforest.owo.serialization.endec.KeyedEndec;
import io.wispforest.owo.serialization.format.forwarding.ForwardingDeserializer;
import io.wispforest.owo.serialization.format.forwarding.ForwardingSerializer;
import io.wispforest.owo.serialization.format.nbt.NbtDeserializer;
import io.wispforest.owo.serialization.format.nbt.NbtSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements MapCarrier {

    @Shadow
    private @Nullable NbtCompound nbt;

    @Shadow
    public abstract NbtCompound getOrCreateNbt();

    @Override
    public <T> T getWithErrors(@NotNull KeyedEndec<T> key) {
        if (!this.has(key)) return key.defaultValue();
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
