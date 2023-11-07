package io.wispforest.owo.serialization.impl.nbt;

import io.wispforest.owo.serialization.MapCarrier;
import io.wispforest.owo.serialization.impl.KeyedField;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

public interface NbtMapCarrier extends MapCarrier {

    default <T> T get(@NotNull KeyedField<T> key) {
        return key.endec.decode(NbtDeserializer::of, getMap().get(key.name));
    }

    default <T> void put(@NotNull KeyedField<T> key, @NotNull T value) {
        getMap().put(key.name, key.endec.encode(NbtSerializer::of, value));
    }

    @Override
    default <T> boolean has(@NotNull KeyedField<T> key){
        return getMap().contains(key.name);
    }

    @Override
    default <T> void delete(@NotNull KeyedField<T> key) {
        getMap().remove(key.name);
    }

    default NbtCompound getMap() {
        throw new IllegalStateException("Interface default method called");
    }
}
