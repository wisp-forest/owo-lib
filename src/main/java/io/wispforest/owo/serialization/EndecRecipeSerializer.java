package io.wispforest.owo.serialization;

import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationAttributes;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.StructEndec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class EndecRecipeSerializer {

    public static <T extends Recipe<?>> RecipeSerializer<T> create(StructEndec<T> endec) {
        return create(endec, endec);
    }

    public static <T extends Recipe<?>> RecipeSerializer<T> create(StructEndec<T> endec, Endec<T> networkEndec) {
        return new RecipeSerializer<>(
            CodecUtils.toMapCodec(endec, SerializationContext.attributes(SerializationAttributes.HUMAN_READABLE)),
            CodecUtils.toPacketCodec(networkEndec)
        );
    }
}
