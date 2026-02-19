package io.wispforest.owo.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class HealCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("heal")
                .executes(HealCommand::executeFullHeal)
                .then(argument("amount", FloatArgumentType.floatArg(0))
                        .executes(HealCommand::executeSelfHeal))
                .then(argument("entity", EntityArgument.entity())
                        .executes(HealCommand::executeTargetedFullHeal)
                        .then(argument("amount", FloatArgumentType.floatArg(0))
                                .executes(HealCommand::executeTargetedHeal))));
    }

    private static int executeFullHeal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var target = context.getSource().getEntityOrException();
        return executeHeal(
                context,
                target,
                target instanceof LivingEntity living ? living.getMaxHealth() : Float.MAX_VALUE
        );
    }

    private static int executeSelfHeal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return executeHeal(
                context,
                context.getSource().getEntityOrException(),
                FloatArgumentType.getFloat(context, "amount")
        );
    }

    private static int executeTargetedFullHeal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var target = EntityArgument.getEntity(context, "entity");
        return executeHeal(
                context,
                target,
                target instanceof LivingEntity living ? living.getMaxHealth() : Float.MAX_VALUE
        );
    }

    private static int executeTargetedHeal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return executeHeal(
                context,
                EntityArgument.getEntity(context, "entity"),
                FloatArgumentType.getFloat(context, "amount")
        );
    }

    private static int executeHeal(CommandContext<CommandSourceStack> context, Entity entity, float amount) throws CommandSyntaxException {
        if (entity instanceof LivingEntity living) {
            float healed = living.getHealth();
            living.heal(amount);
            healed = living.getHealth() - healed;

            float thankYouMojang = healed;
            context.getSource().sendSuccess(() -> TextOps.concat(Owo.PREFIX, TextOps.withColor("healed §" + thankYouMojang + " §hp",
                    TextOps.color(ChatFormatting.GRAY), OwoDebugCommands.GENERAL_PURPLE, TextOps.color(ChatFormatting.GRAY))), false);
        } else {
            context.getSource().sendFailure(TextOps.concat(Owo.PREFIX, Component.nullToEmpty("Cannot heal non living entity")));
        }

        return (int) Math.floor(amount);
    }

}

//chyz was here