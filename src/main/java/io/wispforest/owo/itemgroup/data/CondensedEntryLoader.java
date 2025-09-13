package io.wispforest.owo.itemgroup.data;

import com.mojang.datafixers.util.Either;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.itemgroup.base.ItemStacksSupplier;
import io.wispforest.owo.itemgroup.core.CondensedEntries;
import io.wispforest.owo.itemgroup.core.CondensedEntry;
import io.wispforest.owo.serialization.CodecUtils;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CondensedEntryLoader extends EndecDataLoader<Map<Identifier, Map<Integer, List<CondensedEntry>>>> {

    public static final CondensedEntryLoader INSTANCE = new CondensedEntryLoader();

    public static final Identifier ID = Identifier.of("owo", "condensed_entry_loader");

    protected CondensedEntryLoader() {
        super(ENTRIES_ENDEC, ResourceFinder.json("owo/condensed_entries"));
    }

    @Override
    protected void apply(Map<Identifier, Map<Identifier, Map<Integer, List<CondensedEntry>>>> data, ResourceManager manager, Profiler profiler) {
        CondensedEntries.registerDataEntries(data);
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    private static final Endec<List<CondensedEntry>> CONDENSED_ENTRIES_ENDEC = Endec.<Identifier, RawEntry>map(Identifier::toString, Identifier::of, RawEntry.ENTRY_ENDEC)
        .xmap(
            map -> map.entrySet().stream().map(entry -> new CondensedEntry(entry.getKey(), entry.getValue().childrenEntries(), entry.getValue().useItemMatching())).toList(),
            list -> list.stream().collect(Collectors.toMap(CondensedEntry::id, entry -> new RawEntry(entry.childrenEntries(), entry.useItemMatching())))
        );

    private static final Endec<Map<Identifier, Map<Integer, List<CondensedEntry>>>> ENTRIES_ENDEC =
        map(Identifier::toString, Identifier::of,
            CodecUtils.eitherEndec(map(Object::toString, Integer::valueOf, CONDENSED_ENTRIES_ENDEC), CONDENSED_ENTRIES_ENDEC)
                .xmap(
                    either -> Either.unwrap(either.mapRight(entries -> Util.make(new LinkedHashMap<>(), map -> map.put(0, entries)))),
                    Either::left
                )
        );

    private static <K, V> Endec<Map<K, V>> map(Function<K, String> keyToString, Function<String, K> stringToKey, Endec<V> valueEndec) {
        return Endec.of((ctx, serializer, map) -> {
            try (var mapState = serializer.map(ctx, valueEndec, map.size())) {
                map.forEach((k, v) -> mapState.entry(keyToString.apply(k), v));
            }
        }, (ctx, deserializer) -> {
            var mapState = deserializer.map(ctx, valueEndec);

            Map<K, V> map = new LinkedHashMap<>(mapState.estimatedSize());
            mapState.forEachRemaining(entry -> map.put(stringToKey.apply(entry.getKey()), entry.getValue()));

            return map;
        });
    }

    private record RawEntry(ItemStacksSupplier childrenEntries, boolean useItemMatching) {
        public static final StructEndec<RawEntry> ENTRY_ENDEC = StructEndecBuilder.of(
            ItemStacksSupplier.ENDEC.fieldOf("stacks", RawEntry::childrenEntries),
            Endec.BOOLEAN.optionalFieldOf("use_item_matching", RawEntry::useItemMatching, false),
            RawEntry::new
        );
    }
}
