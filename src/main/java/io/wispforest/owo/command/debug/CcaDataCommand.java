package io.wispforest.owo.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class CcaDataCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("cca-data").executes(CcaDataCommand::executeDumpAll)
                .then(argument("path", NbtPathArgument.nbtPath()).executes(CcaDataCommand::executeDumpPath)));
    }

    private static int executeDumpAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var player = context.getSource().getPlayer();
        final var writeView = TagValueOutput.createWithoutContext(new ProblemReporter.ScopedCollector(Owo.LOGGER));
        player.save(writeView);

        final var nbt = writeView.buildResult().getCompound("cardinal_components").orElseGet(CompoundTag::new);

        context.getSource().sendSuccess(() -> TextOps.concat(Owo.PREFIX, TextOps.withFormatting("CCA Data:", ChatFormatting.GRAY)), false);
        context.getSource().sendSuccess(() -> NbtUtils.toPrettyComponent(nbt), false);

        return 0;
    }

    private static int executeDumpPath(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var player = context.getSource().getPlayer();
        final var path = NbtPathArgument.getPath(context, "path");

        final var writeView = TagValueOutput.createWithoutContext(new ProblemReporter.ScopedCollector(Owo.LOGGER));
        player.save(writeView);

        final var nbt = path.get(writeView.buildResult().getCompound("cardinal_components").orElseGet(CompoundTag::new)).iterator().next();

        context.getSource().sendSuccess(() -> TextOps.concat(Owo.PREFIX, TextOps.withFormatting("CCA Data:", ChatFormatting.GRAY)), false);
        context.getSource().sendSuccess(() -> NbtUtils.toPrettyComponent(nbt), false);

        return 0;
    }

}
