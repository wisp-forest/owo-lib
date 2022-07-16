package io.wispforest.owo.persistence;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentState;

/**
 * A wrapper for PersistentState that simplifies the process of registration and usage. MUST
 * @param <T> The class that is extending OwoPersistentState
 */
public abstract class OwoPersistentState<T> extends PersistentState {

    /**
     * The method that turns an NbtCompound into an instance of your class.
     * @param compound The NbtCompound
     * @return Instance of your class.
     */
    public abstract OwoPersistentState<T> gather(NbtCompound compound);
}
