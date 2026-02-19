package io.wispforest.owo.ext;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import org.jetbrains.annotations.ApiStatus;

public interface OwoItem {
    /**
     * Generates component-derived-components from the stack's components
     * @param source a map containing the item stack's non-derived components
     * @param target a builder for the derived component map
     */
    @ApiStatus.Experimental
    default void deriveStackComponents(DataComponentMap source, DataComponentPatch.Builder target) { }
}
