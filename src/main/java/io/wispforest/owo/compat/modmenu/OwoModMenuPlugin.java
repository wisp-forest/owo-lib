package io.wispforest.owo.compat.modmenu;

import com.google.common.collect.ForwardingMap;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import io.wispforest.owo.config.ui.SimpleButtonScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public class OwoModMenuPlugin implements ModMenuApi {

    private static final Map<String, ConfigScreenFactory<?>> OWO_FACTORIES = new ForwardingMap<>() {
        @Override
        protected @NotNull Map<String, ConfigScreenFactory<?>> delegate() {
            return Util.make(
                    new HashMap<>(),
                    factoryMap -> {
                        ConfigScreenProviders.getSortedProviders().forEach((modId, modSpecificProviders) -> {
                            var configId = Identifier.of(modId, modSpecificProviders.getFirst());

                            factoryMap.put(modId, parent -> ConfigScreenProviders.safelyCreateConfigScreen(configId, parent, Map.of()));
                        });
                    }
            );
        }
    };

    @Override
    @Nullable
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new SimpleButtonScreen(
            Text.of("The Confurration Screen"),
            Text.of("This screen exists due to Mod Menu issue that has not been resolved so are you a... a furry?"),
            Map.of(
                Text.of("Totally a Furry"),
                (btn) -> MinecraftClient.getInstance().currentScreen.close(),
                Text.of("Not a Furry"),
                (btn) -> MinecraftClient.getInstance().currentScreen.close()
            )
        );
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return OWO_FACTORIES;
    }
}
