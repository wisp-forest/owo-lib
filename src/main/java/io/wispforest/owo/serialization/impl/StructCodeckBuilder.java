package io.wispforest.owo.serialization.impl;

import com.mojang.datafixers.util.*;
import io.wispforest.owo.serialization.Codeck;
import io.wispforest.owo.serialization.StructDeserializer;
import io.wispforest.owo.serialization.StructSerializer;

import java.util.function.BiFunction;
import java.util.function.Function;

public class StructCodeckBuilder<T> {

    public static <S, F1> Codeck<S> of(StructField<S, F1> f1, Function<F1, S> constructor){
        return new StructCodeck<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.codec, f1.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(struct.field(f1.name, f1.codec, f1.defaultValue));
            }
        };
    }

    public static <S, F1, F2> Codeck<S> of(StructField<S, F1> f1, StructField<S, F2> f2, BiFunction<F1, F2, S> constructor){
        return new StructCodeck<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.codec, f1.getter.apply(value))
                        .field(f2.name, f2.codec, f2.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(struct.field(f1.name, f1.codec, f1.defaultValue),
                        struct.field(f2.name, f2.codec, f2.defaultValue));
            }
        };
    }

    public static <S, F1, F2, F3> Codeck<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, Function3<F1, F2, F3, S> constructor){
        return new StructCodeck<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.codec, f1.getter.apply(value))
                        .field(f2.name, f2.codec, f2.getter.apply(value))
                        .field(f3.name, f3.codec, f3.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(struct.field(f1.name, f1.codec, f1.defaultValue),
                        struct.field(f2.name, f2.codec, f2.defaultValue),
                        struct.field(f3.name, f3.codec, f3.defaultValue));
            }
        };
    }

    public static <S, F1, F2, F3, F4> Codeck<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, Function4<F1, F2, F3, F4, S> constructor){
        return new StructCodeck<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.codec, f1.getter.apply(value))
                        .field(f2.name, f2.codec, f2.getter.apply(value))
                        .field(f3.name, f3.codec, f3.getter.apply(value))
                        .field(f4.name, f4.codec, f4.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(struct.field(f1.name, f1.codec, f1.defaultValue),
                        struct.field(f2.name, f2.codec, f2.defaultValue),
                        struct.field(f3.name, f3.codec, f3.defaultValue),
                        struct.field(f4.name, f4.codec, f4.defaultValue));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5> Codeck<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, Function5<F1, F2, F3, F4, F5, S> constructor){
        return new StructCodeck<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.codec, f1.getter.apply(value))
                        .field(f2.name, f2.codec, f2.getter.apply(value))
                        .field(f3.name, f3.codec, f3.getter.apply(value))
                        .field(f4.name, f4.codec, f4.getter.apply(value))
                        .field(f5.name, f5.codec, f5.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(struct.field(f1.name, f1.codec, f1.defaultValue),
                        struct.field(f2.name, f2.codec, f2.defaultValue),
                        struct.field(f3.name, f3.codec, f3.defaultValue),
                        struct.field(f4.name, f4.codec, f4.defaultValue),
                        struct.field(f5.name, f5.codec, f5.defaultValue));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6> Codeck<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, Function6<F1, F2, F3, F4, F5, F6, S> constructor){
        return new StructCodeck<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.codec, f1.getter.apply(value))
                        .field(f2.name, f2.codec, f2.getter.apply(value))
                        .field(f3.name, f3.codec, f3.getter.apply(value))
                        .field(f4.name, f4.codec, f4.getter.apply(value))
                        .field(f5.name, f5.codec, f5.getter.apply(value))
                        .field(f6.name, f6.codec, f6.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(struct.field(f1.name, f1.codec, f1.defaultValue),
                        struct.field(f2.name, f2.codec, f2.defaultValue),
                        struct.field(f3.name, f3.codec, f3.defaultValue),
                        struct.field(f4.name, f4.codec, f4.defaultValue),
                        struct.field(f5.name, f5.codec, f5.defaultValue),
                        struct.field(f6.name, f6.codec, f6.defaultValue));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7> Codeck<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, Function7<F1, F2, F3, F4, F5, F6, F7, S> constructor){
        return new StructCodeck<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.codec, f1.getter.apply(value))
                        .field(f2.name, f2.codec, f2.getter.apply(value))
                        .field(f3.name, f3.codec, f3.getter.apply(value))
                        .field(f4.name, f4.codec, f4.getter.apply(value))
                        .field(f5.name, f5.codec, f5.getter.apply(value))
                        .field(f6.name, f6.codec, f6.getter.apply(value))
                        .field(f7.name, f7.codec, f7.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(struct.field(f1.name, f1.codec, f1.defaultValue),
                        struct.field(f2.name, f2.codec, f2.defaultValue),
                        struct.field(f3.name, f3.codec, f3.defaultValue),
                        struct.field(f4.name, f4.codec, f4.defaultValue),
                        struct.field(f5.name, f5.codec, f5.defaultValue),
                        struct.field(f6.name, f6.codec, f6.defaultValue),
                        struct.field(f7.name, f7.codec, f7.defaultValue));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8> Codeck<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, Function8<F1, F2, F3, F4, F5, F6, F7, F8, S> constructor){
        return new StructCodeck<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.codec, f1.getter.apply(value))
                        .field(f2.name, f2.codec, f2.getter.apply(value))
                        .field(f3.name, f3.codec, f3.getter.apply(value))
                        .field(f4.name, f4.codec, f4.getter.apply(value))
                        .field(f5.name, f5.codec, f5.getter.apply(value))
                        .field(f6.name, f6.codec, f6.getter.apply(value))
                        .field(f7.name, f7.codec, f7.getter.apply(value))
                        .field(f8.name, f8.codec, f8.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(struct.field(f1.name, f1.codec, f1.defaultValue),
                        struct.field(f2.name, f2.codec, f2.defaultValue),
                        struct.field(f3.name, f3.codec, f3.defaultValue),
                        struct.field(f4.name, f4.codec, f4.defaultValue),
                        struct.field(f5.name, f5.codec, f5.defaultValue),
                        struct.field(f6.name, f6.codec, f6.defaultValue),
                        struct.field(f7.name, f7.codec, f7.defaultValue),
                        struct.field(f8.name, f8.codec, f8.defaultValue));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9> Codeck<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, Function9<F1, F2, F3, F4, F5, F6, F7, F8, F9, S> constructor){
        return new StructCodeck<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.codec, f1.getter.apply(value))
                        .field(f2.name, f2.codec, f2.getter.apply(value))
                        .field(f3.name, f3.codec, f3.getter.apply(value))
                        .field(f4.name, f4.codec, f4.getter.apply(value))
                        .field(f5.name, f5.codec, f5.getter.apply(value))
                        .field(f6.name, f6.codec, f6.getter.apply(value))
                        .field(f7.name, f7.codec, f7.getter.apply(value))
                        .field(f8.name, f8.codec, f8.getter.apply(value))
                        .field(f9.name, f9.codec, f9.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(struct.field(f1.name, f1.codec, f1.defaultValue),
                        struct.field(f2.name, f2.codec, f2.defaultValue),
                        struct.field(f3.name, f3.codec, f3.defaultValue),
                        struct.field(f4.name, f4.codec, f4.defaultValue),
                        struct.field(f5.name, f5.codec, f5.defaultValue),
                        struct.field(f6.name, f6.codec, f6.defaultValue),
                        struct.field(f7.name, f7.codec, f7.defaultValue),
                        struct.field(f8.name, f8.codec, f8.defaultValue),
                        struct.field(f9.name, f9.codec, f9.defaultValue));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10> Codeck<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, Function10<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, S> constructor){
        return new StructCodeck<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.codec, f1.getter.apply(value))
                        .field(f2.name, f2.codec, f2.getter.apply(value))
                        .field(f3.name, f3.codec, f3.getter.apply(value))
                        .field(f4.name, f4.codec, f4.getter.apply(value))
                        .field(f5.name, f5.codec, f5.getter.apply(value))
                        .field(f6.name, f6.codec, f6.getter.apply(value))
                        .field(f7.name, f7.codec, f7.getter.apply(value))
                        .field(f8.name, f8.codec, f8.getter.apply(value))
                        .field(f9.name, f9.codec, f9.getter.apply(value))
                        .field(f10.name, f10.codec, f10.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(struct.field(f1.name, f1.codec, f1.defaultValue),
                        struct.field(f2.name, f2.codec, f2.defaultValue),
                        struct.field(f3.name, f3.codec, f3.defaultValue),
                        struct.field(f4.name, f4.codec, f4.defaultValue),
                        struct.field(f5.name, f5.codec, f5.defaultValue),
                        struct.field(f6.name, f6.codec, f6.defaultValue),
                        struct.field(f7.name, f7.codec, f7.defaultValue),
                        struct.field(f8.name, f8.codec, f8.defaultValue),
                        struct.field(f9.name, f9.codec, f9.defaultValue),
                        struct.field(f10.name, f10.codec, f10.defaultValue));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11> Codeck<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, Function11<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, S> constructor){
        return new StructCodeck<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.codec, f1.getter.apply(value))
                        .field(f2.name, f2.codec, f2.getter.apply(value))
                        .field(f3.name, f3.codec, f3.getter.apply(value))
                        .field(f4.name, f4.codec, f4.getter.apply(value))
                        .field(f5.name, f5.codec, f5.getter.apply(value))
                        .field(f6.name, f6.codec, f6.getter.apply(value))
                        .field(f7.name, f7.codec, f7.getter.apply(value))
                        .field(f8.name, f8.codec, f8.getter.apply(value))
                        .field(f9.name, f9.codec, f9.getter.apply(value))
                        .field(f10.name, f10.codec, f10.getter.apply(value))
                        .field(f11.name, f11.codec, f11.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(struct.field(f1.name, f1.codec, f1.defaultValue),
                        struct.field(f2.name, f2.codec, f2.defaultValue),
                        struct.field(f3.name, f3.codec, f3.defaultValue),
                        struct.field(f4.name, f4.codec, f4.defaultValue),
                        struct.field(f5.name, f5.codec, f5.defaultValue),
                        struct.field(f6.name, f6.codec, f6.defaultValue),
                        struct.field(f7.name, f7.codec, f7.defaultValue),
                        struct.field(f8.name, f8.codec, f8.defaultValue),
                        struct.field(f9.name, f9.codec, f9.defaultValue),
                        struct.field(f10.name, f10.codec, f10.defaultValue),
                        struct.field(f11.name, f11.codec, f11.defaultValue));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12> Codeck<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, StructField<S, F12> f12, Function12<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, S> constructor){
        return new StructCodeck<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.codec, f1.getter.apply(value))
                        .field(f2.name, f2.codec, f2.getter.apply(value))
                        .field(f3.name, f3.codec, f3.getter.apply(value))
                        .field(f4.name, f4.codec, f4.getter.apply(value))
                        .field(f5.name, f5.codec, f5.getter.apply(value))
                        .field(f6.name, f6.codec, f6.getter.apply(value))
                        .field(f7.name, f7.codec, f7.getter.apply(value))
                        .field(f8.name, f8.codec, f8.getter.apply(value))
                        .field(f9.name, f9.codec, f9.getter.apply(value))
                        .field(f10.name, f10.codec, f10.getter.apply(value))
                        .field(f11.name, f11.codec, f11.getter.apply(value))
                        .field(f12.name, f12.codec, f12.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(struct.field(f1.name, f1.codec, f1.defaultValue),
                        struct.field(f2.name, f2.codec, f2.defaultValue),
                        struct.field(f3.name, f3.codec, f3.defaultValue),
                        struct.field(f4.name, f4.codec, f4.defaultValue),
                        struct.field(f5.name, f5.codec, f5.defaultValue),
                        struct.field(f6.name, f6.codec, f6.defaultValue),
                        struct.field(f7.name, f7.codec, f7.defaultValue),
                        struct.field(f8.name, f8.codec, f8.defaultValue),
                        struct.field(f9.name, f9.codec, f9.defaultValue),
                        struct.field(f10.name, f10.codec, f10.defaultValue),
                        struct.field(f11.name, f11.codec, f11.defaultValue),
                        struct.field(f12.name, f12.codec, f12.defaultValue));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13> Codeck<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, StructField<S, F12> f12, StructField<S, F13> f13, Function13<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, S> constructor){
        return new StructCodeck<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.codec, f1.getter.apply(value))
                        .field(f2.name, f2.codec, f2.getter.apply(value))
                        .field(f3.name, f3.codec, f3.getter.apply(value))
                        .field(f4.name, f4.codec, f4.getter.apply(value))
                        .field(f5.name, f5.codec, f5.getter.apply(value))
                        .field(f6.name, f6.codec, f6.getter.apply(value))
                        .field(f7.name, f7.codec, f7.getter.apply(value))
                        .field(f8.name, f8.codec, f8.getter.apply(value))
                        .field(f9.name, f9.codec, f9.getter.apply(value))
                        .field(f10.name, f10.codec, f10.getter.apply(value))
                        .field(f11.name, f11.codec, f11.getter.apply(value))
                        .field(f12.name, f12.codec, f12.getter.apply(value))
                        .field(f13.name, f13.codec, f13.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(struct.field(f1.name, f1.codec, f1.defaultValue),
                        struct.field(f2.name, f2.codec, f2.defaultValue),
                        struct.field(f3.name, f3.codec, f3.defaultValue),
                        struct.field(f4.name, f4.codec, f4.defaultValue),
                        struct.field(f5.name, f5.codec, f5.defaultValue),
                        struct.field(f6.name, f6.codec, f6.defaultValue),
                        struct.field(f7.name, f7.codec, f7.defaultValue),
                        struct.field(f8.name, f8.codec, f8.defaultValue),
                        struct.field(f9.name, f9.codec, f9.defaultValue),
                        struct.field(f10.name, f10.codec, f10.defaultValue),
                        struct.field(f11.name, f11.codec, f11.defaultValue),
                        struct.field(f12.name, f12.codec, f12.defaultValue),
                        struct.field(f13.name, f13.codec, f13.defaultValue));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14> Codeck<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, StructField<S, F12> f12, StructField<S, F13> f13, StructField<S, F14> f14, Function14<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, S> constructor){
        return new StructCodeck<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.codec, f1.getter.apply(value))
                        .field(f2.name, f2.codec, f2.getter.apply(value))
                        .field(f3.name, f3.codec, f3.getter.apply(value))
                        .field(f4.name, f4.codec, f4.getter.apply(value))
                        .field(f5.name, f5.codec, f5.getter.apply(value))
                        .field(f6.name, f6.codec, f6.getter.apply(value))
                        .field(f7.name, f7.codec, f7.getter.apply(value))
                        .field(f8.name, f8.codec, f8.getter.apply(value))
                        .field(f9.name, f9.codec, f9.getter.apply(value))
                        .field(f10.name, f10.codec, f10.getter.apply(value))
                        .field(f11.name, f11.codec, f11.getter.apply(value))
                        .field(f12.name, f12.codec, f12.getter.apply(value))
                        .field(f13.name, f13.codec, f13.getter.apply(value))
                        .field(f14.name, f14.codec, f14.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(struct.field(f1.name, f1.codec, f1.defaultValue),
                        struct.field(f2.name, f2.codec, f2.defaultValue),
                        struct.field(f3.name, f3.codec, f3.defaultValue),
                        struct.field(f4.name, f4.codec, f4.defaultValue),
                        struct.field(f5.name, f5.codec, f5.defaultValue),
                        struct.field(f6.name, f6.codec, f6.defaultValue),
                        struct.field(f7.name, f7.codec, f7.defaultValue),
                        struct.field(f8.name, f8.codec, f8.defaultValue),
                        struct.field(f9.name, f9.codec, f9.defaultValue),
                        struct.field(f10.name, f10.codec, f10.defaultValue),
                        struct.field(f11.name, f11.codec, f11.defaultValue),
                        struct.field(f12.name, f12.codec, f12.defaultValue),
                        struct.field(f13.name, f13.codec, f13.defaultValue),
                        struct.field(f14.name, f14.codec, f14.defaultValue));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15> Codeck<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, StructField<S, F12> f12, StructField<S, F13> f13, StructField<S, F14> f14, StructField<S, F15> f15, Function15<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15, S> constructor){
        return new StructCodeck<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.codec, f1.getter.apply(value))
                        .field(f2.name, f2.codec, f2.getter.apply(value))
                        .field(f3.name, f3.codec, f3.getter.apply(value))
                        .field(f4.name, f4.codec, f4.getter.apply(value))
                        .field(f5.name, f5.codec, f5.getter.apply(value))
                        .field(f6.name, f6.codec, f6.getter.apply(value))
                        .field(f7.name, f7.codec, f7.getter.apply(value))
                        .field(f8.name, f8.codec, f8.getter.apply(value))
                        .field(f9.name, f9.codec, f9.getter.apply(value))
                        .field(f10.name, f10.codec, f10.getter.apply(value))
                        .field(f11.name, f11.codec, f11.getter.apply(value))
                        .field(f12.name, f12.codec, f12.getter.apply(value))
                        .field(f13.name, f13.codec, f13.getter.apply(value))
                        .field(f14.name, f14.codec, f14.getter.apply(value))
                        .field(f15.name, f15.codec, f15.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(struct.field(f1.name, f1.codec, f1.defaultValue),
                        struct.field(f2.name, f2.codec, f2.defaultValue),
                        struct.field(f3.name, f3.codec, f3.defaultValue),
                        struct.field(f4.name, f4.codec, f4.defaultValue),
                        struct.field(f5.name, f5.codec, f5.defaultValue),
                        struct.field(f6.name, f6.codec, f6.defaultValue),
                        struct.field(f7.name, f7.codec, f7.defaultValue),
                        struct.field(f8.name, f8.codec, f8.defaultValue),
                        struct.field(f9.name, f9.codec, f9.defaultValue),
                        struct.field(f10.name, f10.codec, f10.defaultValue),
                        struct.field(f11.name, f11.codec, f11.defaultValue),
                        struct.field(f12.name, f12.codec, f12.defaultValue),
                        struct.field(f13.name, f13.codec, f13.defaultValue),
                        struct.field(f14.name, f14.codec, f14.defaultValue),
                        struct.field(f15.name, f15.codec, f15.defaultValue));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15, F16> Codeck<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, StructField<S, F12> f12, StructField<S, F13> f13, StructField<S, F14> f14, StructField<S, F15> f15, StructField<S, F16> f16, Function16<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15, F16, S> constructor){
        return new StructCodeck<S>() {
            @Override
            public void encode(StructSerializer struct, S value) {
                struct.field(f1.name, f1.codec, f1.getter.apply(value))
                        .field(f2.name, f2.codec, f2.getter.apply(value))
                        .field(f3.name, f3.codec, f3.getter.apply(value))
                        .field(f4.name, f4.codec, f4.getter.apply(value))
                        .field(f5.name, f5.codec, f5.getter.apply(value))
                        .field(f6.name, f6.codec, f6.getter.apply(value))
                        .field(f7.name, f7.codec, f7.getter.apply(value))
                        .field(f8.name, f8.codec, f8.getter.apply(value))
                        .field(f9.name, f9.codec, f9.getter.apply(value))
                        .field(f10.name, f10.codec, f10.getter.apply(value))
                        .field(f11.name, f11.codec, f11.getter.apply(value))
                        .field(f12.name, f12.codec, f12.getter.apply(value))
                        .field(f13.name, f13.codec, f13.getter.apply(value))
                        .field(f14.name, f14.codec, f14.getter.apply(value))
                        .field(f15.name, f15.codec, f15.getter.apply(value))
                        .field(f16.name, f16.codec, f16.getter.apply(value));
            }

            @Override
            public S decode(StructDeserializer struct) {
                return constructor.apply(struct.field(f1.name, f1.codec, f1.defaultValue),
                        struct.field(f2.name, f2.codec, f2.defaultValue),
                        struct.field(f3.name, f3.codec, f3.defaultValue),
                        struct.field(f4.name, f4.codec, f4.defaultValue),
                        struct.field(f5.name, f5.codec, f5.defaultValue),
                        struct.field(f6.name, f6.codec, f6.defaultValue),
                        struct.field(f7.name, f7.codec, f7.defaultValue),
                        struct.field(f8.name, f8.codec, f8.defaultValue),
                        struct.field(f9.name, f9.codec, f9.defaultValue),
                        struct.field(f10.name, f10.codec, f10.defaultValue),
                        struct.field(f11.name, f11.codec, f11.defaultValue),
                        struct.field(f12.name, f12.codec, f12.defaultValue),
                        struct.field(f13.name, f13.codec, f13.defaultValue),
                        struct.field(f14.name, f14.codec, f14.defaultValue),
                        struct.field(f15.name, f15.codec, f15.defaultValue),
                        struct.field(f16.name, f16.codec, f16.defaultValue));
            }
        };
    }





//    public static void main(String[] args){
//        String typesSpot = "{types}";
//
//        //--
//
//        String typeSpot = "{type}";
//        String typeArgumentName = "{arg}";
//
//        String numberSpot = "{num}";
//
//        String structFieldTemplate = "StructField<S, F{num}> f{num}";
//
//        String structFieldArgs = "{fieldArgs}";
//
//        //--
//
//        String structSerCallTemplate = ".field({arg}.name, {arg}.codec, {arg}.getter.apply(value))";
//
//        String structSerCallsSpot = "{serCalls}";
//
//        //--
//
//        String structDeserCallTemplate =
//        """
//                {arg}.handle(struct.field({arg}.name, {arg}.codec))
//        """;
//
//        String structDeserCallsSpot = "{deserCalls}";
//
//        String method =
//        """
//        public static <S, {types}> Codeck<S> of({fieldArgs} Function<{types}, S> constructor){
//            return new Codeck<S>() {
//                @Override
//                public <E> void encode(Serializer<E> serializer, S value) {
//                    try(StructSerializer struct = serializer.struct()){
//                        struct{serCalls};
//                    }
//                }
//
//                @Override
//                public <E> S decode(Deserializer<E> deserializer) {
//                    var struct = deserializer.struct();
//
//                    return constructor.apply({deserCalls});
//                }
//            };
//        }
//
//        """;
//
//        Map<Integer, String> structTypes = new LinkedHashMap<>();
//        Map<Integer, String> structArgs = new LinkedHashMap<>();
//
//        Map<Integer, String> structFields = new LinkedHashMap<>();
//        Map<Integer, String> structSerCalls = new LinkedHashMap<>();
//        Map<Integer, String> structDeserCalls = new LinkedHashMap<>();
//
//        String allMethods = "";
//
//        for (int i = 1; i < 17; i++) {
//            structTypes.put(i, "F" + i);
//            structArgs.put(i, "f" + i);
//
//            structFields.put(i, structFieldTemplate.replace(numberSpot, String.valueOf(i)));
//            structSerCalls.put(i, structSerCallTemplate.replace(typeArgumentName, structArgs.get(i)));
//            structDeserCalls.put(i, structDeserCallTemplate.replace(typeArgumentName, structArgs.get(i)));
//
//            String types = String.join(", ", structTypes.values());
//
//            String fieldArgs = String.join(", ", structFields.values());
//
//            String serCalls = String.join("\n", structSerCalls.values());
//            String deserCalls = String.join(",\n", structDeserCalls.values());
//
//            String newMethod = method
//                    .replace(typesSpot, types)
//                    .replace(structFieldArgs, fieldArgs)
//                    .replace(structSerCallsSpot, serCalls)
//                    .replace(structDeserCallsSpot, deserCalls);
//
//            allMethods = allMethods.concat(newMethod);
//        }
//
//        try(FileWriter myWriter = new FileWriter("test.txt")) {
//            myWriter.write(allMethods);
//        } catch (IOException e){
//            throw new RuntimeException(e);
//        }
//    }
}
