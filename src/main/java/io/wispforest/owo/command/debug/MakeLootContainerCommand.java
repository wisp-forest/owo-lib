package io.wispforest.owo.command.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.server.command.LootCommand;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MakeLootContainerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal("make-loot-container")
                .then(argument("item", ItemStackArgumentType.itemStack(registryAccess))
                        .then(argument("loot_table", RegistryEntryArgumentType.lootTable(registryAccess))
                                .executes(MakeLootContainerCommand::execute))));
    }

    // TODO: reimplement
    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
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
