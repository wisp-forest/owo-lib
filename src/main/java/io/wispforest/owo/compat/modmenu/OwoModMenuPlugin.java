package io.wispforest.owo.compat.modmenu;

import com.google.common.collect.ForwardingMap;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.wispforest.owo.config.ui.ConfigScreen;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.Util;

@ApiStatus.Internal
public class OwoModMenuPlugin implements ModMenuApi {

    private static final Map<String, ConfigScreenFactory<?>> OWO_FACTORIES = new ForwardingMap<>() {
        @Override
        protected @NotNull Map<String, ConfigScreenFactory<?>> delegate() {
            return Util.make(
                    new HashMap<>(),
                    map -> ConfigScreen.forEachProvider((s, provider) -> map.put(s, provider::apply))
            );
        }
    };

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return OWO_FACTORIES;
    }
}
