package io.wispforest.owo.util;

import com.google.common.collect.ForwardingMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagEntry;

/**
 * A simple utility for inserting values into Tags at runtime
 */
public final class TagInjector {

    @ApiStatus.Internal
    public static final HashMap<TagLocation, Set<TagEntry>> ADDITIONS = new HashMap<>();

    private static final Map<TagLocation, Set<TagEntry>> ADDITIONS_VIEW = new ForwardingMap<>() {
        @Override
        protected @NotNull Map<TagLocation, Set<TagEntry>> delegate() {
            return Collections.unmodifiableMap(ADDITIONS);
        }

        @Override
        public Set<TagEntry> get(@Nullable Object key) {
            return Collections.unmodifiableSet(this.delegate().get(key));
        }
    };

    private TagInjector() {}

    /**
     * @return A view of all planned tag injections
     */
    public static Map<TagLocation, Set<TagEntry>> getInjections() {
        return ADDITIONS_VIEW;
    }

    /**
     * Inject the given identifiers into the given tag
     * <p>
     * If any of the identifiers don't correspond to an entry in the
     * given registry, you <i>will</i> break the tag.
     * If the tag does not exist, it will be created.
     *
     * @param registry   The registry for which the injected tags should apply
     * @param tag        The tag to insert into, this could contain all kinds of values
     * @param entryMaker The function to use for creating tag entries from the given identifiers
     * @param values     The values to insert
     */
    public static void injectRaw(Registry<?> registry, Identifier tag, Function<Identifier, TagEntry> entryMaker, Collection<Identifier> values) {
        ADDITIONS.computeIfAbsent(new TagLocation(Registries.tagsDirPath(registry.key()), tag),identifier -> new HashSet<>())
                .addAll(values.stream().map(entryMaker).toList());
    }

    public static void injectRaw(Registry<?> registry, Identifier tag, Function<Identifier, TagEntry> entryMaker, Identifier... values) {
        injectRaw(registry, tag, entryMaker, Arrays.asList(values));
    }

    // -------

    /**
     * Inject the given values into the given tag, obtaining
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
     * Inject the given identifiers into the given tag
     *
     * @param registry The registry the target tag is for
     * @param tag      The identifier of the tag to inject into
     * @param values   The values to inject
     */
    public static void injectDirectReference(Registry<?> registry, Identifier tag, Collection<Identifier> values) {
        injectRaw(registry, tag, TagEntry::element, values);
    }

    public static void injectDirectReference(Registry<?> registry, Identifier tag, Identifier... values) {
        injectDirectReference(registry, tag, Arrays.asList(values));
    }

    // -------

    /**
     * Inject the given tags into the given tag,
     * effectively nesting them. This is equivalent to
     * prefixing an entry in the tag JSON's {@code values} array
     * with a {@code #}
     *
     * @param registry The registry the target tag is for
     * @param tag      The identifier of the tag to inject into
     * @param values   The values to inject
     */
    public static void injectTagReference(Registry<?> registry, Identifier tag, Collection<Identifier> values) {
        injectRaw(registry, tag, TagEntry::tag, values);
    }

    public static void injectTagReference(Registry<?> registry, Identifier tag, Identifier... values) {
        injectTagReference(registry, tag, Arrays.asList(values));
    }

    public record TagLocation(String type, Identifier tagId) {}

}
