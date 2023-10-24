package io.wispforest.owo.serialization;

import org.jetbrains.annotations.Nullable;

public interface StructDeserializer {

    /**
     * Method that used to get the value of a field based on order of serialization
     *
     * @param endec The endec for the given field
     * @return the field value
     * @param <F>
     */
    default <F> F field(String name, Endec<F> endec){
        var value = field(name, endec, null);

        if(value == null) throw new StructDeserializerException("Unable to deserialize a required field! [Name: " + name + "]");

        return value;
    }

    /**
     * Method for checking if a field exists based on if the key exists
     * or other method employed by a given Formats {@link Deserializer}
     *
     * @param name Name of the given Field
     * @param endec The endec for the given field
     * @return an optional of the given value if present
     * @param <F>
     */
    <F> F field(String name, Endec<F> endec, @Nullable F defaultValue);

    class StructDeserializerException extends RuntimeException {
        public StructDeserializerException(String message){
            super(message);
        }
    }
}
