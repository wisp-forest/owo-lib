package io.wispforest.owo.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ops.TextOps;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class HealCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("heal")
                .then(argument("entity", EntityArgumentType.entity())
                        .then(argument("amount", FloatArgumentType.floatArg(0)).executes(HealCommand::execute))));
    }

    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final var entity = EntityArgumentType.getEntity(context, "entity");
        final float amount = FloatArgumentType.getFloat(context, "amount");

        if (entity instanceof LivingEntity living) {
            living.heal(amount);
        } else {
            context.getSource().sendError(TextOps.concat(Owo.PREFIX, Text.of("Cannot heal non living entity")));
        }

        return (int) Math.floor(amount);
    }

}
