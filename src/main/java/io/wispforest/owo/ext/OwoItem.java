package io.wispforest.owo.ext;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import org.jetbrains.annotations.ApiStatus;

public interface OwoItem {
    /**
     * Generates component-derived-components from the stack's components
     * @param source a map containing the item stack's non-derived components
     * @param target a builder for the derived component map
     */
    @ApiStatus.Experimental
    default void deriveStackComponents(ComponentMap source, ComponentChanges.Builder target) { }
}
