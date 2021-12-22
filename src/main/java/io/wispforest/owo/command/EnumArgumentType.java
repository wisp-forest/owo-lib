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

public class EnumArgumentType<T extends Enum<T>> implements ArgumentType<Enum<T>> {

    private final DynamicCommandExceptionType noValueException;
    private final Class<T> enumClass;

    private EnumArgumentType(Class<T> enumClass, String noElementMessage) {
        this.enumClass = enumClass;
        this.noValueException = new DynamicCommandExceptionType(o -> new LiteralText(noElementMessage.replace("{}", o.toString())));
    }

    public static <T extends Enum<T>> EnumArgumentType<T> create(Class<T> enumClass) {
        return new EnumArgumentType<>(enumClass, "Invalid enum value '{}'");
    }

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
