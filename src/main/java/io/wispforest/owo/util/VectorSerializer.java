package io.wispforest.owo.util;

import net.minecraft.nbt.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

/**
 * Utility class for reading and storing {@link Vec3d} and
 * {@link Vec3f} from and into {@link net.minecraft.nbt.NbtCompound}
 */
public class VectorSerializer {

    /**
     * Stores the given vector  as an array at the
     * given key in the given nbt compound
     *
     * @param nbt   The nbt compound to serialize into
     * @param key   The key to use
     * @param vec3d The vector to serialize
     * @return {@code nbt}
     */
    public static NbtCompound put(NbtCompound nbt, String key, Vec3d vec3d) {

        NbtList vectorArray = new NbtList();
        vectorArray.add(NbtDouble.of(vec3d.x));
        vectorArray.add(NbtDouble.of(vec3d.y));
        vectorArray.add(NbtDouble.of(vec3d.z));

        nbt.put(key, vectorArray);

        return nbt;
    }

    @Deprecated(forRemoval = true)
    public static NbtCompound store(Vec3d vec3d, NbtCompound nbt, String key) {
        return put(nbt, key, vec3d);
    }

    /**
     * Stores the given vector  as an array at the
     * given key in the given nbt compound
     *
     * @param vec3f The vector to serialize
     * @param nbt   The nbt compound to serialize into
     * @param key   The key to use
     * @return {@code nbt}
     */
    public static NbtCompound putf(NbtCompound nbt, String key, Vec3f vec3f) {

        NbtList vectorArray = new NbtList();
        vectorArray.add(NbtFloat.of(vec3f.getX()));
        vectorArray.add(NbtFloat.of(vec3f.getY()));
        vectorArray.add(NbtFloat.of(vec3f.getZ()));

        nbt.put(key, vectorArray);

        return nbt;
    }

    @Deprecated(forRemoval = true)
    public static NbtCompound storef(Vec3f vec3f, NbtCompound nbt, String key) {
        return putf(nbt, key, vec3f);
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

    /**
     * Gets the vector stored at the given key in the
     * given nbt compound
     *
     * @param nbt The nbt compound to read from
     * @param key The key the read from
     * @return The deserialized vector
     */
    public static Vec3f getf(NbtCompound nbt, String key) {

        NbtList vectorArray = nbt.getList(key, NbtElement.FLOAT_TYPE);
        float x = vectorArray.getFloat(0);
        float y = vectorArray.getFloat(1);
        float z = vectorArray.getFloat(2);

        return new Vec3f(x, y, z);
    }

    /**
     * Writes the given vector into the given packet buffer
     *
     * @param vec3d  The vector to write
     * @param buffer The packet buffer to write into
     */
    public static void write(PacketByteBuf buffer, Vec3d vec3d) {
        buffer.writeDouble(vec3d.x);
        buffer.writeDouble(vec3d.y);
        buffer.writeDouble(vec3d.z);
    }

    @Deprecated(forRemoval = true)
    public static void write(Vec3d vec3d, PacketByteBuf buffer) {
        write(buffer, vec3d);
    }

    /**
     * Writes the given vector into the given packet buffer
     *
     * @param vec3f  The vector to write
     * @param buffer The packet buffer to write into
     */
    public static void writef(PacketByteBuf buffer, Vec3f vec3f) {
        buffer.writeFloat(vec3f.getX());
        buffer.writeFloat(vec3f.getY());
        buffer.writeFloat(vec3f.getZ());
    }

    @Deprecated(forRemoval = true)
    public static void writef(Vec3f vec3f, PacketByteBuf buffer) {
        writef(buffer, vec3f);
    }

    /**
     * Reads one vector from the given packet buffer
     *
     * @param buffer The buffer to read from
     * @return The deserialized vector
     */
    public static Vec3d read(PacketByteBuf buffer) {
        return new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    /**
     * Reads one vector from the given packet buffer
     *
     * @param buffer The buffer to read from
     * @return The deserialized vector
     */
    public static Vec3f readf(PacketByteBuf buffer) {
        return new Vec3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

}
