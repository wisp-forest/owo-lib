package com.glisco.owo.command;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.command.LootCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.ApiStatus;

import java.util.regex.Pattern;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@ApiStatus.Internal
public class OwoDebugCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {

            dispatcher.register(literal("dumpdata")
                    .then(literal("item").executes(context -> {

                        if (!context.getSource().getPlayer().getMainHandStack().hasNbt()) {
                            context.getSource().sendError(Text.of("This item has no tag"));
                        } else {
                            Text message = NbtHelper.toPrettyPrintedText(context.getSource().getPlayer().getMainHandStack().getNbt());
                            context.getSource().getPlayer().sendMessage(message, false);
                        }
                        return 0;
                    })).then(literal("block").executes(context -> {

                        final ServerCommandSource source = context.getSource();
                        final ServerPlayerEntity player = source.getPlayer();
                        HitResult target = player.raycast(5, 0, false);

                        if (target.getType() != HitResult.Type.BLOCK) {
                            source.sendError(Text.of("You're not looking at a block"));
                            return 1;
                        }

                        BlockPos pos = ((BlockHitResult) target).getBlockPos();

                        String blockState = player.getWorld().getBlockState(pos).toString();
                        String blockId = blockState.split(Pattern.quote("["))[0];
                        blockId = blockId.substring(6, blockId.length() - 1);

                        if (blockState.contains("[")) {
                            String stateInfo = "[" + blockState.split(Pattern.quote("["))[1];
                            source.sendFeedback(Text.of("Block ID: §b" + blockId), false);
                            source.sendFeedback(Text.of("BlockState: §b" + stateInfo), false);
                        } else {
                            source.sendFeedback(Text.of("Block ID: §b" + blockId), false);
                        }

                        if (player.getWorld().getBlockEntity(pos) != null) {
                            source.sendFeedback(new LiteralText("Tag: ").append(NbtHelper.toPrettyPrintedText(player.getWorld().getBlockEntity(pos).createNbt())), false);
                        }

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
        });
    }
}
