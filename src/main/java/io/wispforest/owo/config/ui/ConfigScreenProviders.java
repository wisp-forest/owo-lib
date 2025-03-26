package io.wispforest.owo.config.ui;

import blue.endless.jankson.JsonObject;
import com.terraformersmc.modmenu.gui.ModsScreen;
import io.wispforest.owo.Owo;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.packets.OwoPackets;
import io.wispforest.owo.packets.c2s.AdjustServerConfig;
import io.wispforest.owo.ui.util.UIErrorToast;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class ConfigScreenProviders {

    public static final Identifier NONE = Identifier.of("owo", "none");

    private static boolean rebuildSortedProviders = false;

    private static final Map<Identifier, ScreenProviderData> PROVIDERS = new LinkedHashMap<>();

    /**
     * Register the given config screen provider. This is primarily
     * used for making a config screen available in ModMenu and to the
     * {@code /owo-config} command, although other places my use it as well
     *
     * @param configId The mod id for which to supply a config screen
     * @param supplier The supplier to register - this gets the parent screen
     *                 as argument
     * @throws IllegalArgumentException If a config screen provider is
     *                                  already registered for the given mod id
     */
    public static <S extends Screen, W extends ConfigWrapper<?>> void register(Identifier configId, Class<W> wrapperClass, BiFunction<@Nullable Screen, W, S> supplier) {
        register(configId, 0, wrapperClass, supplier);
    }

    public static <S extends Screen, W extends ConfigWrapper<?>> void register(Identifier configId, int order, Class<W> wrapperClass, BiFunction<@Nullable Screen, W, S> supplier) {
        if (PROVIDERS.containsKey(configId)) {
            throw new IllegalArgumentException("Tried to register config screen provider for mod id " + configId.toString() + " twice");
        }

        PROVIDERS.put(configId, new ScreenProviderData(configId, ConfigScreenProvider.of(wrapperClass, supplier), order));

        if (!rebuildSortedProviders) rebuildSortedProviders = true;
    }

    /**
     * Get the config screen provider associated with
     * the given mod id
     *
     * @return The associated config screen provider, or {@code null} if
     * none is registered
     */
    public static @Nullable ConfigScreenProvider<? extends ConfigWrapper<?>> get(Identifier configId) {
        return PROVIDERS.get(configId).provider();
    }

    public static void forEach(BiConsumer<Identifier, ConfigScreenProvider<? extends ConfigWrapper<?>>> action) {
        PROVIDERS.forEach((identifier, data) -> action.accept(identifier, data.provider()));
    }

    private static final Map<String, SequencedSet<String>> SORTED_PROVIDER_CACHE = new HashMap<>();

    public static Map<String, SequencedSet<String>> getSortedProviders() {
        if (rebuildSortedProviders) {
            SORTED_PROVIDER_CACHE.clear();

            var tempMap = new HashMap<String, List<ScreenProviderData>>();

            for (var entry : PROVIDERS.entrySet()) {
                var configId = entry.getKey();
                var data = entry.getValue();

                tempMap.computeIfAbsent(configId.getNamespace(), string -> new ArrayList<>())
                        .add(data);
            }

            var baseMap = new HashMap<String, SequencedSet<String>>();

            for (var entry : tempMap.entrySet()) {
                /*
                 * 1. Sort by natural ordering of Strings
                 * 2. Sort by Order Number
                 * 3. Sort by primary configs
                 */
                var sortedSet = entry.getValue().stream()
                        .sorted(Comparator.comparing(data -> data.configId().getPath(), CharSequence::compare))
                        .sorted(Comparator.comparingInt(ScreenProviderData::order))
                        .map(data -> data.configId().getPath())
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                baseMap.put(entry.getKey(), sortedSet);
            }

            SORTED_PROVIDER_CACHE.putAll(baseMap);
        }

        return SORTED_PROVIDER_CACHE;
    }

    public static Identifier getPrimaryModProvider(String modid) {
        var modProviders = getSortedProviders().get(modid);

        if (modProviders == null || modProviders.isEmpty()) return NONE;

        return Identifier.of(modid, modProviders.getFirst());
    }

    public static boolean safelyOpenConfigScreen(Identifier configId, @Nullable Screen parent, ConfigScreen prevConfigScreen) {
        var result = safelyOpenConfigScreen(configId, parent, prevConfigScreen.serverConfigData);

        if (result && MinecraftClient.getInstance().currentScreen instanceof ConfigScreen configScreen) {
            configScreen.setConfigScreenData(prevConfigScreen);
        }

        return result;
    }

    public static boolean safelyOpenConfigScreen(String modid, @Nullable Screen parent, Map<Identifier, JsonObject> configData) {
        return safelyOpenConfigScreen(getPrimaryModProvider(modid), parent, configData);
    }

    public static boolean safelyOpenConfigScreen(Identifier configId, @Nullable Screen parent, Map<Identifier, JsonObject> configData) {
        var screen = safelyCreateConfigScreen(configId, parent, configData);

        if (screen == null) return false;

        MinecraftClient.getInstance().setScreen(screen);

        return true;
    }

    @Nullable
    public static Screen safelyCreateConfigScreen(Identifier configId, @Nullable Screen parent, Map<Identifier, JsonObject> configData) {
        try {
            var data = configData != null && MinecraftClient.getInstance().getServer() == null
                    ? configData.get(configId)
                    : null;

            var wrapper = ConfigWrapper.getOrDuplicateWrapper(configId, data);

            var providerData = PROVIDERS.get(configId);

            if (providerData == null) return null;

            Screen screen = providerData.provider().openScreenSafely(parent, wrapper);

            if (screen instanceof ConfigScreen configScreen) {
                if (providerData != null) {
                    configScreen.addRemovedHook((config, restartRequired) -> {
                        OwoPackets.MAIN.clientHandle().send(new AdjustServerConfig(config.id(), config.saveToObject(), restartRequired));
                    });
                }

                configScreen.setServerConfigData(configData);
            } else if (providerData != null){
                ScreenEvents.remove(screen).register(screen1 -> {
                    OwoPackets.MAIN.clientHandle().send(new AdjustServerConfig(wrapper.id(), wrapper.saveToObject(), false));
                });
            }

            return screen;
        } catch (java.lang.NoClassDefFoundError e) {
            Owo.LOGGER.warn("The '{}' mod config screen is not available because {} is missing.", configId, e.getLocalizedMessage());
            handleError(parent, configId, e);
        } catch (Throwable e) {
            Owo.LOGGER.error("Error from mod '{}'", configId, e);
            handleError(parent, configId, e);
        }

        return null;
    }

    private static void handleError(Screen startingScreen, Identifier configId, Throwable e) {
        if(!FabricLoader.getInstance().isModLoaded("modmenu") || !handleModScreenError(startingScreen, configId, e)) {
            //Owo.LOGGER.warn("Could not set owo config screen [" + modId + ":" + configName + "]", e);
            UIErrorToast.report(e);
        }

        MinecraftClient.getInstance().setScreen(startingScreen);
    }

    private static boolean handleModScreenError(Screen startingScreen, Identifier configId, Throwable e) {
        if(startingScreen instanceof ModsScreen screen) {
            screen.modScreenErrors.put(configId.getNamespace(), e);

            return true;
        }

        return false;
    }

    private record ScreenProviderData(Identifier configId, ConfigScreenProvider<? extends ConfigWrapper<?>> provider, int order){}
}
