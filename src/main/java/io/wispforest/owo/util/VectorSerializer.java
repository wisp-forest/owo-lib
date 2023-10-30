package io.wispforest.owo.util;

import net.minecraft.nbt.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 * Utility class for reading and storing {@link Vec3d} and
 * {@link Vector3f} from and into {@link net.minecraft.nbt.NbtCompound}
 */
public final class VectorSerializer {

    private VectorSerializer() {}

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

    /**
     * Stores the given vector  as an array at the
     * given key in the given nbt compound
     *
     * @param vec3f The vector to serialize
     * @param nbt   The nbt compound to serialize into
     * @param key   The key to use
     * @return {@code nbt}
     */
    public static NbtCompound putf(NbtCompound nbt, String key, Vector3f vec3f) {

        NbtList vectorArray = new NbtList();
        vectorArray.add(NbtFloat.of(vec3f.x));
        vectorArray.add(NbtFloat.of(vec3f.y));
        vectorArray.add(NbtFloat.of(vec3f.z));

        nbt.put(key, vectorArray);

        return nbt;
    }

    /**
     * Stores the given vector  as an array at the
     * given key in the given nbt compound
     *
     * @param vec3i The vector to serialize
     * @param nbt   The nbt compound to serialize into
     * @param key   The key to use
     * @return {@code nbt}
     */
    public static NbtCompound puti(NbtCompound nbt, String key, Vec3i vec3i) {

        NbtList vectorArray = new NbtList();
        vectorArray.add(NbtInt.of(vec3i.getX()));
        vectorArray.add(NbtInt.of(vec3i.getY()));
        vectorArray.add(NbtInt.of(vec3i.getZ()));

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

    /**
     * Gets the vector stored at the given key in the
     * given nbt compound
     *
     * @param nbt The nbt compound to read from
     * @param key The key the read from
     * @return The deserialized vector
     */
    public static Vector3f getf(NbtCompound nbt, String key) {

        NbtList vectorArray = nbt.getList(key, NbtElement.FLOAT_TYPE);
        float x = vectorArray.getFloat(0);
        float y = vectorArray.getFloat(1);
        float z = vectorArray.getFloat(2);

        return new Vector3f(x, y, z);
    }

    /**
     * Gets the vector stored at the given key in the
     * given nbt compound
     *
     * @param nbt The nbt compound to read from
     * @param key The key the read from
     * @return The deserialized vector
     */
    public static Vec3i geti(NbtCompound nbt, String key) {

        NbtList vectorArray = nbt.getList(key, NbtElement.INT_TYPE);
        int x = vectorArray.getInt(0);
        int y = vectorArray.getInt(1);
        int z = vectorArray.getInt(2);

        return new Vec3i(x, y, z);
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

    /**
     * Writes the given vector into the given packet buffer
     *
     * @param vec3f  The vector to write
     * @param buffer The packet buffer to write into
     */
    public static void writef(PacketByteBuf buffer, Vector3f vec3f) {
        buffer.writeFloat(vec3f.x);
        buffer.writeFloat(vec3f.y);
        buffer.writeFloat(vec3f.z);
    }

    /**
     * Writes the given vector into the given packet buffer
     *
     * @param vec3i  The vector to write
     * @param buffer The packet buffer to write into
     */
    public static void writei(PacketByteBuf buffer, Vec3i vec3i) {
        buffer.writeInt(vec3i.getX());
        buffer.writeInt(vec3i.getY());
        buffer.writeInt(vec3i.getZ());
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
    public static Vector3f readf(PacketByteBuf buffer) {
        return new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    /**
     * Reads one vector from the given packet buffer
     *
     * @param buffer The buffer to read from
     * @return The deserialized vector
     */
    public static Vec3i readi(PacketByteBuf buffer) {
        return new Vec3i(buffer.readInt(), buffer.readInt(), buffer.readInt());
    }
}
