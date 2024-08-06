package io.wispforest.owo.ops;

import io.wispforest.owo.mixin.SetComponentsFunctionAccessor;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A simple utility class to make injecting simple items or
 * ItemStacks into one or multiple LootTables a one-line operation
 */
public final class LootOps {

    private LootOps() {}

    private static final Map<Identifier[], Supplier<LootPoolEntryContainer>> ADDITIONS = new HashMap<>();

    /**
     * Injects a single item entry into the specified LootTable(s)
     *
     * @param item         The item to inject
     * @param chance       The chance for the item to actually generate
     * @param targetTables The LootTable(s) to inject into
     */
    public static void injectItem(ItemLike item, float chance, Identifier... targetTables) {
        ADDITIONS.put(targetTables, () -> LootItem.lootTableItem(item).when(LootItemRandomChanceCondition.randomChance(chance)).build());
    }

    /**
     * Injects an item entry into the specified LootTable(s),
     * with a random count between {@code min} and {@code max}
     *
     * @param item         The item to inject
     * @param chance       The chance for the item to actually generate
     * @param min          The minimum amount of items to generate
     * @param max          The maximum amount of items to generate
     * @param targetTables The LootTable(s) to inject into
     */
    public static void injectItemWithCount(ItemLike item, float chance, int min, int max, Identifier... targetTables) {
        ADDITIONS.put(targetTables, () -> LootItem.lootTableItem(item)
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))
                .build());
    }

    /**
     * Injects a single ItemStack entry into the specified LootTable(s)
     *
     * @param stack        The ItemStack to inject
     * @param chance       The chance for the ItemStack to actually generate
     * @param targetTables The LootTable(s) to inject into
     */
    @SuppressWarnings("deprecation")
    public static void injectItemStack(ItemStack stack, float chance, Identifier... targetTables) {
        ADDITIONS.put(targetTables, () -> LootItem.lootTableItem(stack.getItem())
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .apply(() -> SetComponentsFunctionAccessor.createSetComponentsFunction(List.of(), stack.getComponentsPatch()))
                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(stack.getCount())))
                .build());
    }

    /**
     * Test is {@code target} matches against any of the {@code predicates}.
     * Used to easily target multiple LootTables
     *
     * @param target     The target identifier (this would be the current table)
     * @param predicates The identifiers to test against (this would be the targeted tables)
     * @return {@code true} if target matches any of the predicates
     */
    public static boolean anyMatch(Identifier target, Identifier... predicates) {
        for (var predicate : predicates) if (target.equals(predicate)) return true;
        return false;
    }

    @ApiStatus.Internal
    public static void registerListener() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source) -> {
            ADDITIONS.forEach((identifiers, lootPoolEntrySupplier) -> {
                if (anyMatch(key.value(), identifiers)) tableBuilder.withPool(LootPool.lootPool().with(lootPoolEntrySupplier.get()));
            });
        });
    }

}
