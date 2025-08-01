package io.wispforest.owo.packets.s2c;

import blue.endless.jankson.JsonObject;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import io.wispforest.owo.network.ClientAccess;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

import java.util.Map;

public record OpenServerConfig(Identifier configId, JsonObject configData){

    public static final StructEndec<OpenServerConfig> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.IDENTIFIER.fieldOf("configId", OpenServerConfig::configId),
            MinecraftEndecs.JANK_JSON_OBJECT.fieldOf("configData", OpenServerConfig::configData),
            OpenServerConfig::new);

    @Environment(EnvType.CLIENT)
    public static void handle(OpenServerConfig packet, ClientAccess access) {
        var parent = access.runtime().currentScreen;

        if (parent instanceof ConfigScreen configScreen) {
            parent = configScreen.parent;
        }

        if (!ConfigScreenProviders.safelyOpenConfigScreen(packet.configId(), parent, Map.of(packet.configId(), packet.configData()))) {
            Owo.LOGGER.warn("Unable to open the given config screen: {}", packet.configId());
        }
    }
}
