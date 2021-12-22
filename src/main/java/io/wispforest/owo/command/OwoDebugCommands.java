package io.wispforest.owo.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ops.TextOps;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.command.LootCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.spi.StandardLevel;
import org.jetbrains.annotations.ApiStatus;

import java.util.regex.Pattern;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@ApiStatus.Internal
public class OwoDebugCommands {

    private static final EnumArgumentType<StandardLevel> LEVEL_ARGUMENT_TYPE =
            EnumArgumentType.create(StandardLevel.class, "'{}' is not a valid logging level");

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {

            dispatcher.register(literal("dumpdata")
                    .then(literal("item").executes(context -> {

                        final var source = context.getSource();
                        final var stack = source.getPlayer().getMainHandStack();

                        var itemId = Registry.ITEM.getId(stack.getItem()).toString();

                        source.sendFeedback(TextOps.withColor("---[§ Item Information §]---",
                                TextOps.color(Formatting.GRAY), 0xB983FF, TextOps.color(Formatting.GRAY)), false);

                        source.sendFeedback(TextOps.withColor("Identifier: §" + itemId.split(":")[0] + ":§" + itemId.split(":")[1],
                                TextOps.color(Formatting.GRAY), 0x94B3FD, 0x94DAFF), false);

                        if (stack.getItem().isDamageable()) {
                            source.sendFeedback(TextOps.withColor("Durability: §" + stack.getItem().getMaxDamage(),
                                    TextOps.color(Formatting.GRAY), 0x94B3FD), false);
                        } else {
                            source.sendFeedback(TextOps.withColor("Not damageable", TextOps.color(Formatting.GRAY)), false);
                        }

                        if (context.getSource().getPlayer().getMainHandStack().hasNbt()) {
                            source.sendFeedback(TextOps.withColor("Tag: ", TextOps.color(Formatting.GRAY))
                                    .append(NbtHelper.toPrettyPrintedText(stack.getNbt())), false);
                        } else {
                            source.sendFeedback(TextOps.withColor("No tag", TextOps.color(Formatting.GRAY)), false);
                        }

                        source.sendFeedback(Text.of("§7----------------------"), false);

                        return 0;
                    })).then(literal("block").executes(context -> {

                        final ServerCommandSource source = context.getSource();
                        final ServerPlayerEntity player = source.getPlayer();
                        HitResult target = player.raycast(5, 0, false);

                        if (target.getType() != HitResult.Type.BLOCK) {
                            source.sendError(TextOps.concat(Owo.PREFIX, new LiteralText("You're not looking at a block")));
                            return 1;
                        }

                        BlockPos pos = ((BlockHitResult) target).getBlockPos();

                        String blockState = player.getWorld().getBlockState(pos).toString();
                        String blockId = blockState.split(Pattern.quote("["))[0];
                        blockId = blockId.substring(6, blockId.length() - 1);

                        source.sendFeedback(TextOps.withColor("---[§ Block Information §]---",
                                TextOps.color(Formatting.GRAY), 0xB983FF, TextOps.color(Formatting.GRAY)), false);

                        source.sendFeedback(TextOps.withColor("Identifier: §" + blockId.split(":")[0] + ":§" + blockId.split(":")[1],
                                TextOps.color(Formatting.GRAY), 0x94B3FD, 0x94DAFF), false);

                        if (blockState.contains("[")) {
                            var stateString = blockState.split(Pattern.quote("["))[1];
                            stateString = stateString.substring(0, stateString.length() - 1);

                            var stateInfo = stateString.replaceAll("=", ": §").split(",");

                            source.sendFeedback(TextOps.withColor("State properties: ", TextOps.color(Formatting.GRAY)), false);

                            for (var property : stateInfo) {
                                source.sendFeedback(TextOps.withColor("    " + property, 0x94B3FD, 0x94DAFF), false);
                            }
                        } else {
                            source.sendFeedback(TextOps.withColor("No state properties", TextOps.color(Formatting.GRAY)), false);
                        }

                        final var blockEntity = player.getWorld().getBlockEntity(pos);
                        if (blockEntity != null) {
                            source.sendFeedback(TextOps.withColor("Tag: ", TextOps.color(Formatting.GRAY))
                                    .append(NbtHelper.toPrettyPrintedText(blockEntity.createNbt())), false);
                        } else {
                            source.sendFeedback(TextOps.withColor("No block entity", TextOps.color(Formatting.GRAY)), false);
                        }

                        source.sendFeedback(Text.of("§7-----------------------"), false);

                        return 0;
                    })));

            dispatcher.register(literal("give_loot_container")
                    .then(argument("item", ItemStackArgumentType.itemStack())
                            .then(argument("loot_table", IdentifierArgumentType.identifier()).suggests(LootCommand.SUGGESTION_PROVIDER).executes(context -> {
                                var targetStack = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false);
                                var table_id = IdentifierArgumentType.getIdentifier(context, "loot_table");

                                targetStack.getOrCreateSubNbt("BlockEntityTag").putString("LootTable", table_id.toString());

                                context.getSource().getPlayer().getInventory().offerOrDrop(targetStack);

                                return 0;
                            }))));

            dispatcher.register(literal("logger").then(argument("level", LEVEL_ARGUMENT_TYPE).executes(context -> {
                final var level = LEVEL_ARGUMENT_TYPE.get(context, "level");

                var ctx = (LoggerContext) LogManager.getContext(false);
                ctx.getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME).setLevel(Level.getLevel(level.name()));
                ctx.updateLoggers();

                context.getSource().sendFeedback(TextOps.concat(Owo.PREFIX, new LiteralText("Global logging level set to: §9" + level)), false);
                return 0;
            })));

            dispatcher.register(literal("dumpfield").then(argument("field_name", StringArgumentType.string()).executes(context -> {
                final var targetField = StringArgumentType.getString(context, "field_name");
                final ServerCommandSource source = context.getSource();
                final ServerPlayerEntity player = source.getPlayer();
                HitResult target = player.raycast(5, 0, false);

                if (target.getType() != HitResult.Type.BLOCK) {
                    source.sendError(TextOps.concat(Owo.PREFIX, new LiteralText("You're not looking at a block")));
                    return 1;
                }

                BlockPos pos = ((BlockHitResult) target).getBlockPos();
                final var blockEntity = player.getWorld().getBlockEntity(pos);

                if (blockEntity == null) {
                    source.sendError(TextOps.concat(Owo.PREFIX, new LiteralText("No block entity")));
                    return 1;
                }

                var blockEntityClass = blockEntity.getClass();

                try {
                    final var field = blockEntityClass.getDeclaredField(targetField);

                    if (!field.canAccess(blockEntity)) field.setAccessible(true);
                    final var value = field.get(blockEntity);

                    source.sendFeedback(TextOps.concat(Owo.PREFIX, TextOps.withColor("Field value: §" + value, TextOps.color(Formatting.GRAY), 0x94B3FD)), false);

                } catch (Exception e) {
                    source.sendError(TextOps.concat(Owo.PREFIX, new LiteralText("Could not access field - " + e.getClass().getSimpleName() + ": " + e.getMessage())));
                }

                return 0;
            })));
        });

    }
}
