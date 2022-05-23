package io.wispforest.owo.command.debug;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.logging.LogUtils;
import io.wispforest.owo.Owo;
import io.wispforest.owo.command.EnumArgumentType;
import io.wispforest.owo.ops.TextOps;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.event.Level;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@ApiStatus.Internal
public class OwoDebugCommands {

    private static final EnumArgumentType<Level> LEVEL_ARGUMENT_TYPE =
            EnumArgumentType.create(Level.class, "'{}' is not a valid logging level");

    private static final SuggestionProvider<ServerCommandSource> POI_TYPES =
            (context, builder) -> CommandSource.suggestIdentifiers(Registry.POINT_OF_INTEREST_TYPE.getIds(), builder);

    private static final SimpleCommandExceptionType NO_POI_TYPE = new SimpleCommandExceptionType(Text.of("Invalid POI type"));
    public static final int GENERAL_PURPLE = 0xB983FF;
    public static final int KEY_BLUE = 0x94B3FD;
    public static final int VALUE_BLUE = 0x94DAFF;

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("logger").then(argument("level", LEVEL_ARGUMENT_TYPE).executes(context -> {
                final var level = LEVEL_ARGUMENT_TYPE.get(context, "level");
                LogUtils.configureRootLoggingLevel(level);

                context.getSource().sendFeedback(TextOps.concat(Owo.PREFIX, Text.of("global logging level set to: §9" + level)), false);
                return 0;
            })));

            dispatcher.register(literal("query-poi").then(argument("poi_type", IdentifierArgumentType.identifier()).suggests(POI_TYPES)
                    .then(argument("radius", IntegerArgumentType.integer()).executes(context -> {
                        var player = context.getSource().getPlayer();
                        var poiType = Registry.POINT_OF_INTEREST_TYPE.getOrEmpty(IdentifierArgumentType.getIdentifier(context, "poi_type"))
                                .orElseThrow(NO_POI_TYPE::create);

                        var entries = ((ServerWorld) player.world).getPointOfInterestStorage().getInCircle(type -> type.value() == poiType,
                                player.getBlockPos(), IntegerArgumentType.getInteger(context, "radius"), PointOfInterestStorage.OccupationStatus.ANY).toList();

                        player.sendMessage(TextOps.concat(Owo.PREFIX, TextOps.withColor("Found §" + entries.size() + " §entr" + (entries.size() == 1 ? "y" : "ies"),
                                TextOps.color(Formatting.GRAY), GENERAL_PURPLE, TextOps.color(Formatting.GRAY))), false);

                        for (var entry : entries) {

                            final var entryPos = entry.getPos();
                            final var blockId = Registry.BLOCK.getId(player.world.getBlockState(entryPos).getBlock()).toString();
                            final var posString = "(" + entryPos.getX() + " " + entryPos.getY() + " " + entryPos.getZ() + ")";

                            final var message = TextOps.withColor("-> §" + blockId + " §" + posString,
                                    TextOps.color(Formatting.GRAY), KEY_BLUE, VALUE_BLUE);

                            message.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                            "/tp " + entryPos.getX() + " " + entryPos.getY() + " " + entryPos.getZ()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Click to teleport"))));

                            player.sendMessage(message, false);
                        }

                        return entries.size();
                    }))));

            dispatcher.register(literal("dumpfield").then(argument("field_name", StringArgumentType.string()).executes(context -> {
                final var targetField = StringArgumentType.getString(context, "field_name");
                final ServerCommandSource source = context.getSource();
                final ServerPlayerEntity player = source.getPlayer();
                HitResult target = player.raycast(5, 0, false);

                if (target.getType() != HitResult.Type.BLOCK) {
                    source.sendError(TextOps.concat(Owo.PREFIX, Text.literal("You're not looking at a block")));
                    return 1;
                }

                BlockPos pos = ((BlockHitResult) target).getBlockPos();
                final var blockEntity = player.getWorld().getBlockEntity(pos);

                if (blockEntity == null) {
                    source.sendError(TextOps.concat(Owo.PREFIX, Text.literal(("No block entity"))));
                    return 1;
                }

                var blockEntityClass = blockEntity.getClass();

                try {
                    final var field = blockEntityClass.getDeclaredField(targetField);

                    if (!field.canAccess(blockEntity)) field.setAccessible(true);
                    final var value = field.get(blockEntity);

                    source.sendFeedback(TextOps.concat(Owo.PREFIX, TextOps.withColor("Field value: §" + value, TextOps.color(Formatting.GRAY), KEY_BLUE)), false);

                } catch (Exception e) {
                    source.sendError(TextOps.concat(Owo.PREFIX, Text.literal("Could not access field - " + e.getClass().getSimpleName() + ": " + e.getMessage())));
                }

                return 0;
            })));

            MakeLootContainerCommand.register(dispatcher, registryAccess);
            DumpdataCommand.register(dispatcher);
            DamageCommand.register(dispatcher);
            HealCommand.register(dispatcher);

            if (FabricLoader.getInstance().isModLoaded("cardinal-components-base")) {
                CcaDataCommand.register(dispatcher);
            }
        });
    }
}
