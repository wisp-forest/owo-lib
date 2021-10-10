package com.glisco.owo.moddata;

import com.google.gson.JsonObject;

/**
 * A class that can accept some JSON data loaded from a subdirectory
 * all other mods' {@code data} directory when instructed to using
 * {@link ModDataLoader#load(ModDataConsumer)}
 */
public interface ModDataConsumer {

    /**
     * The {@code data} subdirectory to search. For example {@code items} would
     * mean {@code .../data/{modid}/items/...}
     *
     * @return The subdirectory to load from
     */
    String getDataSubdirectory();

    /**
     * This method should process the loaded data
     *
     * @param object The .json files parsed into {@code JsonObject}s
     */
    void acceptParsedFile(JsonObject object);

}
