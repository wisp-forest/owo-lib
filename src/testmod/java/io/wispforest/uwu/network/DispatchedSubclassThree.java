package io.wispforest.uwu.network;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public record DispatchedSubclassThree(Item item, Block block) implements DispatchedInterface {
    @Override
    public String getName() {
        return "three";
    }
}
