package io.wispforest.owo.config.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.Strictness;
import io.wispforest.endec.format.gson.GsonSerializer;
import io.wispforest.owo.Owo;
import io.wispforest.owo.config.base.Key;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.owo.text.TextLanguage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Utility class for creating translation keys for a given config on screen creation.
 * <br><br>
 * Secondary ability is to create a language file dump for your config to easily copy and paste
 * the given config entries for translation.
 */
public class ConfigTranslationHelper  {
    public static final String BASE_SECTION = "_base_section_name";

    private static final Deque<TranslationsStorage> storages = new ArrayDeque<>();

    private ConfigTranslationHelper() {}

    //--

    @Nullable
    private static Key trackingOptionKey = Key.ROOT;

    static void pushConfigId(Identifier configId) {
        storages.push(new TranslationsStorage(configId));
    }

    private static TranslationsStorage peekStorage() {
        var storage = storages.peek();

        Objects.requireNonNull(storage, "Unable to peek current translation storage as it was found to be null");

        return storage;
    }

    static TranslationsStorage popConfigId() {
        return storages.pop();
    }

    @Nullable
    public static Identifier getConfigId() {
        var activeStorage = storages.peek();

        if (activeStorage == null) return null;

        return activeStorage.configId;
    }

    public static Identifier getConfigIdOrThrow() {
        var id = getConfigId();

        Objects.requireNonNull(id, "Unable to get the required Identifier for the current Config when build translation Key!");

        return id;
    }

    //--

    public static String createSectionTranslation(Key parentKey, String section) {
        return createSectionTranslation(parentKey, section, false);
    }

    public static String createSectionTranslation(Key parentKey, String section, boolean isTooltip) {
        var translation = createTranslation(getConfigIdOrThrow(), "section." + section, isTooltip);

        peekStorage().parentKeyToSection.computeIfAbsent(parentKey, key -> new LinkedHashSet<>(List.of(BASE_SECTION))).addLast(section);

        if (!parentKey.isRoot()) {
            var parentParentKey = parentKey.parent();

            peekStorage().sectionToOptionKeys.computeIfAbsent(parentParentKey, key -> new LinkedHashMap<>())
                .computeIfAbsent(getTrackingSectionValue(parentParentKey), s -> new LinkedHashSet<>())
                .add(parentKey);
        }

        return translation;
    }

    private static String getTrackingSectionValue(Key parentKey) {
        return peekStorage().parentKeyToSection.computeIfAbsent(parentKey, key -> new LinkedHashSet<>(List.of(BASE_SECTION))).getLast();
    }

    //--

    public static String createOptionTranslation(Identifier configId, Key key) {
        return createOptionTranslation(configId, key, false);
    }

    public static String createOptionTranslation(Identifier configId, Key key, boolean isTooltip) {
        var currentId = getConfigId();

        var translation = createTranslation(currentId != null ? currentId : configId, "option." + key.asString(), isTooltip);

        var parentKey = key.parent();

        peekStorage().sectionToOptionKeys
            .computeIfAbsent(parentKey, key1 -> new LinkedHashMap<>())
            .computeIfAbsent(getTrackingSectionValue(parentKey), s -> new LinkedHashSet<>())
            .add(key);
        trackingOptionKey = key;

        return translation;
    }

    public static void popOptionKey() {
        trackingOptionKey = Key.ROOT;
    }

    //--

    public static String createConfigCategoryTranslation(Key parentKey) {
        return createConfigCategoryTranslation(parentKey, false);
    }

    public static String createConfigCategoryTranslation(Key parentKey, boolean isTooltip) {
        return createTranslation(getConfigIdOrThrow(), "category." + parentKey.asString(), isTooltip);
    }

    public static String createEnumTranslation(Key key, Enum<?>[] values, int selectedIndex){
        var enumName = StringUtils.uncapitalize(values.getClass().componentType().getSimpleName());
        var valueName = values[selectedIndex].name().toLowerCase(Locale.ROOT);

        peekStorage().optionKeyToTranslations.computeIfAbsent(Key.ROOT, key1 -> new LinkedHashSet<>())
            .addAll(
                Arrays.stream(values)
                    .map(entry -> "enum." + enumName + "." + entry.name().toLowerCase(Locale.ROOT))
                    .map(s -> createTranslation(getConfigId(), s, false))
                    .toList()
            );

        var optionValueKey = createOptionTranslation(getConfigIdOrThrow(), key) + ".value." + valueName;

        if (I18n.hasTranslation(optionValueKey)) return optionValueKey;

        return createTranslation(getConfigIdOrThrow(), "enum." + enumName + "." + valueName, false);
    }

    //--

    public static String createConfigTranslation(TranslationPostfix postfix) {
        var prefix = createConfigPrefix(getConfigIdOrThrow(), false) + ".";

        var translation = prefix + postfix.createPostFix();

        peekStorage().optionKeyToTranslations.computeIfAbsent(trackingOptionKey, key -> new LinkedHashSet<>())
            .addAll(postfix.allVariations().stream().map(s -> prefix + s).toList());

        return translation;
    }

    public interface TranslationPostfix {
        String createPostFix();

        // TODO: UM YEA IDK THIS NEEDS TO BE THOUGHT ABOUT
        default List<String> allVariations() {
            return List.of(createPostFix());
        }
    }

    //--

    private static boolean shouldAllowAlternativeTranslation() {
        return true;
    }

    public static String createConfigTitleTranslation(Identifier configId) {
        var translation = "text.config." + configId.toTranslationKey() + ".title";

        if (!I18n.hasTranslation(translation) && shouldAllowAlternativeTranslation()) {
            translation = "text.config." + configId.getPath() + ".title";
        }

        return translation;
    }

    private static String createTranslation(Identifier configId, String postfix, boolean isTooltip) {
        String translation = createConfigPrefix(configId, false) + postfix;

        if (shouldAllowAlternativeTranslation() && !I18n.hasTranslation(translation)) {
            translation = createConfigPrefix(configId, true) + postfix;
        }

        return translation + (isTooltip ? ".tooltip" : "");
    }

    private static String createConfigPrefix(Identifier configId, boolean legacyKey) {
        return "text.config." + (legacyKey ? configId.getPath() : configId.toTranslationKey()) + ".";
    }

    private static final Gson GSON = new GsonBuilder()
        .setStrictness(Strictness.LENIENT)
        .setPrettyPrinting()
        .create();

    public static void dumpData(TranslationsStorage storage, String languageKey, Consumer<String> consumer) {
        var json = new JsonObject();

        json.addProperty(createConfigTitleTranslation(storage.configId), "");

        var language = TranslationStorage.load(MinecraftClient.getInstance().getResourceManager(), List.of(languageKey), false);

        addTranslationFromKey(s -> {
            if (language instanceof TextLanguage textLanguage) {
                var text = textLanguage.getText(s);

                if (text != null) {
                    json.add(s, MinecraftEndecs.TEXT.encodeFully(GsonSerializer::of, text));

                    return;
                }
            }

            json.addProperty(s, language.hasTranslation(s) ? language.get(s) : "");
        }, storage, Key.ROOT);

        for (String translation : storage.optionKeyToTranslations.getOrDefault(Key.ROOT, List.of())) {
            json.addProperty(translation, "");
        }

        consumer.accept(GSON.toJson(json));
    }

    private static void addTranslationFromKey(Consumer<String> addCallback, TranslationsStorage storage, Key parentKey) {
        var currentId = storage.configId;
        for (String section : storage.parentKeyToSection.getOrDefault(parentKey, List.of(BASE_SECTION))) {
            if (!section.equals(BASE_SECTION)) {
                addCallback.accept(createTranslation(currentId, "section." + section, false));
                addCallback.accept(createTranslation(currentId, "section." + section, false));
            }

            for (Key optionKey : storage.sectionToOptionKeys.getOrDefault(parentKey, Map.of()).getOrDefault(section, List.of())) {
                if (!optionKey.equals(Key.ROOT)) {
                    addCallback.accept(createTranslation(currentId, "option." + optionKey.asString(), false));
                    addCallback.accept(createTranslation(currentId, "option." + optionKey.asString(), true));
                }

                for (String translation : storage.optionKeyToTranslations.getOrDefault(optionKey, List.of())) {
                    addCallback.accept(translation);
                }

                addTranslationFromKey(addCallback, storage, optionKey);
            }
        }
    }

    public static class TranslationsStorage {
        final Map<Key, SequencedCollection<String>> parentKeyToSection = new LinkedHashMap<>();

        final Map<Key, Map<String, SequencedCollection<Key>>> sectionToOptionKeys = new LinkedHashMap<>();

        final Map<Key, SequencedCollection<String>> optionKeyToTranslations = new LinkedHashMap<>();

        final Identifier configId;

        TranslationsStorage(Identifier configId){
            this.configId = configId;
        }
    }
}
