package com.glisco.owo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.math.Vec3d;

public class VectorSerializer {

    public static JsonObject toJson(Vec3d vec3d, JsonObject object, String key) {

        JsonArray vectorArray = new JsonArray();
        vectorArray.add(vec3d.x);
        vectorArray.add(vec3d.y);
        vectorArray.add(vec3d.z);

        object.add(key, vectorArray);

        return object;
    }

    public static Vec3d fromJson(JsonObject object, String key) {

        JsonArray vectorArray = object.get(key).getAsJsonArray();
        double x = vectorArray.get(0).getAsDouble();
        double y = vectorArray.get(1).getAsDouble();
        double z = vectorArray.get(2).getAsDouble();

        return new Vec3d(x, y, z);
    }

}
