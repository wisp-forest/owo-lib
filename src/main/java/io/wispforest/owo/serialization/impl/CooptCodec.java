package io.wispforest.owo.serialization.impl;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Serializer;
import io.wispforest.owo.serialization.impl.json.JsonDeserializer;
import io.wispforest.owo.serialization.impl.json.JsonSerializer;
import io.wispforest.owo.serialization.impl.nbt.NbtDeserializer;
import io.wispforest.owo.serialization.impl.nbt.NbtSerializer;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.dynamic.ForwardingDynamicOps;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class CooptCodec<T> implements Endec<T>, Codec<T> {

    private static final Map<DynamicOps<?>, Pair<Supplier<Serializer<Object>>, Function<Object, Deserializer<Object>>>> MAP = new HashMap<>();

    static {
        register(JsonOps.INSTANCE, JsonSerializer::of, JsonDeserializer::of);
        register(NbtOps.INSTANCE, NbtSerializer::of, NbtDeserializer::of);
    }

    private final Endec<T> endec;

    public CooptCodec(Endec<T> endec){
        this.endec = endec;
    }

    private static <V> void register(DynamicOps<?> ops, Supplier<Serializer<V>> supplier, Function<V, Deserializer<V>> function){
        MAP.put(ops, new Pair<>(() -> (Serializer<Object>) supplier.get(), o -> (Deserializer<Object>) function.apply((V) o)));
    }

    @Nullable
    private static <T1> Pair<Supplier<Serializer<Object>>, Function<Object, Deserializer<Object>>> convertToFormat(DynamicOps<T1> ops){
        if(ops instanceof ForwardingDynamicOps<T1> forwardDynOps) ops = forwardDynOps.delegate;

        return MAP.containsKey(ops) ? MAP.get(ops) : new Pair<>(null, null);
    }

    @Override
    public <T1> DataResult<Pair<T, T1>> decode(DynamicOps<T1> ops, T1 input) {
        var deserializer = (Deserializer<T1>) convertToFormat(ops).getSecond().apply(input);

        boolean error = true;
        Exception exception = null;

        T value = null;

        if(deserializer != null) {
            try {
                value = decode(deserializer);

                error = false;
            } catch (Exception e) {
                exception = e;
            }
        }

        return error
                ? DataResult.error(exception != null ? exception::getMessage : () -> "The corresponding Format for the given DynamicOps could not be located!")
                : DataResult.success(new Pair<>(value, input));
    }

    @Override
    public <T1> DataResult<T1> encode(T input, DynamicOps<T1> ops, T1 prefix) {
        var serializer = (Serializer<T1>) convertToFormat(ops).getFirst().get();

        boolean error = true;
        Exception exception = null;

        T1 value = null;

        if(serializer != null) {
            try {
                encode(serializer, input);

                value = serializer.result();

                error = false;
            } catch (Exception e) {
                exception = e;
            }
        }

        return error
                ? DataResult.error(exception != null ? exception::getMessage : () -> "The corresponding Format for the given DynamicOps could not be located!")
                : DataResult.success(value);
    }

    @Override
    public <E> void encode(Serializer<E> serializer, T value) {
        endec.encode(serializer, value);
    }

    @Override
    public <E> T decode(Deserializer<E> deserializer) {
        return endec.decode(deserializer);
    }
}
