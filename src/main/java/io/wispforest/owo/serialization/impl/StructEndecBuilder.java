package io.wispforest.owo.serialization.impl;

import com.mojang.datafixers.util.*;
import io.wispforest.owo.serialization.Deserializer;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.Serializer;

import java.util.function.BiFunction;
import java.util.function.Function;

public class StructEndecBuilder<T> {

    public static <S, F1> Endec<S> of(StructField<S, F1> f1, Function<F1, S> constructor){
        return new StructEndec<>() {
            @Override
            public void encode(Serializer.Struct struct, S value) {
                f1.serializeFieldInst(struct, value);
            }

            @Override
            public S decode(Deserializer.Struct struct) {
                return constructor.apply(f1.deserializeField(struct));
            }
        };
    }

    public static <S, F1, F2> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, BiFunction<F1, F2, S> constructor){
        return new StructEndec<>() {
            @Override
            public void encode(Serializer.Struct struct, S value) {
                f1.serializeFieldInst(struct, value);
                f2.serializeFieldInst(struct, value);
            }

            @Override
            public S decode(Deserializer.Struct struct) {
                return constructor.apply(f1.deserializeField(struct),
                        f2.deserializeField(struct));
            }
        };
    }

    public static <S, F1, F2, F3> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, Function3<F1, F2, F3, S> constructor){
        return new StructEndec<>() {
            @Override
            public void encode(Serializer.Struct struct, S value) {
                f1.serializeFieldInst(struct, value);
                f2.serializeFieldInst(struct, value);
                f3.serializeFieldInst(struct, value);
            }

            @Override
            public S decode(Deserializer.Struct struct) {
                return constructor.apply(f1.deserializeField(struct),
                        f2.deserializeField(struct),
                        f3.deserializeField(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, Function4<F1, F2, F3, F4, S> constructor){
        return new StructEndec<>() {
            @Override
            public void encode(Serializer.Struct struct, S value) {
                f1.serializeFieldInst(struct, value);
                f2.serializeFieldInst(struct, value);
                f3.serializeFieldInst(struct, value);
                f4.serializeFieldInst(struct, value);
            }

            @Override
            public S decode(Deserializer.Struct struct) {
                return constructor.apply(f1.deserializeField(struct),
                        f2.deserializeField(struct),
                        f3.deserializeField(struct),
                        f4.deserializeField(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, Function5<F1, F2, F3, F4, F5, S> constructor){
        return new StructEndec<>() {
            @Override
            public void encode(Serializer.Struct struct, S value) {
                f1.serializeFieldInst(struct, value);
                f2.serializeFieldInst(struct, value);
                f3.serializeFieldInst(struct, value);
                f4.serializeFieldInst(struct, value);
                f5.serializeFieldInst(struct, value);
            }

            @Override
            public S decode(Deserializer.Struct struct) {
                return constructor.apply(f1.deserializeField(struct),
                        f2.deserializeField(struct),
                        f3.deserializeField(struct),
                        f4.deserializeField(struct),
                        f5.deserializeField(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, Function6<F1, F2, F3, F4, F5, F6, S> constructor){
        return new StructEndec<>() {
            @Override
            public void encode(Serializer.Struct struct, S value) {
                f1.serializeFieldInst(struct, value);
                f2.serializeFieldInst(struct, value);
                f3.serializeFieldInst(struct, value);
                f4.serializeFieldInst(struct, value);
                f5.serializeFieldInst(struct, value);
                f6.serializeFieldInst(struct, value);
            }

            @Override
            public S decode(Deserializer.Struct struct) {
                return constructor.apply(f1.deserializeField(struct),
                        f2.deserializeField(struct),
                        f3.deserializeField(struct),
                        f4.deserializeField(struct),
                        f5.deserializeField(struct),
                        f6.deserializeField(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, Function7<F1, F2, F3, F4, F5, F6, F7, S> constructor){
        return new StructEndec<>() {
            @Override
            public void encode(Serializer.Struct struct, S value) {
                f1.serializeFieldInst(struct, value);
                f2.serializeFieldInst(struct, value);
                f3.serializeFieldInst(struct, value);
                f4.serializeFieldInst(struct, value);
                f5.serializeFieldInst(struct, value);
                f6.serializeFieldInst(struct, value);
                f7.serializeFieldInst(struct, value);
            }

            @Override
            public S decode(Deserializer.Struct struct) {
                return constructor.apply(f1.deserializeField(struct),
                        f2.deserializeField(struct),
                        f3.deserializeField(struct),
                        f4.deserializeField(struct),
                        f5.deserializeField(struct),
                        f6.deserializeField(struct),
                        f7.deserializeField(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, Function8<F1, F2, F3, F4, F5, F6, F7, F8, S> constructor){
        return new StructEndec<>() {
            @Override
            public void encode(Serializer.Struct struct, S value) {
                f1.serializeFieldInst(struct, value);
                f2.serializeFieldInst(struct, value);
                f3.serializeFieldInst(struct, value);
                f4.serializeFieldInst(struct, value);
                f5.serializeFieldInst(struct, value);
                f6.serializeFieldInst(struct, value);
                f7.serializeFieldInst(struct, value);
                f8.serializeFieldInst(struct, value);
            }

            @Override
            public S decode(Deserializer.Struct struct) {
                return constructor.apply(f1.deserializeField(struct),
                        f2.deserializeField(struct),
                        f3.deserializeField(struct),
                        f4.deserializeField(struct),
                        f5.deserializeField(struct),
                        f6.deserializeField(struct),
                        f7.deserializeField(struct),
                        f8.deserializeField(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, Function9<F1, F2, F3, F4, F5, F6, F7, F8, F9, S> constructor){
        return new StructEndec<>() {
            @Override
            public void encode(Serializer.Struct struct, S value) {
                f1.serializeFieldInst(struct, value);
                f2.serializeFieldInst(struct, value);
                f3.serializeFieldInst(struct, value);
                f4.serializeFieldInst(struct, value);
                f5.serializeFieldInst(struct, value);
                f6.serializeFieldInst(struct, value);
                f7.serializeFieldInst(struct, value);
                f8.serializeFieldInst(struct, value);
                f9.serializeFieldInst(struct, value);
            }

            @Override
            public S decode(Deserializer.Struct struct) {
                return constructor.apply(f1.deserializeField(struct),
                        f2.deserializeField(struct),
                        f3.deserializeField(struct),
                        f4.deserializeField(struct),
                        f5.deserializeField(struct),
                        f6.deserializeField(struct),
                        f7.deserializeField(struct),
                        f8.deserializeField(struct),
                        f9.deserializeField(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, Function10<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, S> constructor){
        return new StructEndec<>() {
            @Override
            public void encode(Serializer.Struct struct, S value) {
                f1.serializeFieldInst(struct, value);
                f2.serializeFieldInst(struct, value);
                f3.serializeFieldInst(struct, value);
                f4.serializeFieldInst(struct, value);
                f5.serializeFieldInst(struct, value);
                f6.serializeFieldInst(struct, value);
                f7.serializeFieldInst(struct, value);
                f8.serializeFieldInst(struct, value);
                f9.serializeFieldInst(struct, value);
                f10.serializeFieldInst(struct, value);
            }

            @Override
            public S decode(Deserializer.Struct struct) {
                return constructor.apply(f1.deserializeField(struct),
                        f2.deserializeField(struct),
                        f3.deserializeField(struct),
                        f4.deserializeField(struct),
                        f5.deserializeField(struct),
                        f6.deserializeField(struct),
                        f7.deserializeField(struct),
                        f8.deserializeField(struct),
                        f9.deserializeField(struct),
                        f10.deserializeField(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, Function11<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, S> constructor){
        return new StructEndec<>() {
            @Override
            public void encode(Serializer.Struct struct, S value) {
                f1.serializeFieldInst(struct, value);
                f2.serializeFieldInst(struct, value);
                f3.serializeFieldInst(struct, value);
                f4.serializeFieldInst(struct, value);
                f5.serializeFieldInst(struct, value);
                f6.serializeFieldInst(struct, value);
                f7.serializeFieldInst(struct, value);
                f8.serializeFieldInst(struct, value);
                f9.serializeFieldInst(struct, value);
                f10.serializeFieldInst(struct, value);
                f11.serializeFieldInst(struct, value);
            }

            @Override
            public S decode(Deserializer.Struct struct) {
                return constructor.apply(f1.deserializeField(struct),
                        f2.deserializeField(struct),
                        f3.deserializeField(struct),
                        f4.deserializeField(struct),
                        f5.deserializeField(struct),
                        f6.deserializeField(struct),
                        f7.deserializeField(struct),
                        f8.deserializeField(struct),
                        f9.deserializeField(struct),
                        f10.deserializeField(struct),
                        f11.deserializeField(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, StructField<S, F12> f12, Function12<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, S> constructor){
        return new StructEndec<>() {
            @Override
            public void encode(Serializer.Struct struct, S value) {
                f1.serializeFieldInst(struct, value);
                f2.serializeFieldInst(struct, value);
                f3.serializeFieldInst(struct, value);
                f4.serializeFieldInst(struct, value);
                f5.serializeFieldInst(struct, value);
                f6.serializeFieldInst(struct, value);
                f7.serializeFieldInst(struct, value);
                f8.serializeFieldInst(struct, value);
                f9.serializeFieldInst(struct, value);
                f10.serializeFieldInst(struct, value);
                f11.serializeFieldInst(struct, value);
                f12.serializeFieldInst(struct, value);
            }

            @Override
            public S decode(Deserializer.Struct struct) {
                return constructor.apply(f1.deserializeField(struct),
                        f2.deserializeField(struct),
                        f3.deserializeField(struct),
                        f4.deserializeField(struct),
                        f5.deserializeField(struct),
                        f6.deserializeField(struct),
                        f7.deserializeField(struct),
                        f8.deserializeField(struct),
                        f9.deserializeField(struct),
                        f10.deserializeField(struct),
                        f11.deserializeField(struct),
                        f12.deserializeField(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, StructField<S, F12> f12, StructField<S, F13> f13, Function13<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, S> constructor){
        return new StructEndec<>() {
            @Override
            public void encode(Serializer.Struct struct, S value) {
                f1.serializeFieldInst(struct, value);
                f2.serializeFieldInst(struct, value);
                f3.serializeFieldInst(struct, value);
                f4.serializeFieldInst(struct, value);
                f5.serializeFieldInst(struct, value);
                f6.serializeFieldInst(struct, value);
                f7.serializeFieldInst(struct, value);
                f8.serializeFieldInst(struct, value);
                f9.serializeFieldInst(struct, value);
                f10.serializeFieldInst(struct, value);
                f11.serializeFieldInst(struct, value);
                f12.serializeFieldInst(struct, value);
                f13.serializeFieldInst(struct, value);
            }

            @Override
            public S decode(Deserializer.Struct struct) {
                return constructor.apply(f1.deserializeField(struct),
                        f2.deserializeField(struct),
                        f3.deserializeField(struct),
                        f4.deserializeField(struct),
                        f5.deserializeField(struct),
                        f6.deserializeField(struct),
                        f7.deserializeField(struct),
                        f8.deserializeField(struct),
                        f9.deserializeField(struct),
                        f10.deserializeField(struct),
                        f11.deserializeField(struct),
                        f12.deserializeField(struct),
                        f13.deserializeField(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, StructField<S, F12> f12, StructField<S, F13> f13, StructField<S, F14> f14, Function14<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, S> constructor){
        return new StructEndec<>() {
            @Override
            public void encode(Serializer.Struct struct, S value) {
                f1.serializeFieldInst(struct, value);
                f2.serializeFieldInst(struct, value);
                f3.serializeFieldInst(struct, value);
                f4.serializeFieldInst(struct, value);
                f5.serializeFieldInst(struct, value);
                f6.serializeFieldInst(struct, value);
                f7.serializeFieldInst(struct, value);
                f8.serializeFieldInst(struct, value);
                f9.serializeFieldInst(struct, value);
                f10.serializeFieldInst(struct, value);
                f11.serializeFieldInst(struct, value);
                f12.serializeFieldInst(struct, value);
                f13.serializeFieldInst(struct, value);
                f14.serializeFieldInst(struct, value);
            }

            @Override
            public S decode(Deserializer.Struct struct) {
                return constructor.apply(f1.deserializeField(struct),
                        f2.deserializeField(struct),
                        f3.deserializeField(struct),
                        f4.deserializeField(struct),
                        f5.deserializeField(struct),
                        f6.deserializeField(struct),
                        f7.deserializeField(struct),
                        f8.deserializeField(struct),
                        f9.deserializeField(struct),
                        f10.deserializeField(struct),
                        f11.deserializeField(struct),
                        f12.deserializeField(struct),
                        f13.deserializeField(struct),
                        f14.deserializeField(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, StructField<S, F12> f12, StructField<S, F13> f13, StructField<S, F14> f14, StructField<S, F15> f15, Function15<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15, S> constructor){
        return new StructEndec<>() {
            @Override
            public void encode(Serializer.Struct struct, S value) {
                f1.serializeFieldInst(struct, value);
                f2.serializeFieldInst(struct, value);
                f3.serializeFieldInst(struct, value);
                f4.serializeFieldInst(struct, value);
                f5.serializeFieldInst(struct, value);
                f6.serializeFieldInst(struct, value);
                f7.serializeFieldInst(struct, value);
                f8.serializeFieldInst(struct, value);
                f9.serializeFieldInst(struct, value);
                f10.serializeFieldInst(struct, value);
                f11.serializeFieldInst(struct, value);
                f12.serializeFieldInst(struct, value);
                f13.serializeFieldInst(struct, value);
                f14.serializeFieldInst(struct, value);
                f15.serializeFieldInst(struct, value);
            }

            @Override
            public S decode(Deserializer.Struct struct) {
                return constructor.apply(f1.deserializeField(struct),
                        f2.deserializeField(struct),
                        f3.deserializeField(struct),
                        f4.deserializeField(struct),
                        f5.deserializeField(struct),
                        f6.deserializeField(struct),
                        f7.deserializeField(struct),
                        f8.deserializeField(struct),
                        f9.deserializeField(struct),
                        f10.deserializeField(struct),
                        f11.deserializeField(struct),
                        f12.deserializeField(struct),
                        f13.deserializeField(struct),
                        f14.deserializeField(struct),
                        f15.deserializeField(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15, F16> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, StructField<S, F12> f12, StructField<S, F13> f13, StructField<S, F14> f14, StructField<S, F15> f15, StructField<S, F16> f16, Function16<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15, F16, S> constructor){
        return new StructEndec<>() {
            @Override
            public void encode(Serializer.Struct struct, S value) {
                f1.serializeFieldInst(struct, value);
                f2.serializeFieldInst(struct, value);
                f3.serializeFieldInst(struct, value);
                f4.serializeFieldInst(struct, value);
                f5.serializeFieldInst(struct, value);
                f6.serializeFieldInst(struct, value);
                f7.serializeFieldInst(struct, value);
                f8.serializeFieldInst(struct, value);
                f9.serializeFieldInst(struct, value);
                f10.serializeFieldInst(struct, value);
                f11.serializeFieldInst(struct, value);
                f12.serializeFieldInst(struct, value);
                f13.serializeFieldInst(struct, value);
                f14.serializeFieldInst(struct, value);
                f15.serializeFieldInst(struct, value);
                f16.serializeFieldInst(struct, value);
            }

            @Override
            public S decode(Deserializer.Struct struct) {
                return constructor.apply(f1.deserializeField(struct),
                        f2.deserializeField(struct),
                        f3.deserializeField(struct),
                        f4.deserializeField(struct),
                        f5.deserializeField(struct),
                        f6.deserializeField(struct),
                        f7.deserializeField(struct),
                        f8.deserializeField(struct),
                        f9.deserializeField(struct),
                        f10.deserializeField(struct),
                        f11.deserializeField(struct),
                        f12.deserializeField(struct),
                        f13.deserializeField(struct),
                        f14.deserializeField(struct),
                        f15.deserializeField(struct),
                        f16.deserializeField(struct));
            }
        };
    }

    public static <S, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15, F16, F17> Endec<S> of(StructField<S, F1> f1, StructField<S, F2> f2, StructField<S, F3> f3, StructField<S, F4> f4, StructField<S, F5> f5, StructField<S, F6> f6, StructField<S, F7> f7, StructField<S, F8> f8, StructField<S, F9> f9, StructField<S, F10> f10, StructField<S, F11> f11, StructField<S, F12> f12, StructField<S, F13> f13, StructField<S, F14> f14, StructField<S, F15> f15, StructField<S, F16> f16, StructField<S, F17> f17, Function17<F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15, F16, F17, S> constructor){
        return new StructEndec<>() {
            @Override
            public void encode(Serializer.Struct struct, S value) {
                f1.serializeFieldInst(struct, value);
                f2.serializeFieldInst(struct, value);
                f3.serializeFieldInst(struct, value);
                f4.serializeFieldInst(struct, value);
                f5.serializeFieldInst(struct, value);
                f6.serializeFieldInst(struct, value);
                f7.serializeFieldInst(struct, value);
                f8.serializeFieldInst(struct, value);
                f9.serializeFieldInst(struct, value);
                f10.serializeFieldInst(struct, value);
                f11.serializeFieldInst(struct, value);
                f12.serializeFieldInst(struct, value);
                f13.serializeFieldInst(struct, value);
                f14.serializeFieldInst(struct, value);
                f15.serializeFieldInst(struct, value);
                f16.serializeFieldInst(struct, value);
                f17.serializeFieldInst(struct, value);
            }

            @Override
            public S decode(Deserializer.Struct struct) {
                return constructor.apply(f1.deserializeField(struct),
                        f2.deserializeField(struct),
                        f3.deserializeField(struct),
                        f4.deserializeField(struct),
                        f5.deserializeField(struct),
                        f6.deserializeField(struct),
                        f7.deserializeField(struct),
                        f8.deserializeField(struct),
                        f9.deserializeField(struct),
                        f10.deserializeField(struct),
                        f11.deserializeField(struct),
                        f12.deserializeField(struct),
                        f13.deserializeField(struct),
                        f14.deserializeField(struct),
                        f15.deserializeField(struct),
                        f16.deserializeField(struct),
                        f17.deserializeField(struct));
            }
        };
    }

    // Here as a one up for using endec... really based though
    public interface Function17<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R> {
        R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9, T10 t10, T11 t11, T12 t12, T13 t13, T14 t14, T15 t15, T16 t16, T17 t17);
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
                    return new StructEndec<>() {
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
