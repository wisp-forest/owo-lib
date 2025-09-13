package io.wispforest.owo.serialization;

import com.google.common.reflect.Reflection;
import io.wispforest.endec.*;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.sql.Ref;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class DispatchedEndec<T> implements StructEndec<T> {
    public static final Identifier EMPTY_ID = Identifier.of("owo", "empty");

    private final Map<Identifier, StructEndec<? extends T>> typeToEndec = new HashMap<>();
    private final StructEndec<T> endec;

    private final List<Class<? extends T>> classesToLoad = new ArrayList<>();

    private DispatchedEndec(Function<T, Identifier> instanceToVariant, Endec<Identifier> endec, @Nullable Supplier<T> emptyValue) {
        this.endec = Endec.dispatchedStruct(this::getEndec, instanceToVariant, endec);

        if (emptyValue != null) {
            registerEndec(EMPTY_ID, Endec.unit(emptyValue));
        }
    }

    public static <T extends IdentifiedData> DispatchedEndec<T> of(Endec<Identifier> endec, @Nullable Supplier<T> emptyValue) {
        return of(IdentifiedData::getTypeId, endec, emptyValue);
    }

    public static <T extends IdentifiedData> DispatchedEndec<T> of(Endec<Identifier> endec) {
        return of(IdentifiedData::getTypeId, endec, null);
    }

    public static <T extends IdentifiedData> DispatchedEndec<T> of(@Nullable Supplier<T> emptyValue) {
        return of(IdentifiedData::getTypeId, MinecraftEndecs.IDENTIFIER, emptyValue);
    }

    public static <T extends IdentifiedData> DispatchedEndec<T> of() {
        return of(IdentifiedData::getTypeId, MinecraftEndecs.IDENTIFIER, null);
    }

    public static <T> DispatchedEndec<T> of(Function<T, Identifier> instanceToVariant, Endec<Identifier> endec, @Nullable Supplier<T> emptyValue) {
        return new DispatchedEndec<>(instanceToVariant, endec, emptyValue);
    }

    public static <T> DispatchedEndec<T> of(Function<T, Identifier> instanceToVariant, Endec<Identifier> endec) {
        return of(instanceToVariant, endec, null);
    }

    public static <T> DispatchedEndec<T> of(Function<T, Identifier> instanceToVariant, @Nullable Supplier<T> emptyValue) {
        return of(instanceToVariant, MinecraftEndecs.IDENTIFIER, emptyValue);
    }

    public static <T> DispatchedEndec<T> of(Function<T, Identifier> instanceToVariant) {
        return of(instanceToVariant, MinecraftEndecs.IDENTIFIER, null);
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

    @SafeVarargs
    public final DispatchedEndec<T> loadClasses(Class<? extends T>... classes) {
        this.classesToLoad.addAll(List.of(classes));

        return this;
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
