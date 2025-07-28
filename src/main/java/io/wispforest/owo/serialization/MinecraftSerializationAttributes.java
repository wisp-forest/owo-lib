package io.wispforest.owo.serialization;

import io.wispforest.endec.SerializationAttribute;

public final class MinecraftSerializationAttributes {
    private MinecraftSerializationAttributes() { }

    /**
     * This format will be sent over the network.
     * <p>
     * Registries and block states can be represented as integer IDs.
     */
    public static final SerializationAttribute.Marker NETWORK = SerializationAttribute.marker("network");
}
