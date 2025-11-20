package io.wispforest.owo.itemgroup.core;

import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;

///
/// A key object representing a specific tab for a given [ItemGroup]
///
public record ItemGroupReference(RegistryKey<ItemGroup> group, int tab) {}
