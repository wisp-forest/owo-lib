package io.wispforest.owo.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DamageCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("damage")
                .then(argument("entity", EntityArgumentType.entity())
                        .then(argument("damage", FloatArgumentType.floatArg(0))
                                .executes(DamageCommand::executeDamageVoid))
                        .then(argument("source", DamageSourceArgumentType.damageSource())
                                .then(argument("damage", FloatArgumentType.floatArg(0)).executes(DamageCommand::executeDamageSource)))));
    }

    private static int executeDamageSource(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var source = DamageSourceArgumentType.getDamageSource(context, "source");
        return executeDamage(context, source);
    }

    private static int executeDamageVoid(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return executeDamage(context, DamageSource.OUT_OF_WORLD);
    }

    private static int executeDamage(CommandContext<ServerCommandSource> context, DamageSource source) throws CommandSyntaxException {
        final var entity = EntityArgumentType.getEntity(context, "entity");
        final float amount = FloatArgumentType.getFloat(context, "damage");

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
