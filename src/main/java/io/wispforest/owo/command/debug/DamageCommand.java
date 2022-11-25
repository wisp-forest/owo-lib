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
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DamageCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("damage")
                .then(argument("source", DamageSourceArgumentType.damageSource())
                        .then(argument("damage", FloatArgumentType.floatArg(0))
                                .executes(DamageCommand::executeSourcedSelfDamage)))
                .then(argument("damage", FloatArgumentType.floatArg(0))
                        .executes(DamageCommand::executeSelfDamage))
                .then(argument("entity", EntityArgumentType.entity())
                        .then(argument("damage", FloatArgumentType.floatArg(0))
                                .executes(DamageCommand::executeTargetedDamage))
                        .then(argument("source", DamageSourceArgumentType.damageSource())
                                .then(argument("damage", FloatArgumentType.floatArg(0))
                                        .executes(DamageCommand::executeTargetedSourcedDamage)))));
    }

    private static int executeSelfDamage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return executeDamage(
                context,
                DamageSource.OUT_OF_WORLD,
                context.getSource().getEntityOrThrow(),
                FloatArgumentType.getFloat(context, "damage")
        );
    }

    private static int executeSourcedSelfDamage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return executeDamage(
                context,
                DamageSourceArgumentType.getDamageSource(context, "source"),
                context.getSource().getEntityOrThrow(),
                FloatArgumentType.getFloat(context, "damage")
        );
    }

    private static int executeTargetedDamage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return executeDamage(
                context,
                DamageSource.OUT_OF_WORLD,
                EntityArgumentType.getEntity(context, "entity"),
                FloatArgumentType.getFloat(context, "damage")
        );
    }

    private static int executeTargetedSourcedDamage(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return executeDamage(
                context,
                DamageSourceArgumentType.getDamageSource(context, "source"),
                EntityArgumentType.getEntity(context, "entity"),
                FloatArgumentType.getFloat(context, "damage")
        );
    }

    private static int executeDamage(CommandContext<ServerCommandSource> context, DamageSource source, Entity entity, float amount) throws CommandSyntaxException {
        if (entity instanceof LivingEntity living) {
            float damage = living.getHealth();
            living.damage(source, amount);
            damage -= living.getHealth();

            context.getSource().sendFeedback(TextOps.concat(Owo.PREFIX, TextOps.withColor("dealt §" + damage + " §damage",
                    TextOps.color(Formatting.GRAY), OwoDebugCommands.GENERAL_PURPLE, TextOps.color(Formatting.GRAY))), false);
        } else {
            entity.damage(source, amount);
            context.getSource().sendFeedback(TextOps.concat(Owo.PREFIX, TextOps.withFormatting("dealt unknown damage", Formatting.GRAY)), false);
        }
        return (int) Math.floor(amount);
    }
}
