package io.wispforest.owo.itemgroup.core;

import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;

public record ItemGroupReference(RegistryKey<ItemGroup> group, int tab) {}
