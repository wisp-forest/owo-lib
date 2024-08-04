package io.wispforest.owo.itemgroup;

import java.util.function.BiConsumer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public interface OwoItemSettingsExtension {

    default Item.Properties group(ItemGroupReference ref) {
        throw new IllegalStateException("Implemented in mixin.");
    }

    /**
     * @param group The item group this item should appear in
     */
    default Item.Properties group(OwoItemGroup group) {
        throw new IllegalStateException("Implemented in mixin.");
    }

    default OwoItemGroup group() {
        throw new IllegalStateException("Implemented in mixin.");
    }

    default Item.Properties tab(int tab) {
        throw new IllegalStateException("Implemented in mixin.");
    }

    default int tab() {
        throw new IllegalStateException("Implemented in mixin.");
    }

    /**
     * @param generator The function this item uses for creating stacks in the
     *                  {@link OwoItemGroup} it is in, by default this will be {@link OwoItemGroup#DEFAULT_STACK_GENERATOR}
     */
    default Item.Properties stackGenerator(BiConsumer<Item, CreativeModeTab.Output> generator) {
        throw new IllegalStateException("Implemented in mixin.");
    }

    default BiConsumer<Item, CreativeModeTab.Output> stackGenerator() {
        throw new IllegalStateException("Implemented in mixin.");
    }

    /**
     * Automatically increment {@link net.minecraft.stats.Stats#ITEM_USED}
     * for this item every time {@link Item#use(Level, Player, InteractionHand)}
     * returns an accepted result
     */
    default Item.Properties trackUsageStat() {
        throw new IllegalStateException("Implemented in mixin.");
    }

    default boolean shouldTrackUsageStat() {
        throw new IllegalStateException("Implemented in mixin.");
    }
}
