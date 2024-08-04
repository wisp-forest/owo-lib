package io.wispforest.owo.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.TextFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtPathArgument.NbtPath;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class CcaDataCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("cca-data").executes(CcaDataCommand::executeDumpAll)
                .then(argument("path", NbtPathArgument.nbtPath()).executes(CcaDataCommand::executeDumpPath)));
    }

    private static int executeDumpAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var player = context.getSource().getPlayer();
        final var nbt = player.writeNbt(new NbtCompound()).getCompound("cardinal_components");

        context.getSource().sendSuccess(() -> TextOps.concat(Owo.PREFIX, TextOps.withFormatting("CCA Data:", TextFormatting.GRAY)), false);
        context.getSource().sendSuccess(() -> NbtUtils.toPrettyComponent(nbt), false);

        return 0;
    }

    private static int executeDumpPath(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var player = context.getSource().getPlayer();
        final var path = NbtPathArgument.getPath(context, "path");
        final var nbt = path.get(player.writeNbt(new NbtCompound())
                .getCompound("cardinal_components")).iterator().next();

        context.getSource().sendSuccess(() -> TextOps.concat(Owo.PREFIX, TextOps.withFormatting("CCA Data:", TextFormatting.GRAY)), false);
        context.getSource().sendSuccess(() -> NbtUtils.toPrettyComponent(nbt), false);

        return 0;
    }

}
