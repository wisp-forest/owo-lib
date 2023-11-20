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
                .xmap(map -> (L) map, map -> map);
    }

    @Override
    public void encode(Serializer<?> serializer, List<V> value) {
        try (var state = serializer.sequence(endec, value.size())) {
            value.forEach(state::element);
        }
    }

    @Override
    public List<V> decode(Deserializer<?> deserializer) {
        var sequenceDeserializer = deserializer.sequence(endec);

        final List<V> list = listConstructor.apply(sequenceDeserializer.estimatedSize());

        sequenceDeserializer.forEachRemaining(list::add);

        return list;
    }
}
