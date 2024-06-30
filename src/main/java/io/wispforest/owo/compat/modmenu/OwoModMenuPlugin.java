package io.wispforest.owo.compat.modmenu;

import com.google.common.collect.ForwardingMap;
import io.wispforest.owo.config.ui.ConfigScreen;
import net.minecraft.util.Util;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public class OwoModMenuPlugin /*implements ModMenuApi*/ {

    private static final Map<String, IConfigScreenFactory> OWO_FACTORIES = new ForwardingMap<>() {
        @Override
        protected @NotNull Map<String, IConfigScreenFactory> delegate() {
            return Util.make(
                    new HashMap<>(),
                    map -> ConfigScreen.forEachProvider((s, provider) -> map.put(s, (arg, arg2) -> provider.apply(arg2)))
            );
        }
    };

    public static Map<String, IConfigScreenFactory> getProvidedConfigScreenFactories() {
        return OWO_FACTORIES;
    }
}
