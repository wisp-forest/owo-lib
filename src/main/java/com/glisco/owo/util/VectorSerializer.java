package com.glisco.owo.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.Vec3d;

/**
 * Utility class for reading and storing {@link Vec3d}
 * from and into {@link net.minecraft.nbt.NbtCompound}
 */
public class VectorSerializer {

    /**
     * Stores the given vector  as an array at the
     * given key in the given nbt compound
     *
     * @param vec3d The vector to serialize
     * @param nbt   The nbt compound to serialize into
     * @param key   The key to use
     * @return {@code nbt}
     */
    public static NbtCompound store(Vec3d vec3d, NbtCompound nbt, String key) {

        NbtList vectorArray = new NbtList();
        vectorArray.add(NbtDouble.of(vec3d.x));
        vectorArray.add(NbtDouble.of(vec3d.y));
        vectorArray.add(NbtDouble.of(vec3d.z));

        nbt.put(key, vectorArray);

        return nbt;
    }

    /**
     * Gets the vector stored at the given key in the
     * given nbt compound
     *
     * @param nbt The nbt compound to read from
     * @param key The key the read from
     * @return The deserialized vector
     */
    public static Vec3d get(NbtCompound nbt, String key) {

        NbtList vectorArray = nbt.getList(key, NbtElement.DOUBLE_TYPE);
        double x = vectorArray.getDouble(0);
        double y = vectorArray.getDouble(1);
        double z = vectorArray.getDouble(2);

        return new Vec3d(x, y, z);
    }

}
