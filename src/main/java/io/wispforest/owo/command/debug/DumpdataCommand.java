package io.wispforest.owo.command.debug;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.TextFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtPathArgument.NbtPath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Text;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import java.util.regex.Pattern;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class DumpdataCommand {

    private static final int GENERAL_PURPLE = 0xB983FF;
    private static final int KEY_BLUE = 0x94B3FD;
    private static final int VALUE_BLUE = 0x94DAFF;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("dumpdata")
                .then(literal("item").executes(withRootPath(DumpdataCommand::executeItem))
                        .then(argument("nbt_path", NbtPathArgument.nbtPath()).executes(withPathArg(DumpdataCommand::executeItem))))
                .then(literal("block").executes(withRootPath(DumpdataCommand::executeBlock))
                        .then(argument("nbt_path", NbtPathArgument.nbtPath()).executes(withPathArg(DumpdataCommand::executeBlock))))
                .then(literal("entity").executes(withRootPath(DumpdataCommand::executeEntity))
                        .then(argument("nbt_path", NbtPathArgument.nbtPath()).executes(withPathArg(DumpdataCommand::executeEntity)))));
    }

    private static Command<CommandSourceStack> withRootPath(DataDumper dumper) {
        return context -> dumper.dump(context, NbtPathArgument.nbtPath().parse(new StringReader("")));
    }

    private static Command<CommandSourceStack> withPathArg(DataDumper dumper) {
        return context -> {
            final var path = NbtPathArgument.getPath(context, "nbt_path");
            return dumper.dump(context, path);
        };
    }

    private static int executeItem(CommandContext<CommandSourceStack> context, NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        final var source = context.getSource();
        final var stack = source.getPlayer().getMainHandItem();

        informationHeader(source, "Item");
        sendIdentifier(source, stack.getItem(), BuiltInRegistries.ITEM);

        if (stack.get(DataComponents.MAX_DAMAGE) != null) {
            feedback(source, TextOps.withColor("Durability: §" + stack.get(DataComponents.MAX_DAMAGE),
                    TextOps.color(TextFormatting.GRAY), KEY_BLUE));
        } else {
            feedback(source, TextOps.withFormatting("Not damageable", TextFormatting.GRAY));
        }

        if (!stack.getComponentsPatch().isEmpty()) {
            feedback(source, TextOps.withFormatting("Component changes" + formatPath(path) + ": ", TextFormatting.GRAY)
                    .append(NbtUtils.toPrettyComponent(getPath(DataComponentPatch.CODEC.encodeStart(NbtOps.INSTANCE, stack.getComponentsPatch()).getOrThrow(), path))));
        } else {
            feedback(source, TextOps.withFormatting("No component changes", TextFormatting.GRAY));
        }

        feedback(source, TextOps.withFormatting("-----------------------", TextFormatting.GRAY));

        return 0;
    }

    private static int executeEntity(CommandContext<CommandSourceStack> context, NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        final var source = context.getSource();
        final var player = source.getPlayer();

        final var target = ProjectileUtil.getEntityHitResult(
                player,
                player.getEyePosition(0),
                player.getEyePosition(0).add(player.getViewVector(0).scale(5)),
                player.getBoundingBox().expandTowards(player.getViewVector(0).scale(5)).inflate(1),
                entity -> true,
                5 * 5);

        if (target == null || target.getType() != HitResult.Type.ENTITY) {
            source.sendFailure(TextOps.concat(Owo.PREFIX, Text.literal("You're not looking at an entity")));
            return 1;
        }

        final var entity = target.getEntity();

        informationHeader(source, "Entity");
        sendIdentifier(source, entity.getType(), BuiltInRegistries.ENTITY_TYPE);

        feedback(source, TextOps.withFormatting("NBT" + formatPath(path) + ": ", TextFormatting.GRAY)
                .append(NbtUtils.toPrettyComponent(getPath(entity.writeNbt(new NbtCompound()), path))));

        feedback(source, TextOps.withFormatting("-----------------------", TextFormatting.GRAY));

        return 0;
    }

    private static int executeBlock(CommandContext<CommandSourceStack> context, NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        final var source = context.getSource();
        final var player = source.getPlayer();

        final var target = player.pick(5, 0, false);

        if (target.getType() != HitResult.Type.BLOCK) {
            source.sendFailure(TextOps.concat(Owo.PREFIX, Text.literal("You're not looking at a block")));
            return 1;
        }

        final var pos = ((BlockHitResult) target).getBlockPos();

        final var blockState = player.level().getBlockState(pos);
        final var blockStateString = blockState.toString();

        informationHeader(source, "Block");
        sendIdentifier(source, blockState.getBlock(), BuiltInRegistries.BLOCK);

        if (blockStateString.contains("[")) {
            feedback(source, TextOps.withFormatting("State properties: ", TextFormatting.GRAY));

            var stateString = blockStateString.split(Pattern.quote("["))[1];
            stateString = stateString.substring(0, stateString.length() - 1);
            var stateInfo = stateString.replaceAll("=", ": §").split(",");

            for (var property : stateInfo) {
                feedback(source, TextOps.withColor("    " + property, KEY_BLUE, VALUE_BLUE));
            }
        } else {
            feedback(source, TextOps.withFormatting("No state properties", TextFormatting.GRAY));
        }

        final var blockEntity = player.level().getBlockEntity(pos);
        if (blockEntity != null) {
            feedback(source, TextOps.withFormatting("Block Entity NBT" + formatPath(path) + ": ", TextFormatting.GRAY)
                    .append(NbtUtils.toPrettyComponent(getPath(blockEntity.toNbt(player.registryAccess()), path))));
        } else {
            feedback(source, TextOps.withFormatting("No block entity", TextFormatting.GRAY));
        }

        feedback(source, TextOps.withFormatting("-----------------------", TextFormatting.GRAY));

        return 0;
    }

    private static <T> void sendIdentifier(CommandSourceStack source, T object, Registry<T> registry) {
        final var id = registry.getId(object).toString().split(":");
        feedback(source, TextOps.withColor("Identifier: §" + id[0] + ":§" + id[1], TextOps.color(TextFormatting.GRAY), KEY_BLUE, VALUE_BLUE));
    }

    private static void informationHeader(CommandSourceStack source, String name) {
        feedback(source, TextOps.withColor("---[§ " + name + " Information §]---",
                TextOps.color(TextFormatting.GRAY), GENERAL_PURPLE, TextOps.color(TextFormatting.GRAY)));
    }

    private static void feedback(CommandSourceStack source, Text message) {
        source.sendSuccess(() -> message, false);
    }

    private static String formatPath(NbtPathArgument.NbtPath path) {
        return path.toString().isBlank() ? "" : "(" + path + ")";
    }

    private static NbtElement getPath(NbtElement nbt, NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        return path.get(nbt).iterator().next();
    }

    @FunctionalInterface
    private interface DataDumper {
        int dump(CommandContext<CommandSourceStack> context, NbtPathArgument.NbtPath path) throws CommandSyntaxException;
    }
}
