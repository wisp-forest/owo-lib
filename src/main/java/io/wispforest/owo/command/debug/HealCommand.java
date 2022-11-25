package io.wispforest.owo.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class HealCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("heal")
                .executes(context -> execute(context, context.getSource().getPlayerOrThrow(), context.getSource().getPlayerOrThrow().getMaxHealth()))
                .then(argument("amount", FloatArgumentType.floatArg(0))
                        .executes(context -> execute(context, context.getSource().getPlayerOrThrow(), FloatArgumentType.getFloat(context, "amount"))))
                .then(argument("entity", EntityArgumentType.entity())
                        .executes(context -> execute(context, EntityArgumentType.getEntity(context, "entity"), 1))
                        .then(argument("amount", FloatArgumentType.floatArg(0))
                                .executes(context -> execute(context, EntityArgumentType.getEntity(context, "entity"), FloatArgumentType.getFloat(context, "amount"))))));
    }

    private static int execute(CommandContext<ServerCommandSource> context, Entity entity, float amount) throws CommandSyntaxException {
        if (entity instanceof LivingEntity living) {
            float healed = living.getHealth();
            living.heal(amount);
            healed = living.getHealth() - healed;

            context.getSource().sendFeedback(TextOps.concat(Owo.PREFIX, TextOps.withColor("healed §" + healed + " §hp",
                    TextOps.color(Formatting.GRAY), OwoDebugCommands.GENERAL_PURPLE, TextOps.color(Formatting.GRAY))), false);
        } else {
            context.getSource().sendError(TextOps.concat(Owo.PREFIX, Text.of("Cannot heal non living entity")));
        }

        return (int) Math.floor(amount);
    }

}
