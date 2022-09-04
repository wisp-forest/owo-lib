package io.wispforest.owo.util;

import net.minecraft.tag.TagEntry;
import net.minecraft.tag.TagManagerLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Function;

/**
 * A simple utility for inserting values into Tags at runtime
 */
public class TagInjector {

    @ApiStatus.Internal
    public static final HashMap<TagLocation, Set<TagEntry>> ADDITIONS = new HashMap<>();

    /**
     * Injects the given Identifiers into the given Tag.
     * If the Identifiers don't correspond to an entry in the
     * given Registry, you <i>will</i> break the Tag.
     * If the Tag does not exist, it will be created.
     *
     * @param registry   The registry for which the injected tags should apply
     * @param tag        The tag to insert into, this could contain all kinds of values
     * @param entryMaker The function to use for creating tag entries from the given identifiers
     * @param values     The values to insert
     */
    public static void injectRaw(Registry<?> registry, Identifier tag, Function<Identifier, TagEntry> entryMaker, Collection<Identifier> values) {
        ADDITIONS.computeIfAbsent(new TagLocation(TagManagerLoader.getPath(registry.getKey()), tag), identifier -> new HashSet<>())
                .addAll(values.stream().map(entryMaker).toList());
    }

    public static void injectRaw(Registry<?> registry, Identifier tag, Function<Identifier, TagEntry> entryMaker, Identifier... values) {
        injectRaw(registry, tag, entryMaker, Arrays.asList(values));
    }

    // -------

    /**
     * Injects the given values into the given tag, obtaining
     * their identifiers from the given registry
     *
     * @param registry The registry the target tag is for
     * @param tag      The identifier of the tag to inject into
     * @param values   The values to inject
     * @param <T>      The type of the target registry
     */
    public static <T> void inject(Registry<T> registry, Identifier tag, Collection<T> values) {
        injectDirectReference(registry, tag, values.stream().map(registry::getId).toList());
    }

    @SafeVarargs
    public static <T> void inject(Registry<T> registry, Identifier tag, T... values) {
        inject(registry, tag, Arrays.asList(values));
    }

    // -------

    /**
     * Injects the given identifiers into the given tag
     *
     * @param registry The registry the target tag is for
     * @param tag      The identifier of the tag to inject into
     * @param values   The values to inject
     */
    public static void injectDirectReference(Registry<?> registry, Identifier tag, Collection<Identifier> values) {
        injectRaw(registry, tag, TagEntry::create, values);
    }

    public static void injectDirectReference(Registry<?> registry, Identifier tag, Identifier... values) {
        injectDirectReference(registry, tag, Arrays.asList(values));
    }

    // -------

    /**
     * Injects the given tags into the given tag,
     * effectively nesting them. This is equivalent to
     * prefixing an entry in the tag JSON's {@code values} array
     * with a {@code #}
     *
     * @param registry The registry the target tag is for
     * @param tag      The identifier of the tag to inject into
     * @param values   The values to inject
     */
    public static void injectTagReference(Registry<?> registry, Identifier tag, Collection<Identifier> values) {
        injectRaw(registry, tag, TagEntry::createTag, values);
    }

    public static void injectTagReference(Registry<?> registry, Identifier tag, Identifier... values) {
        injectTagReference(registry, tag, Arrays.asList(values));
    }

    public record TagLocation(String type, Identifier tagId) {}

}
