package io.wispforest.owo.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.LiteralText;

import java.util.Arrays;
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
    private final Class<T> enumClass;

    private EnumArgumentType(Class<T> enumClass, String noElementMessage) {
        this.enumClass = enumClass;
        this.noValueException = new DynamicCommandExceptionType(o -> new LiteralText(noElementMessage.replace("{}", o.toString())));
    }

    /**
     * Creates a new instance that uses {@code Invalid enum value '{}'} as the
     * error message if an invalid value is supplied
     *
     * @param enumClass The {@code enum} type to parse for
     * @param <T>       The {@code enum} type to parse for
     * @return A new argument type that can parse instances of {@code T}
     */
    public static <T extends Enum<T>> EnumArgumentType<T> create(Class<T> enumClass) {
        return new EnumArgumentType<>(enumClass, "Invalid enum value '{}'");
    }

    /**
     * Creates a new instance that uses {@code noElementMessage} as the
     * error message if an invalid value is supplied
     *
     * @param enumClass        The {@code enum} type to parse for
     * @param noElementMessage The error message to send if an invalid value is
     *                         supplied, with an optional {@code {}} placeholder
     *                         for the supplied value
     * @param <T>              The {@code enum} type to parse for
     * @return A new argument type that can parse instances of {@code T}
     */
    public static <T extends Enum<T>> EnumArgumentType<T> create(Class<T> enumClass, String noElementMessage) {
        return new EnumArgumentType<>(enumClass, noElementMessage);
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
}
