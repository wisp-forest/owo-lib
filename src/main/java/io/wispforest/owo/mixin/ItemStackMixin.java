package io.wispforest.owo.mixin;

import io.wispforest.owo.nbt.NbtCarrier;
import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.serialization.impl.KeyedField;
import io.wispforest.owo.serialization.impl.nbt.NbtMapCarrier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements NbtCarrier, NbtMapCarrier {

    @Shadow
    private @Nullable NbtCompound nbt;

    @Shadow
    public abstract NbtCompound getOrCreateNbt();

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

    //--

    @Override
    public NbtCompound getMap() {
        return getOrCreateNbt();
    }

    @Override
    public <T> void delete(@NotNull KeyedField<T> key) {
        if (this.nbt == null) return;
        NbtMapCarrier.super.delete(key);
    }

    @Override
    public <T> boolean has(@NotNull KeyedField<T> key) {
        return this.nbt != null && NbtMapCarrier.super.has(key);
    }
}
