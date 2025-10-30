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

import java.util.function.Function;

///
/// An [Endec] based [ArgumentSerializer] requiring a [StructEndec] and
///
public record EndecArgumentSerializer<A extends ArgumentType<?>, T>(StructEndec<T> endec, PropertiesFactory<A, T> propertiesFactory, ArgumentTypeFactory<A, T> argumentTypeFactory)
    implements ArgumentSerializer<A, EndecArgumentSerializer.EndecArgumentTypeProperties<A, T>> {

    public static <A extends ArgumentType<?>> EndecArgumentSerializer<A, Void> of(Function<CommandRegistryAccess, A> argTypeConstructor) {
        return new EndecArgumentSerializer<>(Endec.unit(() -> null), a -> null, (commandBuildContext, unused) -> argTypeConstructor.apply(commandBuildContext));
    }

    @Override
    public void writePacket(EndecArgumentTypeProperties<A, T> properties, PacketByteBuf buffer) {
        endec.encodeFully(() -> ByteBufSerializer.of(buffer), properties.data());
    }

    @Override
    public EndecArgumentTypeProperties<A, T> fromPacket(PacketByteBuf buffer) {
        return new EndecArgumentTypeProperties<>(this, endec.decodeFully(ByteBufDeserializer::of, buffer), argumentTypeFactory);
    }

    @Override
    public void writeJson(EndecArgumentTypeProperties<A, T> properties, JsonObject json) {
        json.asMap().putAll(((JsonObject) endec.encodeFully(GsonSerializer::of, properties.data())).asMap());
    }

    @Override
    public EndecArgumentTypeProperties<A, T> getArgumentTypeProperties(A argument) {
        return new EndecArgumentTypeProperties<>(this, propertiesFactory.create(argument), argumentTypeFactory);
    }

    public record EndecArgumentTypeProperties<A extends ArgumentType<?>, T>(ArgumentSerializer<A, ?> type, T data, ArgumentTypeFactory<A, T> argumentFactory) implements ArgumentTypeProperties<A> {
        @Override
        public A createType(CommandRegistryAccess ctx) {
            return argumentFactory.create(ctx, data());
        }

        @Override
        public ArgumentSerializer<A, ?> getSerializer() {
            return type;
        }
    }

    public interface PropertiesFactory<A extends ArgumentType<?>, T> {
        T create(A argumentType);
    }

    public interface ArgumentTypeFactory<A extends ArgumentType<?>, T> {
        A create(CommandRegistryAccess access, T properties);
    }
}
