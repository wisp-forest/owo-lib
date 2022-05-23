package io.wispforest.owo.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.wispforest.owo.mixin.ArgumentTypesInvoker;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * A simple implementation of {@link ArgumentType} that works with any {@code enum}.
 * It is recommended to create one instance of this and use it both in the call
 * to {@link net.minecraft.server.command.CommandManager#argument(String, ArgumentType)}
 * as well as for getting the supplied argument via {@link #get(CommandContext, String)}
 *
 * @param <T> The {@code enum} this instance can parse
 */
public class EnumArgumentType<T extends Enum<T>> implements ArgumentType<Enum<T>> {

    private final DynamicCommandExceptionType noValueException;
    private final String noElementMessage;
    private final Class<T> enumClass;

    private EnumArgumentType(Class<T> enumClass, String noElementMessage) {
        this.enumClass = enumClass;
        this.noElementMessage = noElementMessage;
        this.noValueException = new DynamicCommandExceptionType(o -> Text.literal(this.noElementMessage.replace("{}", o.toString())));
    }

    /**
     * Creates a new instance that uses {@code Invalid enum value '{}'} as the
     * error message if an invalid value is supplied. This <b>must</b> be called
     * on both <b>server and client</b> so the serializer can be registered correctly
     *
     * @param enumClass The {@code enum} type to parse for
     * @param <T>       The {@code enum} type to parse for
     * @return A new argument type that can parse instances of {@code T}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Enum<T>> EnumArgumentType<T> create(Class<T> enumClass) {
        final var type = new EnumArgumentType<>(enumClass, "Invalid enum value '{}'");
        ArgumentTypesInvoker.owo$register(Registry.COMMAND_ARGUMENT_TYPE, "owo:enum_" + enumClass.getName().toLowerCase(Locale.ROOT), type.getClass(), new Serializer(type));
        return type;
    }

    /**
     * Creates a new instance that uses {@code noElementMessage} as the
     * error message if an invalid value is supplied. This <b>must</b> be called
     * on both <b>server and client</b> so the serializer can be registered correctly
     *
     * @param enumClass        The {@code enum} type to parse for
     * @param noElementMessage The error message to send if an invalid value is
     *                         supplied, with an optional {@code {}} placeholder
     *                         for the supplied value
     * @param <T>              The {@code enum} type to parse for
     * @return A new argument type that can parse instances of {@code T}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Enum<T>> EnumArgumentType<T> create(Class<T> enumClass, String noElementMessage) {
        final var type = new EnumArgumentType<>(enumClass, noElementMessage);
        ArgumentTypesInvoker.owo$register(Registry.COMMAND_ARGUMENT_TYPE, "owo:enum_" + enumClass.getName().toLowerCase(Locale.ROOT), type.getClass(), new Serializer(type));
        return type;
    }

    public T get(CommandContext<?> context, String name) {
        return context.getArgument(name, enumClass);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Arrays.stream(enumClass.getEnumConstants()).map(Enum::toString), builder);
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        final var name = reader.readString();
        try {
            return Enum.valueOf(enumClass, name);
        } catch (IllegalArgumentException e) {
            throw noValueException.create(name);
        }
    }

    public static final class Serializer<T extends Enum<T>, TypeInstance extends EnumArgumentType<T>> implements ArgumentSerializer<TypeInstance, ArgumentSerializer.ArgumentTypeProperties<TypeInstance>> {

        private final EnumArgumentType.ArgumentTypeProperties<T, TypeInstance> properties;

        public Serializer(TypeInstance type) {
            this.properties = new EnumArgumentType.ArgumentTypeProperties<>(type, this);
        }

        @Override
        public ArgumentTypeProperties<TypeInstance> fromPacket(PacketByteBuf buf) {
            return this.properties;
        }

        @Override
        public ArgumentTypeProperties<TypeInstance> getArgumentTypeProperties(TypeInstance argumentType) {
            return this.properties;
        }

        @Override
        public void writePacket(ArgumentTypeProperties properties, PacketByteBuf buf) {}

        @Override
        public void writeJson(ArgumentTypeProperties properties, JsonObject json) {}
    }

    private static final class ArgumentTypeProperties<T extends Enum<T>, TypeInstance extends EnumArgumentType<T>> implements ArgumentSerializer.ArgumentTypeProperties<TypeInstance> {

        private final TypeInstance instance;
        private final Serializer<T, TypeInstance> serializer;

        private ArgumentTypeProperties(TypeInstance instance, Serializer<T, TypeInstance> serializer) {
            this.instance = instance;
            this.serializer = serializer;
        }

        @Override
        public TypeInstance createType(CommandRegistryAccess commandRegistryAccess) {
            return this.instance;
        }

        @Override
        public ArgumentSerializer<TypeInstance, ?> getSerializer() {
            return this.serializer;
        }
    }
}
