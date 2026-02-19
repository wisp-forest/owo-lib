package io.wispforest.owo.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.item.ItemArgument;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class MakeLootContainerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(literal("make-loot-container")
                .then(argument("item", ItemArgument.item(registryAccess))
                        .then(argument("loot_table", ResourceOrIdArgument.lootTable(registryAccess))
                                .executes(MakeLootContainerCommand::execute))));
    }

    // TODO: reimplement
    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
//        var targetStack = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false);
//        var tableId = RegistryEntryArgumentType.getLootTable(context, "loot_table");
//
//        var blockEntityTag = targetStack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
//        if (blockEntityTag == null) {
//            blockEntityTag = TypedEntityData.create()
//        }
//
//        blockEntityTag = blockEntityTag.apply(x -> {
//            x.putString("LootTable", tableId.getIdAsString());
//        });
//        targetStack.set(DataComponentTypes.BLOCK_ENTITY_DATA, blockEntityTag);
//
//        context.getSource().getPlayer().getInventory().offerOrDrop(targetStack);

        return 0;
    }
}
