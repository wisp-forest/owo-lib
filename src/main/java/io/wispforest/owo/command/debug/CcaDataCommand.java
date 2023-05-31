package io.wispforest.owo.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CcaDataCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("cca-data").executes(CcaDataCommand::executeDumpAll)
                .then(argument("path", NbtPathArgumentType.nbtPath()).executes(CcaDataCommand::executeDumpPath)));
    }

    private static int executeDumpAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var player = context.getSource().getPlayer();
        final var nbt = player.writeNbt(new NbtCompound()).getCompound("cardinal_components");

        context.getSource().sendFeedback(() -> TextOps.concat(Owo.PREFIX, TextOps.withFormatting("CCA Data:", Formatting.GRAY)), false);
        context.getSource().sendFeedback(() -> NbtHelper.toPrettyPrintedText(nbt), false);

        return 0;
    }

    private static int executeDumpPath(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var player = context.getSource().getPlayer();
        final var path = NbtPathArgumentType.getNbtPath(context, "path");
        final var nbt = path.get(player.writeNbt(new NbtCompound())
                .getCompound("cardinal_components")).iterator().next();

        context.getSource().sendFeedback(() -> TextOps.concat(Owo.PREFIX, TextOps.withFormatting("CCA Data:", Formatting.GRAY)), false);
        context.getSource().sendFeedback(() -> NbtHelper.toPrettyPrintedText(nbt), false);

        return 0;
    }

}
