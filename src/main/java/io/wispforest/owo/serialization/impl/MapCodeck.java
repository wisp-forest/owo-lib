package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.serialization.Codeck;
import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Serializer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class MapCodeck<K, V> implements Codeck<Map<K, V>> {

    private Supplier<Map<K, V>> mapConstructor = LinkedHashMap::new;

    private final Codeck<V> codeck;

    public Function<K, String> fromKey;
    public Function<String, K> toKey;

    private MapCodeck(Codeck<V> codeck, Function<K, String> fromKey, Function<String, K> toKey){
        this.codeck = codeck;

        this.fromKey = fromKey;
        this.toKey = toKey;
    }

    public MapCodeck<K, V> mapConstructor(Supplier<Map<K, V>> mapConstructor){
        this.mapConstructor = mapConstructor;

        return this;
    }

    public static <V> MapCodeck<String, V> of(Codeck<V> codeck){
        return new MapCodeck<>(codeck, s -> s, s -> s);
    }

    public <R> MapCodeck<R, V> keyThen(Function<K, R> toFunc, Function<R, K> fromFunc) {
        return new MapCodeck<>(this.codeck, fromFunc.andThen(this.fromKey), this.toKey.andThen(toFunc));
    }

    @Override
    public <E> void encode(Serializer<E> serializer, Map<K, V> value) {
        try (var state = serializer.map(codeck, value.size())) {
            value.forEach((k, v) -> state.entry(fromKey.apply(k), v));
        }
    }

    @Override
    public <E> Map<K, V> decode(Deserializer<E> deserializer) {
        final Map<K, V> map = mapConstructor.get();

        deserializer.map(codeck).forEachRemaining(entry -> map.put(toKey.apply(entry.getKey()), entry.getValue()));

        return map;
    }
}
