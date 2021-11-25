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

    public static final String BLOCK_TAG = "blocks";
    public static final String ITEM_TAG = "items";
    public static final String ENTITY_TAG = "entity_types";
    public static final String FUNCTION_TAG = "functions";
    public static final String GAME_EVENT_TAG = "game_events";
    public static final String FLUID_TAG = "fluids";

    public static final HashMap<TagLocation, Set<Identifier>> ADDITIIONS = new HashMap<>();

    /**
     * Injects the given Identifiers into the given Tag.
     * If the Identifiers don't correspond to an object in the
     * relevant Registry, you <i>will</i> break the Tag.
     * If the Tag does not exist, it will be created.
     *
     * @param tagType The type of tag to inject. This essentially corresponds to the tag file path,
     *                so {@code fluids} would be {@code data/<namespace>/tags/fluids}
     * @param tag     The tag to insert into, this could contain all kinds of values
     * @param values  The values to insert
     */
    public static void injectRaw(String tagType, Identifier tag, Collection<Identifier> values) {
        ADDITIIONS.computeIfAbsent(new TagLocation(tagType, tag), identifier -> new HashSet<>()).addAll(values);
    }

    public static void injectRaw(String tagType, Identifier tag, Identifier... values) {
        injectRaw(tagType, tag, Arrays.asList(values));
    }

    /**
     * A convenience method for directly inserting Blocks into the given Tag.
     * If any of the Blocks are not registered yet, this <i>will</i> throw an
     * exception.
     */
    public static void injectBlocks(Identifier tag, Collection<Block> values) {
        injectRaw(BLOCK_TAG, tag, values.stream().map(Registry.BLOCK::getId).toList());
    }

    public static void injectBlocks(Identifier tag, Block... values) {
        injectRaw(BLOCK_TAG, tag, Arrays.stream(values).map(Registry.BLOCK::getId).toList());
    }

    /**
     * A convenience method for directly inserting Items into the given Tag.
     * If any of the Items are not registered yet, this <i>will</i> throw an
     * exception.
     */
    public static void injectItems(Identifier tag, Collection<Item> values) {
        injectRaw(ITEM_TAG, tag, values.stream().map(Registry.ITEM::getId).toList());
    }

    public static void injectItems(Identifier tag, Item... values) {
        injectRaw(ITEM_TAG, tag, Arrays.stream(values).map(Registry.ITEM::getId).toList());
    }

    public static record TagLocation(String type, Identifier tagId) {
        public TagLocation(String type, Identifier tagId) {
            this.type = "tags/" + type;
            this.tagId = tagId;
        }
    }

}
