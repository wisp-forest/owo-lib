package io.wispforest.owo.util;

import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Utility class for reading and storing {@link Vec3} and
 * {@link Vector3f} from and into {@link net.minecraft.nbt.CompoundTag}
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
    public static CompoundTag put(CompoundTag nbt, String key, Vec3 vec3d) {

        ListTag vectorArray = new ListTag();
        vectorArray.add(DoubleTag.valueOf(vec3d.x));
        vectorArray.add(DoubleTag.valueOf(vec3d.y));
        vectorArray.add(DoubleTag.valueOf(vec3d.z));

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
    public static CompoundTag putf(CompoundTag nbt, String key, Vector3f vec3f) {

        ListTag vectorArray = new ListTag();
        vectorArray.add(FloatTag.valueOf(vec3f.x));
        vectorArray.add(FloatTag.valueOf(vec3f.y));
        vectorArray.add(FloatTag.valueOf(vec3f.z));

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
    public static CompoundTag puti(CompoundTag nbt, String key, Vec3i vec3i) {

        nbt.putIntArray(key, new int[]{vec3i.getX(), vec3i.getY(), vec3i.getZ()});

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
    public static Vec3 get(CompoundTag nbt, String key) {

        ListTag vectorArray = nbt.getList(key).get();
        double x = vectorArray.getDoubleOr(0, 0d);
        double y = vectorArray.getDoubleOr(1, 0d);
        double z = vectorArray.getDoubleOr(2, 0d);

        return new Vec3(x, y, z);
    }

    /**
     * Gets the vector stored at the given key in the
     * given nbt compound
     *
     * @param nbt The nbt compound to read from
     * @param key The key the read from
     * @return The deserialized vector
     */
    public static Vector3f getf(CompoundTag nbt, String key) {

        ListTag vectorArray = nbt.getList(key).get();
        float x = vectorArray.getFloatOr(0, 0f);
        float y = vectorArray.getFloatOr(1, 0f);
        float z = vectorArray.getFloatOr(2, 0f);

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
    public static Vec3i geti(CompoundTag nbt, String key) {

        int[] vectorArray = nbt.getIntArray(key).get();
        int x = vectorArray[0];
        int y = vectorArray[1];
        int z = vectorArray[2];

        return new Vec3i(x, y, z);
    }

    /**
     * Writes the given vector into the given packet buffer
     *
     * @param vec3d  The vector to write
     * @param buffer The packet buffer to write into
     */
    public static void write(FriendlyByteBuf buffer, Vec3 vec3d) {
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
    public static void writef(FriendlyByteBuf buffer, Vector3f vec3f) {
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
    public static void writei(FriendlyByteBuf buffer, Vec3i vec3i) {
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
    public static Vec3 read(FriendlyByteBuf buffer) {
        return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    /**
     * Reads one vector from the given packet buffer
     *
     * @param buffer The buffer to read from
     * @return The deserialized vector
     */
    public static Vector3f readf(FriendlyByteBuf buffer) {
        return new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    /**
     * Reads one vector from the given packet buffer
     *
     * @param buffer The buffer to read from
     * @return The deserialized vector
     */
    public static Vec3i readi(FriendlyByteBuf buffer) {
        return new Vec3i(buffer.readInt(), buffer.readInt(), buffer.readInt());
    }
}
