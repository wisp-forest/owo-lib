package io.wispforest.owo.serialization.impl;

import com.mojang.datafixers.util.*;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.StructDeserializer;
import io.wispforest.owo.serialization.StructSerializer;

import java.util.function.BiFunction;
import java.util.function.Function;

public class StructEndecBuilder<T> {

    public static <S, F1> Endec<S> of(StructField<S, F1> f1, Function<F1, S> constructor){
        return new StructEndec<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.endec, f1.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(f1.deserialize(struct));
            }
        };
    }

    public static <S, F1, F2> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, BiFunction<F1, F2, S> constructor){
        return new StructEndec<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.endec, f1.getter.apply(value))
                        .field(f2.name, f2.endec, f2.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(f1.deserialize(struct),
                        f2.deserialize(struct));
            }
        };
    }

    public static <S, F1, F2, F3> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, Function3<F1, F2, F3, S> constructor){
        return new StructEndec<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.endec, f1.getter.apply(value))
                        .field(f2.name, f2.endec, f2.getter.apply(value))
                        .field(f3.name, f3.endec, f3.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(f1.deserialize(struct),
                        f2.deserialize(struct),
                        f3.deserialize(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, Function4<F1, F2, F3, F4, S> constructor){
        return new StructEndec<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.endec, f1.getter.apply(value))
                        .field(f2.name, f2.endec, f2.getter.apply(value))
                        .field(f3.name, f3.endec, f3.getter.apply(value))
                        .field(f4.name, f4.endec, f4.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(f1.deserialize(struct),
                        f2.deserialize(struct),
                        f3.deserialize(struct),
                        f4.deserialize(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, Function5<F1, F2, F3, F4, F5, S> constructor){
        return new StructEndec<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.endec, f1.getter.apply(value))
                        .field(f2.name, f2.endec, f2.getter.apply(value))
                        .field(f3.name, f3.endec, f3.getter.apply(value))
                        .field(f4.name, f4.endec, f4.getter.apply(value))
                        .field(f5.name, f5.endec, f5.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(f1.deserialize(struct),
                        f2.deserialize(struct),
                        f3.deserialize(struct),
                        f4.deserialize(struct),
                        f5.deserialize(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, Function6<F1, F2, F3, F4, F5, F6, S> constructor){
        return new StructEndec<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.endec, f1.getter.apply(value))
                        .field(f2.name, f2.endec, f2.getter.apply(value))
                        .field(f3.name, f3.endec, f3.getter.apply(value))
                        .field(f4.name, f4.endec, f4.getter.apply(value))
                        .field(f5.name, f5.endec, f5.getter.apply(value))
                        .field(f6.name, f6.endec, f6.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(f1.deserialize(struct),
                        f2.deserialize(struct),
                        f3.deserialize(struct),
                        f4.deserialize(struct),
                        f5.deserialize(struct),
                        f6.deserialize(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, Function7<F1, F2, F3, F4, F5, F6, F7, S> constructor){
        return new StructEndec<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.endec, f1.getter.apply(value))
                        .field(f2.name, f2.endec, f2.getter.apply(value))
                        .field(f3.name, f3.endec, f3.getter.apply(value))
                        .field(f4.name, f4.endec, f4.getter.apply(value))
                        .field(f5.name, f5.endec, f5.getter.apply(value))
                        .field(f6.name, f6.endec, f6.getter.apply(value))
                        .field(f7.name, f7.endec, f7.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(f1.deserialize(struct),
                        f2.deserialize(struct),
                        f3.deserialize(struct),
                        f4.deserialize(struct),
                        f5.deserialize(struct),
                        f6.deserialize(struct),
                        f7.deserialize(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, Function8<F1, F2, F3, F4, F5, F6, F7, F8, S> constructor){
        return new StructEndec<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.endec, f1.getter.apply(value))
                        .field(f2.name, f2.endec, f2.getter.apply(value))
                        .field(f3.name, f3.endec, f3.getter.apply(value))
                        .field(f4.name, f4.endec, f4.getter.apply(value))
                        .field(f5.name, f5.endec, f5.getter.apply(value))
                        .field(f6.name, f6.endec, f6.getter.apply(value))
                        .field(f7.name, f7.endec, f7.getter.apply(value))
                        .field(f8.name, f8.endec, f8.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(f1.deserialize(struct),
                        f2.deserialize(struct),
                        f3.deserialize(struct),
                        f4.deserialize(struct),
                        f5.deserialize(struct),
                        f6.deserialize(struct),
                        f7.deserialize(struct),
                        f8.deserialize(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, Function9<F1, F2, F3, F4, F5, F6, F7, F8, F9, S> constructor){
        return new StructEndec<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.endec, f1.getter.apply(value))
                        .field(f2.name, f2.endec, f2.getter.apply(value))
                        .field(f3.name, f3.endec, f3.getter.apply(value))
                        .field(f4.name, f4.endec, f4.getter.apply(value))
                        .field(f5.name, f5.endec, f5.getter.apply(value))
                        .field(f6.name, f6.endec, f6.getter.apply(value))
                        .field(f7.name, f7.endec, f7.getter.apply(value))
                        .field(f8.name, f8.endec, f8.getter.apply(value))
                        .field(f9.name, f9.endec, f9.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(f1.deserialize(struct),
                        f2.deserialize(struct),
                        f3.deserialize(struct),
                        f4.deserialize(struct),
                        f5.deserialize(struct),
                        f6.deserialize(struct),
                        f7.deserialize(struct),
                        f8.deserialize(struct),
                        f9.deserialize(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, Function10<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, S> constructor){
        return new StructEndec<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.endec, f1.getter.apply(value))
                        .field(f2.name, f2.endec, f2.getter.apply(value))
                        .field(f3.name, f3.endec, f3.getter.apply(value))
                        .field(f4.name, f4.endec, f4.getter.apply(value))
                        .field(f5.name, f5.endec, f5.getter.apply(value))
                        .field(f6.name, f6.endec, f6.getter.apply(value))
                        .field(f7.name, f7.endec, f7.getter.apply(value))
                        .field(f8.name, f8.endec, f8.getter.apply(value))
                        .field(f9.name, f9.endec, f9.getter.apply(value))
                        .field(f10.name, f10.endec, f10.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(f1.deserialize(struct),
                        f2.deserialize(struct),
                        f3.deserialize(struct),
                        f4.deserialize(struct),
                        f5.deserialize(struct),
                        f6.deserialize(struct),
                        f7.deserialize(struct),
                        f8.deserialize(struct),
                        f9.deserialize(struct),
                        f10.deserialize(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, Function11<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, S> constructor){
        return new StructEndec<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.endec, f1.getter.apply(value))
                        .field(f2.name, f2.endec, f2.getter.apply(value))
                        .field(f3.name, f3.endec, f3.getter.apply(value))
                        .field(f4.name, f4.endec, f4.getter.apply(value))
                        .field(f5.name, f5.endec, f5.getter.apply(value))
                        .field(f6.name, f6.endec, f6.getter.apply(value))
                        .field(f7.name, f7.endec, f7.getter.apply(value))
                        .field(f8.name, f8.endec, f8.getter.apply(value))
                        .field(f9.name, f9.endec, f9.getter.apply(value))
                        .field(f10.name, f10.endec, f10.getter.apply(value))
                        .field(f11.name, f11.endec, f11.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(f1.deserialize(struct),
                        f2.deserialize(struct),
                        f3.deserialize(struct),
                        f4.deserialize(struct),
                        f5.deserialize(struct),
                        f6.deserialize(struct),
                        f7.deserialize(struct),
                        f8.deserialize(struct),
                        f9.deserialize(struct),
                        f10.deserialize(struct),
                        f11.deserialize(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, StructField<S, F12> f12, Function12<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, S> constructor){
        return new StructEndec<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.endec, f1.getter.apply(value))
                        .field(f2.name, f2.endec, f2.getter.apply(value))
                        .field(f3.name, f3.endec, f3.getter.apply(value))
                        .field(f4.name, f4.endec, f4.getter.apply(value))
                        .field(f5.name, f5.endec, f5.getter.apply(value))
                        .field(f6.name, f6.endec, f6.getter.apply(value))
                        .field(f7.name, f7.endec, f7.getter.apply(value))
                        .field(f8.name, f8.endec, f8.getter.apply(value))
                        .field(f9.name, f9.endec, f9.getter.apply(value))
                        .field(f10.name, f10.endec, f10.getter.apply(value))
                        .field(f11.name, f11.endec, f11.getter.apply(value))
                        .field(f12.name, f12.endec, f12.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(f1.deserialize(struct),
                        f2.deserialize(struct),
                        f3.deserialize(struct),
                        f4.deserialize(struct),
                        f5.deserialize(struct),
                        f6.deserialize(struct),
                        f7.deserialize(struct),
                        f8.deserialize(struct),
                        f9.deserialize(struct),
                        f10.deserialize(struct),
                        f11.deserialize(struct),
                        f12.deserialize(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, StructField<S, F12> f12, StructField<S, F13> f13, Function13<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, S> constructor){
        return new StructEndec<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.endec, f1.getter.apply(value))
                        .field(f2.name, f2.endec, f2.getter.apply(value))
                        .field(f3.name, f3.endec, f3.getter.apply(value))
                        .field(f4.name, f4.endec, f4.getter.apply(value))
                        .field(f5.name, f5.endec, f5.getter.apply(value))
                        .field(f6.name, f6.endec, f6.getter.apply(value))
                        .field(f7.name, f7.endec, f7.getter.apply(value))
                        .field(f8.name, f8.endec, f8.getter.apply(value))
                        .field(f9.name, f9.endec, f9.getter.apply(value))
                        .field(f10.name, f10.endec, f10.getter.apply(value))
                        .field(f11.name, f11.endec, f11.getter.apply(value))
                        .field(f12.name, f12.endec, f12.getter.apply(value))
                        .field(f13.name, f13.endec, f13.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(f1.deserialize(struct),
                        f2.deserialize(struct),
                        f3.deserialize(struct),
                        f4.deserialize(struct),
                        f5.deserialize(struct),
                        f6.deserialize(struct),
                        f7.deserialize(struct),
                        f8.deserialize(struct),
                        f9.deserialize(struct),
                        f10.deserialize(struct),
                        f11.deserialize(struct),
                        f12.deserialize(struct),
                        f13.deserialize(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, StructField<S, F12> f12, StructField<S, F13> f13, StructField<S, F14> f14, Function14<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, S> constructor){
        return new StructEndec<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.endec, f1.getter.apply(value))
                        .field(f2.name, f2.endec, f2.getter.apply(value))
                        .field(f3.name, f3.endec, f3.getter.apply(value))
                        .field(f4.name, f4.endec, f4.getter.apply(value))
                        .field(f5.name, f5.endec, f5.getter.apply(value))
                        .field(f6.name, f6.endec, f6.getter.apply(value))
                        .field(f7.name, f7.endec, f7.getter.apply(value))
                        .field(f8.name, f8.endec, f8.getter.apply(value))
                        .field(f9.name, f9.endec, f9.getter.apply(value))
                        .field(f10.name, f10.endec, f10.getter.apply(value))
                        .field(f11.name, f11.endec, f11.getter.apply(value))
                        .field(f12.name, f12.endec, f12.getter.apply(value))
                        .field(f13.name, f13.endec, f13.getter.apply(value))
                        .field(f14.name, f14.endec, f14.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(f1.deserialize(struct),
                        f2.deserialize(struct),
                        f3.deserialize(struct),
                        f4.deserialize(struct),
                        f5.deserialize(struct),
                        f6.deserialize(struct),
                        f7.deserialize(struct),
                        f8.deserialize(struct),
                        f9.deserialize(struct),
                        f10.deserialize(struct),
                        f11.deserialize(struct),
                        f12.deserialize(struct),
                        f13.deserialize(struct),
                        f14.deserialize(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, StructField<S, F12> f12, StructField<S, F13> f13, StructField<S, F14> f14, StructField<S, F15> f15, Function15<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15, S> constructor){
        return new StructEndec<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.endec, f1.getter.apply(value))
                        .field(f2.name, f2.endec, f2.getter.apply(value))
                        .field(f3.name, f3.endec, f3.getter.apply(value))
                        .field(f4.name, f4.endec, f4.getter.apply(value))
                        .field(f5.name, f5.endec, f5.getter.apply(value))
                        .field(f6.name, f6.endec, f6.getter.apply(value))
                        .field(f7.name, f7.endec, f7.getter.apply(value))
                        .field(f8.name, f8.endec, f8.getter.apply(value))
                        .field(f9.name, f9.endec, f9.getter.apply(value))
                        .field(f10.name, f10.endec, f10.getter.apply(value))
                        .field(f11.name, f11.endec, f11.getter.apply(value))
                        .field(f12.name, f12.endec, f12.getter.apply(value))
                        .field(f13.name, f13.endec, f13.getter.apply(value))
                        .field(f14.name, f14.endec, f14.getter.apply(value))
                        .field(f15.name, f15.endec, f15.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(f1.deserialize(struct),
                        f2.deserialize(struct),
                        f3.deserialize(struct),
                        f4.deserialize(struct),
                        f5.deserialize(struct),
                        f6.deserialize(struct),
                        f7.deserialize(struct),
                        f8.deserialize(struct),
                        f9.deserialize(struct),
                        f10.deserialize(struct),
                        f11.deserialize(struct),
                        f12.deserialize(struct),
                        f13.deserialize(struct),
                        f14.deserialize(struct),
                        f15.deserialize(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15, F16> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, StructField<S, F12> f12, StructField<S, F13> f13, StructField<S, F14> f14, StructField<S, F15> f15, StructField<S, F16> f16, Function16<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15, F16, S> constructor){
        return new StructEndec<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.endec, f1.getter.apply(value))
                        .field(f2.name, f2.endec, f2.getter.apply(value))
                        .field(f3.name, f3.endec, f3.getter.apply(value))
                        .field(f4.name, f4.endec, f4.getter.apply(value))
                        .field(f5.name, f5.endec, f5.getter.apply(value))
                        .field(f6.name, f6.endec, f6.getter.apply(value))
                        .field(f7.name, f7.endec, f7.getter.apply(value))
                        .field(f8.name, f8.endec, f8.getter.apply(value))
                        .field(f9.name, f9.endec, f9.getter.apply(value))
                        .field(f10.name, f10.endec, f10.getter.apply(value))
                        .field(f11.name, f11.endec, f11.getter.apply(value))
                        .field(f12.name, f12.endec, f12.getter.apply(value))
                        .field(f13.name, f13.endec, f13.getter.apply(value))
                        .field(f14.name, f14.endec, f14.getter.apply(value))
                        .field(f15.name, f15.endec, f15.getter.apply(value))
                        .field(f16.name, f16.endec, f16.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(f1.deserialize(struct),
                        f2.deserialize(struct),
                        f3.deserialize(struct),
                        f4.deserialize(struct),
                        f5.deserialize(struct),
                        f6.deserialize(struct),
                        f7.deserialize(struct),
                        f8.deserialize(struct),
                        f9.deserialize(struct),
                        f10.deserialize(struct),
                        f11.deserialize(struct),
                        f12.deserialize(struct),
                        f13.deserialize(struct),
                        f14.deserialize(struct),
                        f15.deserialize(struct),
                        f16.deserialize(struct));
            }
        };
    }

    /*public static void main(String[] args){
        String typesSpot = "{types}";

        String constructorNum = "{cnum}";

        //--

        String typeSpot = "{type}";
        String typeArgumentName = "{arg}";

        String numberSpot = "{num}";

        String structFieldTemplate = "StructField<S, F{num}> f{num}";

        String structFieldArgs = "{fieldArgs}";

        //--

        String structSerCallTemplate = ".field({arg}.name, {arg}.endec, {arg}.getter.apply(value))";

        String structSerCallsSpot = "{serCalls}";

        //--

        //String structDeserCallTemplate = "struct.field({arg}.name, {arg}.endec, {arg}.defaultValue)";
        String structDeserCallTemplate = "{arg}.deserialize(struct)";

        String structDeserCallsSpot = "{deserCalls}";

        String method =
                """
                public static <S, {types}> Endec<S> of({fieldArgs}, Function{cnum}<{types}, S> constructor){
                    return new StructEndec<S>() {
                        @Override
                        public void encode(StructSerializer struct, S value) {
                            struct{serCalls};
                        }

                        @Override
                        public S decode(StructDeserializer struct) {
                            return constructor.apply({deserCalls});
                        }
                    };
                }

                """;

        Map<Integer, String> structTypes = new LinkedHashMap<>();
        Map<Integer, String> structArgs = new LinkedHashMap<>();

        Map<Integer, String> structFields = new LinkedHashMap<>();
        Map<Integer, String> structSerCalls = new LinkedHashMap<>();
        Map<Integer, String> structDeserCalls = new LinkedHashMap<>();

        String allMethods = "";

        for (int i = 1; i < 17; i++) {
            structTypes.put(i, "F" + i);
            structArgs.put(i, "f" + i);

            structFields.put(i, structFieldTemplate.replace(numberSpot, String.valueOf(i)));
            structSerCalls.put(i, structSerCallTemplate.replace(typeArgumentName, structArgs.get(i)));
            structDeserCalls.put(i, structDeserCallTemplate.replace(typeArgumentName, structArgs.get(i)));

            String types = String.join(", ", structTypes.values());

            String fieldArgs = String.join(", ", structFields.values());

            String serCalls = String.join("\n", structSerCalls.values());
            String deserCalls = String.join(",\n", structDeserCalls.values());

            String newMethod = method
                    .replace(constructorNum, String.valueOf(i))
                    .replace(typesSpot, types)
                    .replace(structFieldArgs, fieldArgs)
                    .replace(structSerCallsSpot, serCalls)
                    .replace(structDeserCallsSpot, deserCalls);

            allMethods = allMethods.concat(newMethod);
        }

        try(FileWriter myWriter = new FileWriter("test.txt")) {
            myWriter.write(allMethods);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }*/
}
