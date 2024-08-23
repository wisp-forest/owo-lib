package io.wispforest.owo.ext;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;

public interface OwoItem {
    default void deriveStackComponents(ComponentMap source, ComponentChanges.Builder target) {

    }
}
