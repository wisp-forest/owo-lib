package io.wispforest.owo.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.wispforest.owo.config.ConfigWrapper;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public class OwoModMenuPlugin implements ModMenuApi {

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return Util.make(
                new HashMap<>(),
                map -> ConfigWrapper.forEachScreenProvider((s, provider) -> map.put(s, provider::apply))
        );
    }
}
