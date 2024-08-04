package io.wispforest.owo.command.debug;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.logging.LogUtils;
import io.wispforest.owo.Owo;
import io.wispforest.owo.command.EnumArgumentType;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.renderdoc.RenderDoc;
import io.wispforest.owo.renderdoc.RenderDoc.Capture;
import io.wispforest.owo.renderdoc.RenderdocScreen;
import io.wispforest.owo.ui.hud.HudInspectorScreen;
import io.wispforest.owo.ui.parsing.ConfigureHotReloadScreen;
import io.wispforest.owo.ui.parsing.UIModelLoader;
import java.lang.reflect.Field;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.TextFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableText;
import net.minecraft.network.chat.Text;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.event.Level;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@ApiStatus.Internal
public class OwoDebugCommands {

    private static final EnumArgumentType<Level> LEVEL_ARGUMENT_TYPE =
            EnumArgumentType.create(Level.class, "'{}' is not a valid logging level");

    private static final SuggestionProvider<CommandSourceStack> POI_TYPES =
            (context, builder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.POINT_OF_INTEREST_TYPE.idSet(), builder);

    private static final SimpleCommandExceptionType NO_POI_TYPE = new SimpleCommandExceptionType(Text.nullToEmpty("Invalid POI type"));
    public static final int GENERAL_PURPLE = 0xB983FF;
    public static final int KEY_BLUE = 0x94B3FD;
    public static final int VALUE_BLUE = 0x94DAFF;

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            dispatcher.register(literal("logger").then(argument("level", LEVEL_ARGUMENT_TYPE).executes(context -> {
                final var level = LEVEL_ARGUMENT_TYPE.get(context, "level");
                LogUtils.configureRootLoggingLevel(level);

                context.getSource().sendSuccess(() -> TextOps.concat(Owo.PREFIX, Text.nullToEmpty("global logging level set to: §9" + level)), false);
                return 0;
            })));

            dispatcher.register(literal("query-poi").then(argument("poi_type", IdentifierArgument.id()).suggests(POI_TYPES)
                    .then(argument("radius", IntegerArgumentType.integer()).executes(context -> {
                        var player = context.getSource().getPlayer();
                        var poiType = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getOptional(IdentifierArgument.getId(context, "poi_type"))
                                .orElseThrow(NO_POI_TYPE::create);

                        var entries = ((ServerLevel) player.level()).getPoiManager().getInRange(type -> type.value() == poiType,
                                player.getBlockPos(), IntegerArgumentType.getInteger(context, "radius"), PoiManager.Occupancy.ANY).toList();

                        player.displayClientMessage(TextOps.concat(Owo.PREFIX, TextOps.withColor("Found §" + entries.size() + " §entr" + (entries.size() == 1 ? "y" : "ies"),
                                TextOps.color(TextFormatting.GRAY), GENERAL_PURPLE, TextOps.color(TextFormatting.GRAY))), false);

                        for (var entry : entries) {

                            final var entryPos = entry.getPos();
                            final var blockId = BuiltInRegistries.BLOCK.getId(player.level().getBlockState(entryPos).getBlock()).toString();
                            final var posString = "(" + entryPos.getX() + " " + entryPos.getY() + " " + entryPos.getZ() + ")";

                            final var message = TextOps.withColor("-> §" + blockId + " §" + posString,
                                    TextOps.color(TextFormatting.GRAY), KEY_BLUE, VALUE_BLUE);

                            message.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                            "/tp " + entryPos.getX() + " " + entryPos.getY() + " " + entryPos.getZ()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.nullToEmpty("Click to teleport"))));

                            player.displayClientMessage(message, false);
                        }

                        return entries.size();
                    }))));

            dispatcher.register(literal("dumpfield").then(argument("field_name", StringArgumentType.string()).executes(context -> {
                final var targetField = StringArgumentType.getString(context, "field_name");
                final CommandSourceStack source = context.getSource();
                final ServerPlayer player = source.getPlayer();
                HitResult target = player.pick(5, 0, false);

                if (target.getType() != HitResult.Type.BLOCK) {
                    source.sendFailure(TextOps.concat(Owo.PREFIX, Text.literal("You're not looking at a block")));
                    return 1;
                }

                BlockPos pos = ((BlockHitResult) target).getBlockPos();
                final var blockEntity = player.level().getBlockEntity(pos);

                if (blockEntity == null) {
                    source.sendFailure(TextOps.concat(Owo.PREFIX, Text.literal(("No block entity"))));
                    return 1;
                }

                var blockEntityClass = blockEntity.getClass();

                try {
                    final var field = blockEntityClass.getDeclaredField(targetField);

                    if (!field.canAccess(blockEntity)) field.setAccessible(true);
                    final var value = field.get(blockEntity);

                    source.sendSuccess(() -> TextOps.concat(Owo.PREFIX, TextOps.withColor("Field value: §" + value, TextOps.color(TextFormatting.GRAY), KEY_BLUE)), false);

                } catch (Exception e) {
                    source.sendFailure(TextOps.concat(Owo.PREFIX, Text.literal("Could not access field - " + e.getClass().getSimpleName() + ": " + e.getMessage())));
                }

                return 0;
            })));

            MakeLootContainerCommand.register(dispatcher, registryAccess);
            DumpdataCommand.register(dispatcher);
            HealCommand.register(dispatcher);

            if (FabricLoader.getInstance().isModLoaded("cardinal-components-base")) {
                CcaDataCommand.register(dispatcher);
            }
        });
    }

    @Environment(EnvType.CLIENT)
    public static class Client {

        private static final SuggestionProvider<FabricClientCommandSource> LOADED_UI_MODELS =
                (context, builder) -> SharedSuggestionProvider.suggestResource(UIModelLoader.allLoadedModels(), builder);

        private static final SimpleCommandExceptionType NO_SUCH_UI_MODEL = new SimpleCommandExceptionType(Text.literal("No such UI model is loaded"));

        public static void register() {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
                dispatcher.register(ClientCommandManager.literal("owo-hud-inspect")
                        .executes(context -> {
                            Minecraft.getInstance().setScreen(new HudInspectorScreen());
                            return 0;
                        }));

                dispatcher.register(ClientCommandManager.literal("owo-ui-set-reload-path")
                        .then(ClientCommandManager.argument("model-id", IdentifierArgument.id()).suggests(LOADED_UI_MODELS).executes(context -> {
                            var modelId = context.getArgument("model-id", Identifier.class);
                            if (UIModelLoader.getPreloaded(modelId) == null) throw NO_SUCH_UI_MODEL.create();

                            Minecraft.getInstance().setScreen(new ConfigureHotReloadScreen(modelId, null));
                            return 0;
                        })));

                if (RenderDoc.isAvailable()) {
                    dispatcher.register(ClientCommandManager.literal("renderdoc").executes(context -> {
                        Minecraft.getInstance().setScreen(new RenderdocScreen());
                        return 1;
                    }).then(ClientCommandManager.literal("comment")
                            .then(ClientCommandManager.argument("capture_index", IntegerArgumentType.integer(0))
                                    .then(ClientCommandManager.argument("comment", StringArgumentType.greedyString())
                                            .executes(context -> {
                                                var capture = RenderDoc.getCapture(IntegerArgumentType.getInteger(context, "capture_index"));
                                                if (capture == null) {
                                                    context.getSource().sendError(TextOps.concat(Owo.PREFIX, Text.nullToEmpty("no such capture")));
                                                    return 0;
                                                }

                                                RenderDoc.setCaptureComments(capture, StringArgumentType.getString(context, "comment"));
                                                context.getSource().sendFeedback(TextOps.concat(Owo.PREFIX, Text.nullToEmpty("comment updated")));

                                                return 1;
                                            })))));
                }
            });
        }
    }
}
