package com.glisco.owo.blockentity;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.nbt.NbtCompound;

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
