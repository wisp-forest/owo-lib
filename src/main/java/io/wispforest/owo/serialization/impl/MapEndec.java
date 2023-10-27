package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Serializer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;

public class MapEndec<K, V> implements Endec<Map<K, V>> {

    private IntFunction<Map<K, V>> mapConstructor = LinkedHashMap::new;

    private final Endec<V> endec;

    public Function<K, String> fromKey;
    public Function<String, K> toKey;

    private MapEndec(Endec<V> endec, Function<K, String> fromKey, Function<String, K> toKey){
        this.endec = endec;

        this.fromKey = fromKey;
        this.toKey = toKey;
    }

    public MapEndec<K, V> mapConstructor(IntFunction<Map<K, V>> mapConstructor){
        this.mapConstructor = mapConstructor;

        return this;
    }

    public static <V> MapEndec<String, V> of(Endec<V> endec){
        return new MapEndec<>(endec, s -> s, s -> s);
    }

    public <R> MapEndec<R, V> keyThen(Function<K, R> toFunc, Function<R, K> fromFunc) {
        return new MapEndec<>(this.endec, fromFunc.andThen(this.fromKey), this.toKey.andThen(toFunc));
    }

    public MapEndec<K, V> keyValidator(Function<K, K> validator) {
        return new MapEndec<>(this.endec, this.fromKey, this.toKey.andThen(validator));
    }

    public <M extends Map<K, V>> Endec<M> conform(IntFunction<M> mapConstructor){
        return this.mapConstructor(mapConstructor::apply)
                .then(map -> (M) map, map -> map);
    }

    @Override
    public <E> void encode(Serializer<E> serializer, Map<K, V> value) {
        try (var state = serializer.map(endec, value.size())) {
            value.forEach((k, v) -> state.entry(fromKey.apply(k), v));
        }
    }

    @Override
    public <E> Map<K, V> decode(Deserializer<E> deserializer) {
        var mapDeserializer = deserializer.map(endec);

        final Map<K, V> map = mapConstructor.apply(mapDeserializer.size());

        mapDeserializer.forEachRemaining(entry -> map.put(toKey.apply(entry.getKey()), entry.getValue()));

        return map;
    }
}
