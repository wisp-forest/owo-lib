package io.wispforest.owo.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer;
import io.wispforest.endec.format.bytebuf.ByteBufSerializer;
import io.wispforest.endec.format.gson.GsonSerializer;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;

import java.util.function.BiFunction;
import java.util.function.Function;

public record RecordArgumentTypeInfo<A extends ArgumentType<?>, T>(StructEndec<T> endec, Function<A, T> toTemplate, BiFunction<CommandRegistryAccess, T, A> fromTemplate) implements ArgumentSerializer<A, RecordArgumentTypeInfo.RecordInfoTemplate<A, T>> {

    public static <A extends ArgumentType<?>> RecordArgumentTypeInfo<A, Void> of(Function<CommandRegistryAccess, A> argTypeConstructor) {
        return new RecordArgumentTypeInfo<>(Endec.unit(() -> null), a -> null, (commandBuildContext, unused) -> argTypeConstructor.apply(commandBuildContext));
    }

    @Override
    public void writePacket(RecordInfoTemplate<A, T> template, PacketByteBuf buffer) {
        endec.encodeFully(() -> ByteBufSerializer.of(buffer), template.data());
    }

    @Override
    public RecordInfoTemplate<A, T> fromPacket(PacketByteBuf buffer) {
        return new RecordInfoTemplate<>(this, endec.decodeFully(ByteBufDeserializer::of, buffer), fromTemplate);
    }

    @Override
    public void writeJson(RecordInfoTemplate<A, T> template, JsonObject json) {
        json.asMap().putAll(((JsonObject) endec.encodeFully(GsonSerializer::of, template.data())).asMap());
    }

    @Override
    public RecordInfoTemplate<A, T> getArgumentTypeProperties(A argument) {
        return new RecordInfoTemplate<>(this, toTemplate.apply(argument), fromTemplate);
    }

    public record RecordInfoTemplate<A extends ArgumentType<?>, T>(ArgumentSerializer<A, ?> type, T data, BiFunction<CommandRegistryAccess, T, A> fromTemplate) implements ArgumentTypeProperties<A> {
        @Override
        public A createType(CommandRegistryAccess ctx) {
            return fromTemplate.apply(ctx, data());
        }

        @Override
        public ArgumentSerializer<A, ?> getSerializer() {
            return type;
        }
    }
}
