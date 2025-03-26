package io.wispforest.owo.config.ui.component.struct;

import io.wispforest.owo.config.options.OptionControlSpec;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.config.ui.component.OptionValueProvider;
import io.wispforest.owo.config.ui.component.OrderedOptionContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.util.ReflectionUtils;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;

public class MapOptionContainer<K, V> extends OrderedOptionContainer<Map<K, V>, MapOptionContainer<K, V>.KeyValuePair> {

    public MapOptionContainer(UIModel uiModel, OptionControlSpec<Map<K, V>> option, boolean expanded, boolean isDetached) {
        super(uiModel, option, expanded, isDetached);
    }

    private Pair<Type, @Nullable Class<?>> keyType = null;
    private Pair<Type, @Nullable Class<?>> valueType = null;

    public Pair<Type, @Nullable Class<?>> keyType() {
        if (keyType == null) {
            this.keyType = ReflectionUtils.getTypeAndClassArgument(this.backingOption.getGenericType(), 0);
        }

        return this.keyType;
    }

    public Pair<Type, @Nullable Class<?>> valueType() {
        if (valueType == null) {
            this.valueType = ReflectionUtils.getTypeAndClassArgument(this.backingOption.getGenericType(), 1);
        }

        return this.valueType;
    }

    @Override
    protected <P extends OptionValueProvider & Component> P createProviderComponent(MapOptionContainer<K, V>.KeyValuePair listEntry) {
        var layout = StructOptionContainer.of(this.uiModel, this.backingOption, KeyValuePair.class, List.of(keyType(), valueType()), listEntry, clazz -> createDefaultValue());

        var keyProvider = layout.getProvider(this.backingOption.key().child("key"));

        if (keyProvider instanceof ConfigTextBox configTextBox) {
            configTextBox.applyPredicate(configTextBox.applyPredicate().and(string -> {
                var bl = !this.backingList.stream().anyMatch(keyValuePair -> keyValuePair.key.equals(string));

                configTextBox.tooltip(bl ? Text.empty() : Text.of("Duplicate Key detected!"));

                return bl;
            }));
        }

        return (P) layout;
    }

    @Override
    protected List<MapOptionContainer<K, V>.KeyValuePair> convertToList(Map<K, V> data) {
        var list = new ArrayList<KeyValuePair>();

        data.forEach((key, value) -> list.add(new KeyValuePair(key, value)));

        return list;
    }

    @Override
    protected Map<K, V> convertToCollection(List<MapOptionContainer<K, V>.KeyValuePair> backingList) {
        return Util.make(new LinkedHashMap<>(), map -> backingList.forEach(kvEntry -> map.putIfAbsent(kvEntry.left(), kvEntry.right())));
    }

    @Override
    public KeyValuePair createDefaultValue() {
        return new KeyValuePair(ReflectionUtils.tryInstantiation((Class<K>) keyType().second()), ReflectionUtils.tryInstantiation((Class<V>) valueType().second()));
    }

    public final class KeyValuePair implements Pair<K, V> {
        public K key;
        public V value;

        private KeyValuePair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K left() {
            return this.key;
        }

        @Override
        public V right() {
            return this.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        @Override
        public String toString() {
            return "KeyValuePair[" +
                    "key=" + key + ", " +
                    "value=" + value + ']';
        }
    }
}
