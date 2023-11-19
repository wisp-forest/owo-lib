package io.wispforest.uwu.recipe;

import com.mojang.serialization.Codec;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.impl.SerializationAttribute;
import io.wispforest.owo.serialization.impl.bytebuf.ByteBufDeserializer;
import io.wispforest.owo.serialization.impl.bytebuf.ByteBufSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;

public class EndecRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {

    public final Endec<T> endec;
    public final Codec<T> codec;

    public EndecRecipeSerializer(Endec<T> endec){
        this.codec = endec.codec(SerializationAttribute.HUMAN_READABLE);
        this.endec = endec;
    }

    @Override
    public Codec<T> codec() {
        return this.codec;
    }

    @Override
    public void write(PacketByteBuf buf, T recipe) {
        this.endec.encode(new ByteBufSerializer<>(buf), recipe);
    }

    @Override
    public T read(PacketByteBuf buf) {
        return this.endec.decode(new ByteBufDeserializer(buf));
    }
}
