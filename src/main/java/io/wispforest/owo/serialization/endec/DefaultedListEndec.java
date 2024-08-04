package io.wispforest.owo.serialization.endec;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.NonNullList;

public final class DefaultedListEndec {

    private DefaultedListEndec() {}

    public static <T> Endec<NonNullList<T>> forSize(Endec<T> elementEndec, T defaultValue, int size) {
        return forSize(elementEndec, defaultValue, element -> Objects.equals(defaultValue, element), size);
    }

    public static <T> Endec<NonNullList<T>> forSize(Endec<T> elementEndec, T defaultValue, Predicate<T> skipWhen, int size) {
        var entryEndec = StructEndecBuilder.of(
                elementEndec.fieldOf("element", s -> s.element),
                Endec.VAR_INT.fieldOf("idx", s -> s.idx),
                Entry::new
        );

        return entryEndec.listOf().xmap(
                entries -> {
                    var list = NonNullList.withSize(size, defaultValue);
                    entries.forEach(entry -> list.set(entry.idx, entry.element));
                    return list;
                }, elements -> {
                    var entries = new ArrayList<Entry<T>>();
                    for (int i = 0; i < elements.size(); i++) {
                        if (skipWhen.test(elements.get(i))) continue;
                        entries.add(new Entry<>(elements.get(i), i));
                    }
                    return entries;
                }
        );
    }

    private record Entry<T>(T element, int idx) {}
}