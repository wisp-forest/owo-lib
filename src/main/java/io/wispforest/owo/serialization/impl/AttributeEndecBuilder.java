package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Serializer;

import java.util.LinkedHashMap;
import java.util.Map;

public class AttributeEndecBuilder<T> {

    private final Map<SerializationAttribute, Endec<T>> branches = new LinkedHashMap<>();

    public AttributeEndecBuilder(Endec<T> endec, SerializationAttribute attr){
        this.branches.put(attr, endec);
    }

    public AttributeEndecBuilder<T> orElseIf(Endec<T> endec, SerializationAttribute attr){
        if(this.branches.containsKey(attr)) throw new IllegalStateException("Already Registered endec with the given Attribute! [" + attr.name() + "]");
        this.branches.put(attr, endec);

        return this;
    }

    public Endec<T> orElse(Endec<T> endec){
        return new Endec<>() {
            @Override
            public <E> void encode(Serializer<E> serializer, T value) {
                var branchEndec = endec;

                for (var branch : branches.entrySet()) {
                    if(serializer.attributes().contains(branch.getKey())) {
                        branchEndec = branch.getValue();
                        break;
                    }
                }

                branchEndec.encode(serializer, value);
            }

            @Override
            public <E> T decode(Deserializer<E> deserializer) {
                var branchEndec = endec;

                for (var branch : branches.entrySet()) {
                    if(deserializer.attributes().contains(branch.getKey())) {
                        branchEndec = branch.getValue();
                        break;
                    }
                }

                return branchEndec.decode(deserializer);
            }
        };
    }
}
