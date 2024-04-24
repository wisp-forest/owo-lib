package io.wispforest.owo.serialization;

public final class SerializationAttributes {

    /**
     * This format is self-describing - that is, the deserializer supports
     * {@link SelfDescribedDeserializer#readAny(Serializer)} to decode its current element
     * based purely on structure data stored in the input alone
     * <p>
     * Endecs should use this to make decisions like storing a hierarchical data
     * structure without writing identifying data
     */
    public static final SerializationAttribute.Marker SELF_DESCRIBING = SerializationAttribute.marker("self_describing");

    /**
     * This format is intended to be human-readable (and potentially -editable)
     * <p>
     * Endecs should use this to make decisions like representing a
     * {@link net.minecraft.util.math.BlockPos} as an integer sequence instead of packing it into a long
     */
    public static final SerializationAttribute.Marker HUMAN_READABLE = SerializationAttribute.marker("human_readable");

    public static final SerializationAttribute.WithValue<RegistriesAttribute> REGISTRIES = SerializationAttribute.withValue("registries");

    private SerializationAttributes() {}
}
