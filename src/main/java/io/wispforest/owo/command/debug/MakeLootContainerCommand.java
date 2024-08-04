package io.wispforest.owo.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class MakeLootContainerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(literal("make-loot-container")
                .then(argument("item", ItemArgument.item(registryAccess))
                        .then(argument("loot_table", IdentifierArgument.id())
                                .suggests(LootCommand.SUGGEST_LOOT_TABLE)
                                .executes(MakeLootContainerCommand::execute))));
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var targetStack = ItemArgument.getItem(context, "item").createItemStack(1, false);
        var tableId = IdentifierArgument.getId(context, "loot_table");

        var blockEntityTag = targetStack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
        blockEntityTag = blockEntityTag.update(x -> {
            x.putString("LootTable", tableId.toString());
        });
        targetStack.set(DataComponents.BLOCK_ENTITY_DATA, blockEntityTag);

        context.getSource().getPlayer().getInventory().placeItemBackInInventory(targetStack);

        return 0;
    }
}
