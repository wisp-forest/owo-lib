package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.serialization.Codeck;
import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Serializer;

import java.util.LinkedHashMap;
import java.util.Map;

public class AttributeCodeckBuilder<T> {

    private final Map<SerializationAttribute, Codeck<T>> branches = new LinkedHashMap<>();

    public AttributeCodeckBuilder(Codeck<T> codeck, SerializationAttribute attr){
        this.branches.put(attr, codeck);
    }

    public AttributeCodeckBuilder<T> orElseIf(Codeck<T> codeck, SerializationAttribute attr){
        if(this.branches.containsKey(attr)) throw new IllegalStateException("Already Registered Codeck with the given Attribute! [" + attr.name() + "]");
        this.branches.put(attr, codeck);

        return this;
    }

    public Codeck<T> orElse(Codeck<T> codeck){
        return new Codeck<>() {
            @Override
            public <E> void encode(Serializer<E> serializer, T value) {
                var branchCodeck = codeck;

                for (var branch : branches.entrySet()) {
                    if(serializer.attributes().contains(branch.getKey())) {
                        branchCodeck = branch.getValue();
                        break;
                    }
                }

                branchCodeck.encode(serializer, value);
            }

            @Override
            public <E> T decode(Deserializer<E> deserializer) {
                var branchCodeck = codeck;

                for (var branch : branches.entrySet()) {
                    if(deserializer.attributes().contains(branch.getKey())) {
                        branchCodeck = branch.getValue();
                        break;
                    }
                }

                return branchCodeck.decode(deserializer);
            }
        };
    }
}
