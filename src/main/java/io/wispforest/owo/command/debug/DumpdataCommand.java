package io.wispforest.owo.command.debug;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.regex.Pattern;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DumpdataCommand {

    private static final int GENERAL_PURPLE = 0xB983FF;
    private static final int KEY_BLUE = 0x94B3FD;
    private static final int VALUE_BLUE = 0x94DAFF;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("dumpdata")
                .then(literal("item").executes(withRootPath(DumpdataCommand::executeItem))
                        .then(argument("nbt_path", NbtPathArgumentType.nbtPath()).executes(withPathArg(DumpdataCommand::executeItem))))
                .then(literal("block").executes(withRootPath(DumpdataCommand::executeBlock))
                        .then(argument("nbt_path", NbtPathArgumentType.nbtPath()).executes(withPathArg(DumpdataCommand::executeBlock))))
                .then(literal("entity").executes(withRootPath(DumpdataCommand::executeEntity))
                        .then(argument("nbt_path", NbtPathArgumentType.nbtPath()).executes(withPathArg(DumpdataCommand::executeEntity)))));
    }

    private static Command<ServerCommandSource> withRootPath(DataDumper dumper) {
        return context -> dumper.dump(context, NbtPathArgumentType.nbtPath().parse(new StringReader("")));
    }

    private static Command<ServerCommandSource> withPathArg(DataDumper dumper) {
        return context -> {
            final var path = NbtPathArgumentType.getNbtPath(context, "nbt_path");
            return dumper.dump(context, path);
        };
    }

    private static int executeItem(CommandContext<ServerCommandSource> context, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
        final var source = context.getSource();
        final var stack = source.getPlayer().getMainHandStack();

        informationHeader(source, "Item");
        sendIdentifier(source, stack.getItem(), Registries.ITEM);

        if (stack.getItem().isDamageable()) {
            feedback(source, TextOps.withColor("Durability: §" + stack.getItem().getMaxDamage(),
                    TextOps.color(Formatting.GRAY), KEY_BLUE));
        } else {
            feedback(source, TextOps.withFormatting("Not damageable", Formatting.GRAY));
        }

        if (context.getSource().getPlayer().getMainHandStack().hasNbt()) {
            feedback(source, TextOps.withFormatting("NBT" + formatPath(path) + ": ", Formatting.GRAY)
                    .append(NbtHelper.toPrettyPrintedText(getPath(stack.getNbt(), path))));
        } else {
            feedback(source, TextOps.withFormatting("No NBT", Formatting.GRAY));
        }

        feedback(source, TextOps.withFormatting("-----------------------", Formatting.GRAY));

        return 0;
    }

    private static int executeEntity(CommandContext<ServerCommandSource> context, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
        final var source = context.getSource();
        final var player = source.getPlayer();

        final var target = ProjectileUtil.raycast(
                player,
                player.getCameraPosVec(0),
                player.getCameraPosVec(0).add(player.getRotationVec(0).multiply(5)),
                player.getBoundingBox().stretch(player.getRotationVec(0).multiply(5)).expand(1),
                entity -> true,
                5 * 5);

        if (target == null || target.getType() != HitResult.Type.ENTITY) {
            source.sendError(TextOps.concat(Owo.PREFIX, Text.literal("You're not looking at an entity")));
            return 1;
        }

        final var entity = target.getEntity();

        informationHeader(source, "Entity");
        sendIdentifier(source, entity.getType(), Registries.ENTITY_TYPE);

        feedback(source, TextOps.withFormatting("NBT" + formatPath(path) + ": ", Formatting.GRAY)
                .append(NbtHelper.toPrettyPrintedText(getPath(entity.writeNbt(new NbtCompound()), path))));

        feedback(source, TextOps.withFormatting("-----------------------", Formatting.GRAY));

        return 0;
    }

    private static int executeBlock(CommandContext<ServerCommandSource> context, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
        final var source = context.getSource();
        final var player = source.getPlayer();

        final var target = player.raycast(5, 0, false);

        if (target.getType() != HitResult.Type.BLOCK) {
            source.sendError(TextOps.concat(Owo.PREFIX, Text.literal("You're not looking at a block")));
            return 1;
        }

        final var pos = ((BlockHitResult) target).getBlockPos();

        final var blockState = player.getWorld().getBlockState(pos);
        final var blockStateString = blockState.toString();

        informationHeader(source, "Block");
        sendIdentifier(source, blockState.getBlock(), Registries.BLOCK);

        if (blockStateString.contains("[")) {
            feedback(source, TextOps.withFormatting("State properties: ", Formatting.GRAY));

            var stateString = blockStateString.split(Pattern.quote("["))[1];
            stateString = stateString.substring(0, stateString.length() - 1);
            var stateInfo = stateString.replaceAll("=", ": §").split(",");

            for (var property : stateInfo) {
                feedback(source, TextOps.withColor("    " + property, KEY_BLUE, VALUE_BLUE));
            }
        } else {
            feedback(source, TextOps.withFormatting("No state properties", Formatting.GRAY));
        }

        final var blockEntity = player.getWorld().getBlockEntity(pos);
        if (blockEntity != null) {
            feedback(source, TextOps.withFormatting("Block Entity NBT" + formatPath(path) + ": ", Formatting.GRAY)
                    .append(NbtHelper.toPrettyPrintedText(getPath(blockEntity.createNbt(), path))));
        } else {
            feedback(source, TextOps.withFormatting("No block entity", Formatting.GRAY));
        }

        feedback(source, TextOps.withFormatting("-----------------------", Formatting.GRAY));

        return 0;
    }

    private static <T> void sendIdentifier(ServerCommandSource source, T object, Registry<T> registry) {
        final var id = registry.getId(object).toString().split(":");
        feedback(source, TextOps.withColor("Identifier: §" + id[0] + ":§" + id[1], TextOps.color(Formatting.GRAY), KEY_BLUE, VALUE_BLUE));
    }

    private static void informationHeader(ServerCommandSource source, String name) {
        feedback(source, TextOps.withColor("---[§ " + name + " Information §]---",
                TextOps.color(Formatting.GRAY), GENERAL_PURPLE, TextOps.color(Formatting.GRAY)));
    }

    private static void feedback(ServerCommandSource source, Text message) {
        source.sendFeedback(() -> message, false);
    }

    private static String formatPath(NbtPathArgumentType.NbtPath path) {
        return path.toString().isBlank() ? "" : "(" + path + ")";
    }

    private static NbtElement getPath(NbtElement nbt, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
        return path.get(nbt).iterator().next();
    }

    @FunctionalInterface
    private interface DataDumper {
        int dump(CommandContext<ServerCommandSource> context, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException;
    }
}
