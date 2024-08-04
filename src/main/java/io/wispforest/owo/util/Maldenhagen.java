package io.wispforest.owo.util;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.level.block.Block;

/**
 * A simple utility class for making ore blocks update after they are generated.
 * This is especially useful for ores that are supposed to glow, as with the normal
 * ore feature they won't do that since lighting is never calculated for them
 */
public final class Maldenhagen {

    private Maldenhagen() {}

    private static final Set<Block> COPIUM_INJECTED = new HashSet<>();

    /**
     * Marks a block for update after generation
     *
     * @param block The block to update
     */
    public static void injectCopium(Block block) {
        COPIUM_INJECTED.add(block);
    }

    /**
     * @param block The block to test
     * @return {@code true} if the block should update after generation
     */
    public static boolean isOnCopium(Block block) {
        return COPIUM_INJECTED.contains(block);
    }

}
