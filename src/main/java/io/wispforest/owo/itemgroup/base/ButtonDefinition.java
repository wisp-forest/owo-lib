package io.wispforest.owo.itemgroup.base;

import io.wispforest.owo.itemgroup.gui.ItemGroupButtonWidget;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Defines a button's appearance and translation key
 * <p>
 * Used by {@link ItemGroupButtonWidget}
 */
public interface ButtonDefinition {

    String name();

    Icon icon();

    Identifier texture();

    Text tooltip();

    static Text tooltipFor(RegistryKey<ItemGroup> group, String component, String componentName) {
        var registryId = group.getValue();
        var groupId = registryId.getNamespace().equals("minecraft")
            ? registryId.getPath()
            : registryId.getNamespace() + "." + registryId.getPath();

        return Text.translatable("itemGroup." + groupId + "." + component + "." + componentName);
    }

}
