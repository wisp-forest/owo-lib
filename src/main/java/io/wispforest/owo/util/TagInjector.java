package io.wispforest.owo.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;

/**
 * A simple utility for inserting values into Tags at runtime
 */
public class TagInjector {

    public static final HashMap<Identifier, Set<Identifier>> ADDITIIONS = new HashMap<>();

    /**
     * Injects the given Identifiers into the given Tag.
     * If the Identifiers don't correspond to an object in the
     * relevant Registry, you <i>will</i> break the Tag.
     * If the Tag does not exist, it will be created.
     *
     * @param tag    The tag to insert into, this could contain all kinds of values
     * @param values The values to insert
     */
    public static void injectRaw(Identifier tag, Collection<Identifier> values) {
        ADDITIIONS.computeIfAbsent(tag, identifier -> new HashSet<>()).addAll(values);
    }

    public static void injectRaw(Identifier tag, Identifier... values) {
        injectRaw(tag, Arrays.asList(values));
    }

    /**
     * A convenience method for directly inserting Blocks into the given Tag.
     * If any of Blocks are not registered yet, this <i>will</i> throw an
     * exception.
     */
    public static void injectBlocks(Identifier tag, Collection<Block> values) {
        injectRaw(tag, values.stream().map(Registry.BLOCK::getId).toList());
    }

    public static void injectBlocks(Identifier tag, Block... values) {
        injectRaw(tag, Arrays.stream(values).map(Registry.BLOCK::getId).toList());
    }

    /**
     * A convenience method for directly inserting Items into the given Tag.
     * If any of Items are not registered yet, this <i>will</i> throw an
     * exception.
     */
    public static void injectItems(Identifier tag, Collection<Item> values) {
        injectRaw(tag, values.stream().map(Registry.ITEM::getId).toList());
    }

    public static void injectItems(Identifier tag, Item... values) {
        injectRaw(tag, Arrays.stream(values).map(Registry.ITEM::getId).toList());
    }

}
