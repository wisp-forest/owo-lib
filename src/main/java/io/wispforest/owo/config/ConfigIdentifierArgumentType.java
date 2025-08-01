package io.wispforest.owo.config;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class ConfigIdentifierArgumentType implements ArgumentType<Identifier> {

    public static final ConfigIdentifierArgumentType INSTANCE = new ConfigIdentifierArgumentType();

    private static final SimpleCommandExceptionType NO_SUCH_CONFIG_SCREEN = new SimpleCommandExceptionType(
            TextOps.concat(Owo.PREFIX, Text.literal("no config with that id"))
    );

    @Override
    public Identifier parse(StringReader reader) throws CommandSyntaxException {
        var id = Identifier.fromCommandInput(reader);
        var wrapper = ConfigWrapper.getKnownConfigInstances().get(id);
        if (wrapper == null) throw NO_SUCH_CONFIG_SCREEN.create();

        return id;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var configNames = new ArrayList<String>();
        ConfigWrapper.getKnownConfigInstances().keySet().forEach(s -> configNames.add(s.toString()));
        return CommandSource.suggestMatching(configNames, builder);
    }
}
