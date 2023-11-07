package io.wispforest.owo.mixin;

import io.wispforest.owo.nbt.NbtCarrier;
import io.wispforest.owo.nbt.NbtKey;
import io.wispforest.owo.serialization.impl.nbt.NbtMapCarrier;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NbtCompound.class)
public abstract class NbtCompoundMixin implements NbtCarrier, NbtMapCarrier {
    @Shadow
    public abstract boolean contains(String key, int type);

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

    //--

    @Override
    public NbtCompound getMap() {
        return (NbtCompound) (Object) this;
    }
}
