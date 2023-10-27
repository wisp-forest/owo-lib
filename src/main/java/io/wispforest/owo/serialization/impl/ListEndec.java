package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Serializer;

import java.util.*;
import java.util.function.IntFunction;

public class ListEndec<V> implements Endec<List<V>> {

    private IntFunction<List<V>> listConstructor = ArrayList::new;

    private final Endec<V> endec;

    public ListEndec(Endec<V> endec){
        this.endec = endec;
    }

    public ListEndec<V> listConstructor(IntFunction<List<V>> listConstructor){
        this.listConstructor = listConstructor;

        return this;
    }

    public <L extends List<V>> Endec<L> conform(IntFunction<L> mapConstructor){
        return this.listConstructor(mapConstructor::apply)
                .then(map -> (L) map, map -> map);
    }

    @Override
    public <E> void encode(Serializer<E> serializer, List<V> value) {
        try (var state = serializer.sequence(endec, value.size())) {
            value.forEach(state::element);
        }
    }

    @Override
    public <E> List<V> decode(Deserializer<E> deserializer) {
        var sequenceDeserializer = deserializer.sequence(endec);

        final List<V> list = listConstructor.apply(sequenceDeserializer.size());

        sequenceDeserializer.forEachRemaining(list::add);

        return list;
    }
}
