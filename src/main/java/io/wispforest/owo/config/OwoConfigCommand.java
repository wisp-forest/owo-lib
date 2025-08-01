package io.wispforest.owo.config;

import blue.endless.jankson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.owo.command.RecordArgumentTypeInfo;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.packets.OwoPackets;
import io.wispforest.owo.packets.s2c.OpenServerConfig;
import io.wispforest.owo.packets.s2c.OpenServerConfigSelection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
public class OwoConfigCommand {

    private static final SimpleCommandExceptionType NO_SUCH_CONFIG_SCREEN = new SimpleCommandExceptionType(
            TextOps.concat(Owo.PREFIX, Text.literal("no config screen with that id"))
    );

    @Environment(EnvType.CLIENT)
    public static void registerClient(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess access) {
        dispatcher.register(ClientCommandManager.literal("owo-config")
                .then(ClientCommandManager.literal("reload")
                        .then(ClientCommandManager.argument("config_id", ConfigIdentifierArgumentType.INSTANCE)
                                .executes(context -> {
                                    var configWrapper = context.getArgument("config_id", ConfigWrapper.class);
                                    configWrapper.loadFile();
                                    return 0;
                                }))
                )
                .then(ClientCommandManager.literal("open")
                        .then(ClientCommandManager.argument("config_id", ConfigIdentifierArgumentType.INSTANCE)
                                .executes(context -> {
                                    if (!ConfigScreenProviders.safelyOpenConfigScreen(context.getArgument("config_id", Identifier.class), null, Map.of())) {
                                        throw NO_SUCH_CONFIG_SCREEN.create();
                                    }

                                    return 0;
                                })
                        )
                        .then(ClientCommandManager.argument("mod_id", ConfigurableModArgumentType.INSTANCE)
                                .executes(context -> {
                                    var modId = context.getArgument("mod_id", String.class);

                                    ConfigScreenProviders.safelyOpenConfigScreen(modId, null, Map.of());

                                    return 0;
                                })
                        )
                ));
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("owo-config-server")
                    .requires(source -> source.hasPermissionLevel(3))
                    .then(CommandManager.literal("reload")
                            .then(CommandManager.argument("config_id", ConfigIdentifierArgumentType.INSTANCE)
                                    .executes(context -> {
                                        var wrapper = ConfigWrapper.getKnownConfigInstances().get(context.getArgument("config_id", Identifier.class));
                                        wrapper.loadFile();
                                        return 0;
                                    }))
                    )
                    .then(CommandManager.literal("open")
                            .then(CommandManager.argument("config_id", ConfigIdentifierArgumentType.INSTANCE)
                                    .executes(context -> {
                                        var wrapper = ConfigWrapper.getKnownConfigInstances().get(context.getArgument("config_id", Identifier.class));

                                        if (wrapper == null) throw NO_SUCH_CONFIG_SCREEN.create();

                                        OwoPackets.MAIN.serverHandle(context.getSource().getPlayerOrThrow())
                                                .send(new OpenServerConfig(wrapper.id(), wrapper.saveToObject()));

                                        return 0;
                                    })
                            )
                            .then(CommandManager.argument("mod_id", ConfigurableModArgumentType.INSTANCE)
                                    .executes(context -> {
                                        var handler = OwoPackets.MAIN.serverHandle(context.getSource().getPlayerOrThrow());
                                        var modId = context.getArgument("mod_id", String.class);

                                        var modSpecificConfigs = ConfigWrapper.getGroupedConfigInstances().get(modId);

                                        if (modSpecificConfigs.size() == 1) {
                                            var wrapper = List.copyOf(modSpecificConfigs.values()).getFirst();

                                            handler.send(new OpenServerConfig(wrapper.id(), wrapper.saveToObject()));
                                        } else {
                                            var availableConfigs = new LinkedHashMap<Identifier, JsonObject>();

                                            modSpecificConfigs.forEach((string, wrapper) -> availableConfigs.put(wrapper.id(), wrapper.saveToObject()));

                                            handler.send(new OpenServerConfigSelection(modId, availableConfigs));
                                        }

                                        return 0;
                                    })
                            )
                    )
            );
        });

        ArgumentTypeRegistry.registerArgumentType(
                Identifier.of("owo", "config_argument"),
                ConfigIdentifierArgumentType.class,
                RecordArgumentTypeInfo.of(commandRegistryAccess -> new ConfigIdentifierArgumentType())
        );

        ArgumentTypeRegistry.registerArgumentType(
                Identifier.of("owo", "configurable_mod_argument"),
                ConfigurableModArgumentType.class,
                RecordArgumentTypeInfo.of(commandRegistryAccess -> new ConfigurableModArgumentType())
        );
    }

    private static class ConfigurableModArgumentType implements ArgumentType<String> {
        public static final ConfigurableModArgumentType INSTANCE = new ConfigurableModArgumentType();

        private static final SimpleCommandExceptionType NO_SUCH_CONFIGURABLE_MOD = new SimpleCommandExceptionType(
                TextOps.concat(Owo.PREFIX, Text.literal("no mod with that id"))
        );

        @Override
        public String parse(StringReader reader) throws CommandSyntaxException {
            var mod = reader.readUnquotedString();

            if (getConfigurableMods().get(mod) == null) throw NO_SUCH_CONFIGURABLE_MOD.create();

            return mod;
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return CommandSource.suggestMatching(getConfigurableMods().keySet(), builder);
        }

        private static Map<String, MutableInt> getConfigurableMods() {
            return ConfigWrapper.getKnownConfigInstances().keySet().stream()
                    .map(Identifier::getNamespace)
                    .<Map<String, MutableInt>>collect(LinkedHashMap::new, (map, string) -> {
                        map.computeIfAbsent(string, string1 -> new MutableInt(0))
                                .add(1);
                    }, (map1, map2) -> {
                        map2.forEach((key, value) -> {
                            map1.computeIfAbsent(key, string1 -> new MutableInt(0))
                                    .add(value);
                        });
                    });
        }
    }
}
