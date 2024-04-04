package io.wispforest.owo.serialization.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.SerializationAttribute;
import io.wispforest.owo.serialization.StructEndec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;

public abstract class EndecRecipeSerializer<R extends Recipe<?>> implements RecipeSerializer<R> {

    private final StructEndec<R> endec;
    private final PacketCodec<PacketByteBuf, R> packetCodec;
    private final MapCodec<R> codec;

    protected EndecRecipeSerializer(StructEndec<R> endec, Endec<R> networkEndec) {
        this.endec = endec;
        this.packetCodec = networkEndec.packetCodec();
        this.codec = this.endec.mapCodec(SerializationAttribute.HUMAN_READABLE);
    }

    protected EndecRecipeSerializer(StructEndec<R> endec) {
        this(endec, endec);
    }

    @Override
    public MapCodec<R> codec() {
        return this.codec;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PacketCodec<RegistryByteBuf, R> packetCodec() {
        // Don't ya just love Java generics?
        return (PacketCodec<RegistryByteBuf, R>)(Object) packetCodec;
    }
}
