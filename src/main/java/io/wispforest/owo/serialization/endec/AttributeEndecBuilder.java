package io.wispforest.owo.serialization.endec;

import io.wispforest.owo.serialization.*;

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
            public void encode(SerializationContext ctx, Serializer<?> serializer, T value) {
                var branchEndec = endec;

                for (var branch : AttributeEndecBuilder.this.branches.entrySet()) {
                    if (ctx.hasAttribute(branch.getKey())) {
                        branchEndec = branch.getValue();
                        break;
                    }
                }

                branchEndec.encode(ctx, serializer, value);
            }

            @Override
            public T decode(SerializationContext ctx, Deserializer<?> deserializer) {
                var branchEndec = endec;

                for (var branch : AttributeEndecBuilder.this.branches.entrySet()) {
                    if (ctx.hasAttribute(branch.getKey())) {
                        branchEndec = branch.getValue();
                        break;
                    }
                }

                return branchEndec.decode(ctx, deserializer);
            }
        };
    }
}
