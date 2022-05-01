package io.wispforest.owo.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.server.command.LootCommand;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MakeLootContainerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("make-loot-container")
                .then(argument("item", ItemStackArgumentType.itemStack())
                        .then(argument("loot_table", IdentifierArgumentType.identifier())
                                .suggests(LootCommand.SUGGESTION_PROVIDER)
                                .executes(MakeLootContainerCommand::execute))));
    }

    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var targetStack = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false);
        var tableId = IdentifierArgumentType.getIdentifier(context, "loot_table");

        targetStack.getOrCreateSubNbt("BlockEntityTag").putString("LootTable", tableId.toString());
        context.getSource().getPlayer().getInventory().offerOrDrop(targetStack);

        return 0;
    }
}
