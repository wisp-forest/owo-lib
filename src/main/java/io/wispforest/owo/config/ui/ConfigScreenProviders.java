package io.wispforest.owo.config.ui;

import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ConfigScreenProviders {

    private static final Map<String, Function<Screen, ? extends Screen>> PROVIDERS = new HashMap<>();
    private static final Map<String, Function<Screen, ? extends ConfigScreen>> OWO_SCREEN_PROVIDERS = new HashMap<>();

    /**
     * Register the given config screen provider. This is primarily
     * used for making a config screen available in ModMenu and to the
     * {@code /owo-config} command, although other places my use it as well
     *
     * @param modId    The mod id for which to supply a config screen
     * @param supplier The supplier to register - this gets the parent screen
     *                 as argument
     * @throws IllegalArgumentException If a config screen provider is
     *                                  already registered for the given mod id
     */
    public static <S extends Screen> void register(String modId, Function<Screen, S> supplier) {
        if (PROVIDERS.put(modId, supplier) != null) {
            throw new IllegalArgumentException("Tried to register config screen provider for mod id " + modId + " twice");
        }
    }

    /**
     * Get the config screen provider associated with
     * the given mod id
     *
     * @return The associated config screen provider, or {@code null} if
     * none is registered
     */
    public static @Nullable Function<Screen, ? extends Screen> get(String modId) {
        return PROVIDERS.get(modId);
    }

    public static void forEach(BiConsumer<String, Function<Screen, ? extends Screen>> action) {
        PROVIDERS.forEach(action);
    }

    // -- internal methods for backwards-compat in ConfigScreen --

    @ApiStatus.Internal
    public static <S extends ConfigScreen> void registerOwoConfigScreen(String modId, Function<Screen, S> supplier) {
        register(modId, supplier);
        OWO_SCREEN_PROVIDERS.put(modId, supplier);
    }

    static @Nullable Function<Screen, ? extends ConfigScreen> getOwoProvider(String modId) {
        return OWO_SCREEN_PROVIDERS.get(modId);
    }

    static void forEachOwoProvider(BiConsumer<String, Function<Screen, ? extends ConfigScreen>> action) {
        OWO_SCREEN_PROVIDERS.forEach(action);
    }
}
