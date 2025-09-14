package io.wispforest.owo.serialization;

import com.google.common.reflect.Reflection;
import io.wispforest.endec.*;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class DispatchedEndec<T> implements StructEndec<T> {
    public static final Identifier EMPTY_ID = Identifier.of("owo", "empty");

    private final Map<Identifier, StructEndec<? extends T>> typeToEndec = new LinkedHashMap<>();
    private final StructEndec<T> endec;

    private final List<Class<? extends T>> classesToLoad;

    private DispatchedEndec(Function<T, Identifier> instanceToVariant, Endec<Identifier> endec, @Nullable Supplier<T> emptyValue, boolean allowTypelessData, List<Class<? extends T>> classesToLoad) {
        var dispatchedEndec = Endec.dispatchedStruct(this::getEndec, instanceToVariant, endec);

        if (allowTypelessData) {
            this.endec = StructEndec.of((ctx, serializer, struct, value) -> {
                if (serializer instanceof SelfDescribedSerializer<?>) {
                    var valueEndec = getEndec(instanceToVariant.apply(value));

                    valueEndec.encodeStruct(ctx, serializer, struct, value);
                } else {
                    dispatchedEndec.encodeStruct(ctx, serializer, struct, value);
                }
            }, (ctx, deserializer, struct) -> {
                if (deserializer instanceof SelfDescribedDeserializer<?>) {
                    var exceptions = new ArrayList<Exception>();

                    for (var entry : typeToEndec.entrySet()) {
                        var valueEndec = entry.getValue();

                        if (entry.getKey() == EMPTY_ID) continue;

                        try {
                            return deserializer.tryRead(deserializer1 -> valueEndec.decode(ctx, deserializer1));
                        } catch (Exception e) {
                            exceptions.add(e);
                        }
                    }

                    if (emptyValue != null) return emptyValue.get();

                    var e = new IllegalStateException("Unable to handle typeless data as it was not valid for any of the endecs on DispatchedEndec!");

                    exceptions.forEach(e::addSuppressed);

                    throw e;
                } else {
                    return dispatchedEndec.decodeStruct(ctx, deserializer, struct);
                }
            });
        } else {
            this.endec = dispatchedEndec;
        }

        if (emptyValue != null) {
            registerEndec(EMPTY_ID, Endec.unit(emptyValue));
        }

        this.classesToLoad = classesToLoad;
    }

    public static <T> Builder<T> of(Function<T, Identifier> instanceToVariant) {
        return new Builder<>(instanceToVariant);
    }

    public static <T> Builder<T> ofOptionalIdentifiedData() {
        return new Builder<>(IdentifiedData.optionalIDGetter());
    }

    public static <T extends IdentifiedData> Builder<T> of() {
        return new Builder<>(IdentifiedData::getTypeId);
    }

    public static final class Builder<T> {

        private final Function<T, Identifier> instanceToVariant;
        private final List<Class<? extends T>> classesToLoad = new ArrayList<>();

        private Endec<Identifier> idEndec = MinecraftEndecs.IDENTIFIER;
        private @Nullable Supplier<T> emptyValue;
        private boolean allowTypelessData;

        Builder(Function<T, Identifier> instanceToVariant){
            this.instanceToVariant = instanceToVariant;
        }

        public Builder<T> idEndec(Endec<Identifier> endec) {
            this.idEndec = endec;

            return this;
        }

        public Builder<T> emptyValue(Supplier<T> emptyValue) {
            this.emptyValue = emptyValue;

            return this;
        }

        public Builder<T> allowTypelessData() {
            this.allowTypelessData = true;

            return this;
        }

        @SafeVarargs
        public final Builder<T> baseClasses(Class<? extends T>... classes) {
            this.classesToLoad.addAll(List.of(classes));

            return this;
        }

        public DispatchedEndec<T> create() {
            return new DispatchedEndec<>(this.instanceToVariant, this.idEndec, this.emptyValue, this.allowTypelessData, this.classesToLoad);
        }
    }

    public <V extends T> StructEndec<V> registerEndec(Identifier id, StructEndec<V> endec) {
        if (this.typeToEndec.containsKey(id)) throw new IllegalStateException("Unable to register the given endec as the given Identifier has already been used! [Id: " + id + "]");

        this.typeToEndec.put(id, endec);

        return endec;
    }

    public <V extends T> StructEndec<T> getEndec(Identifier id) {
        if (!this.classesToLoad.isEmpty()) {
            Reflection.initialize(this.classesToLoad.toArray(Class[]::new));

            this.classesToLoad.clear();
        }

        var endec = this.typeToEndec.get(id);

        if (endec == null) throw new IllegalStateException("Unable to find any Endec for the given id: " + id);

        return (StructEndec<T>) endec;
    }

    @Override
    public void encodeStruct(SerializationContext ctx, Serializer<?> serializer, Serializer.Struct struct, T value) {
        this.endec.encodeStruct(ctx, serializer, struct, value);
    }

    @Override
    public T decodeStruct(SerializationContext ctx, Deserializer<?> deserializer, Deserializer.Struct struct) {
        return this.endec.decodeStruct(ctx, deserializer, struct);
    }
}
