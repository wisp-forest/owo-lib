package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.serialization.Codeck;
import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Serializer;

import java.util.*;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class ListCodeck<V> implements Codeck<List<V>> {

    private IntFunction<List<V>> listConstructor = ArrayList::new;

    private final Codeck<V> codeck;

    public ListCodeck(Codeck<V> codeck){
        this.codeck = codeck;
    }

    public ListCodeck<V> listConstructor(IntFunction<List<V>> listConstructor){
        this.listConstructor = listConstructor;

        return this;
    }

    @Override
    public <E> void encode(Serializer<E> serializer, List<V> value) {
        try (var state = serializer.sequence(codeck, value.size())) {
            value.forEach(state::element);
        }
    }

    @Override
    public <E> List<V> decode(Deserializer<E> deserializer) {
        var sequenceDeserializer = deserializer.sequence(codeck);

        final List<V> list = listConstructor.apply(sequenceDeserializer.size());

        sequenceDeserializer.forEachRemaining(list::add);

        return list;
    }
}
