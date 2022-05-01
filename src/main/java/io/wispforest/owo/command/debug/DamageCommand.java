package io.wispforest.owo.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.command.ServerCommandSource;

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

        entity.damage(source, amount);

        return (int) Math.floor(amount);
    }

}
