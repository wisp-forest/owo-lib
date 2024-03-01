package io.wispforest.owo.itemgroup;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.function.BiConsumer;

public interface OwoItemSettings {

    default Item.Settings group(ItemGroupReference ref) {
        throw new IllegalStateException("Implemented in mixin.");
    }

    /**
     * @param group The item group this item should appear in
     */
    default Item.Settings group(OwoItemGroup group) {
        throw new IllegalStateException("Implemented in mixin.");
    }

    default OwoItemGroup group() {
        throw new IllegalStateException("Implemented in mixin.");
    }

    default Item.Settings tab(int tab) {
        throw new IllegalStateException("Implemented in mixin.");
    }

    default int tab() {
        throw new IllegalStateException("Implemented in mixin.");
    }

    /**
     * @param generator The function this item uses for creating stacks in the
     *                  {@link OwoItemGroup} it is in, by default this will be {@link OwoItemGroup#DEFAULT_STACK_GENERATOR}
     */
    default Item.Settings stackGenerator(BiConsumer<Item, ItemGroup.Entries> generator) {
        throw new IllegalStateException("Implemented in mixin.");
    }

    default BiConsumer<Item, ItemGroup.Entries> stackGenerator() {
        throw new IllegalStateException("Implemented in mixin.");
    }

    /**
     * Automatically increment {@link net.minecraft.stat.Stats#USED}
     * for this item every time {@link Item#use(World, PlayerEntity, Hand)}
     * returns an accepted result
     */
    default Item.Settings trackUsageStat() {
        throw new IllegalStateException("Implemented in mixin.");
    }

    default boolean shouldTrackUsageStat() {
        throw new IllegalStateException("Implemented in mixin.");
    }
}
