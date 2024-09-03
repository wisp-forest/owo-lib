package io.wispforest.owo.config;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import io.wispforest.owo.ops.TextOps;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
public class OwoConfigCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess access) {
        dispatcher.register(ClientCommandManager.literal("owo-config")
                .then(ClientCommandManager.argument("config_id", new ConfigScreenArgumentType())
                        .executes(context -> {
                            var screen = context.getArgument("config_id", ConfigScreen.class);
                            MinecraftClient.getInstance().send(() -> MinecraftClient.getInstance().setScreen(screen));
                            return 0;
                        })));
    }

    private static class ConfigScreenArgumentType implements ArgumentType<Screen> {

        private static final SimpleCommandExceptionType NO_SUCH_CONFIG_SCREEN = new SimpleCommandExceptionType(
                TextOps.concat(Owo.PREFIX, Text.literal("no config screen with that id"))
        );

        @Override
        public Screen parse(StringReader reader) throws CommandSyntaxException {
            var provider = ConfigScreenProviders.get(reader.readString());
            if (provider == null) throw NO_SUCH_CONFIG_SCREEN.create();

            return provider.apply(null);
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            var configNames = new ArrayList<String>();
            ConfigScreenProviders.forEach((s, screenFunction) -> configNames.add(s));
            return CommandSource.suggestMatching(configNames, builder);
        }
    }
}
