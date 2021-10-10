package com.glisco.owo.blockentity;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.nbt.NbtCompound;

/**
 * A simple extension of Fabric's {@link BlockEntityClientSerializable} that uses
 * the default {@link net.minecraft.block.entity.BlockEntity#readNbt(NbtCompound)} and
 * {@link net.minecraft.block.entity.BlockEntity#writeNbt(NbtCompound)} methods for
 * sending data to the client
 */
public interface SimpleSerializableBlockEntity extends BlockEntityClientSerializable {

    void readNbt(NbtCompound nbt);

    NbtCompound writeNbt(NbtCompound nbt);

    @Override
    default void fromClientTag(NbtCompound tag) {
        readNbt(tag);
    }

    @Override
    default NbtCompound toClientTag(NbtCompound tag) {
        return writeNbt(tag);
    }
}
