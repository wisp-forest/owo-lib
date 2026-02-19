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
import io.wispforest.owo.renderdoc.RenderdocScreen;
import io.wispforest.owo.ui.hud.HudInspectorScreen;
import io.wispforest.owo.ui.parsing.ConfigureHotReloadScreen;
import io.wispforest.owo.ui.parsing.UIModelLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.event.Level;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@ApiStatus.Internal
public class OwoDebugCommands {

    private static EnumArgumentType<Level> LEVEL_ARGUMENT_TYPE;

    private static final SuggestionProvider<CommandSourceStack> POI_TYPES =
        (context, builder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.POINT_OF_INTEREST_TYPE.keySet(), builder);

    private static final SimpleCommandExceptionType NO_POI_TYPE = new SimpleCommandExceptionType(Component.nullToEmpty("Invalid POI type"));
    public static final int GENERAL_PURPLE = 0xB983FF;
    public static final int KEY_BLUE = 0x94B3FD;
    public static final int VALUE_BLUE = 0x94DAFF;

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterEvent.class, event -> {
            event.register(RegistryKeys.COMMAND_ARGUMENT_TYPE, helper -> {
                LEVEL_ARGUMENT_TYPE = EnumArgumentType.create(Level.class, "'{}' is not a valid logging level");
            });
        });

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            var dispatcher = event.getDispatcher();
            var registryAccess = event.getBuildContext();

            dispatcher.register(literal("logger").then(argument("level", LEVEL_ARGUMENT_TYPE).executes(context -> {
                final var level = LEVEL_ARGUMENT_TYPE.get(context, "level");
                LogUtils.configureRootLoggingLevel(level);

                context.getSource().sendSuccess(() -> TextOps.concat(Owo.PREFIX, Component.nullToEmpty("global logging level set to: §9" + level)), false);
                return 0;
            })));

            dispatcher.register(literal("query-poi").then(argument("poi_type", IdentifierArgument.id()).suggests(POI_TYPES)
                .then(argument("radius", IntegerArgumentType.integer()).executes(context -> {
                    var player = context.getSource().getPlayer();
                    var poiType = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getOptional(IdentifierArgument.getId(context, "poi_type"))
                        .orElseThrow(NO_POI_TYPE::create);

                    var entries = ((ServerLevel) player.level()).getPoiManager().getInRange(type -> type.value() == poiType,
                        player.blockPosition(), IntegerArgumentType.getInteger(context, "radius"), PoiManager.Occupancy.ANY).toList();

                    player.displayClientMessage(TextOps.concat(Owo.PREFIX, TextOps.withColor("Found §" + entries.size() + " §entr" + (entries.size() == 1 ? "y" : "ies"),
                        TextOps.color(ChatFormatting.GRAY), GENERAL_PURPLE, TextOps.color(ChatFormatting.GRAY))), false);

                    for (var entry : entries) {

                        final var entryPos = entry.getPos();
                        final var blockId = BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(entryPos).getBlock()).toString();
                        final var posString = "(" + entryPos.getX() + " " + entryPos.getY() + " " + entryPos.getZ() + ")";

                        final var message = TextOps.withColor("-> §" + blockId + " §" + posString,
                            TextOps.color(ChatFormatting.GRAY), KEY_BLUE, VALUE_BLUE);

                        message.withStyle(style -> style.withClickEvent(new ClickEvent.SuggestCommand(
                                "/tp " + entryPos.getX() + " " + entryPos.getY() + " " + entryPos.getZ()))
                            .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty("Click to teleport"))));

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
                    source.sendFailure(TextOps.concat(Owo.PREFIX, Component.literal("You're not looking at a block")));
                    return 1;
                }

                BlockPos pos = ((BlockHitResult) target).getBlockPos();
                final var blockEntity = player.level().getBlockEntity(pos);

                if (blockEntity == null) {
                    source.sendFailure(TextOps.concat(Owo.PREFIX, Component.literal(("No block entity"))));
                    return 1;
                }

                var blockEntityClass = blockEntity.getClass();

                try {
                    final var field = blockEntityClass.getDeclaredField(targetField);

                    if (!field.canAccess(blockEntity)) field.setAccessible(true);
                    final var value = field.get(blockEntity);

                    source.sendSuccess(() -> TextOps.concat(Owo.PREFIX, TextOps.withColor("Field value: §" + value, TextOps.color(ChatFormatting.GRAY), KEY_BLUE)), false);

                } catch (Exception e) {
                    source.sendFailure(TextOps.concat(Owo.PREFIX, Component.literal("Could not access field - " + e.getClass().getSimpleName() + ": " + e.getMessage())));
                }

                return 0;
            })));

            MakeLootContainerCommand.register(dispatcher, registryAccess);
            DumpdataCommand.register(dispatcher);
            HealCommand.register(dispatcher);

//            if (FabricLoader.getInstance().isModLoaded("cardinal-components-base")) {
//                CcaDataCommand.register(dispatcher);
//            }
        });
    }
}
