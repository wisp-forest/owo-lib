package io.wispforest.owo.serialization.endec;

import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.SerializationAttribute;
import io.wispforest.owo.serialization.Serializer;

import java.util.LinkedHashMap;
import java.util.Map;

public class AttributeEndecBuilder<T> {

    private final Map<SerializationAttribute, Endec<T>> branches = new LinkedHashMap<>();

    public AttributeEndecBuilder(Endec<T> endec, SerializationAttribute attribute) {
        this.branches.put(attribute, endec);
    }

    public AttributeEndecBuilder<T> orElseIf(Endec<T> endec, SerializationAttribute attribute) {
        if (this.branches.containsKey(attribute)) {
            throw new IllegalStateException("Cannot have more than one branch for attribute " + attribute.name);
        }

        this.branches.put(attribute, endec);
        return this;
    }

    public Endec<T> orElse(Endec<T> endec) {
        return new Endec<>() {
            @Override
            public void encode(Serializer<?> serializer, T value) {
                var branchEndec = endec;

                for (var branch : AttributeEndecBuilder.this.branches.entrySet()) {
                    if (serializer.hasAttribute(branch.getKey())) {
                        branchEndec = branch.getValue();
                        break;
                    }
                }

                branchEndec.encode(serializer, value);
            }

            @Override
            public T decode(Deserializer<?> deserializer) {
                var branchEndec = endec;

                for (var branch : AttributeEndecBuilder.this.branches.entrySet()) {
                    if (deserializer.hasAttribute(branch.getKey())) {
                        branchEndec = branch.getValue();
                        break;
                    }
                }

                return branchEndec.decode(deserializer);
            }
        };
    }
}
