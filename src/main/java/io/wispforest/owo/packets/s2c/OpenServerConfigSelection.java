package io.wispforest.owo.packets.s2c;

import blue.endless.jankson.JsonObject;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.Owo;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import io.wispforest.owo.network.ClientAccess;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

import java.util.Map;

public record OpenServerConfigSelection(String modId, Map<Identifier, JsonObject> modConfigData) {

    public static final StructEndec<OpenServerConfigSelection> ENDEC = StructEndecBuilder.of(
            Endec.STRING.fieldOf("modId", OpenServerConfigSelection::modId),
            Endec.map(Identifier::toString, Identifier::tryParse, MinecraftEndecs.JANK_JSON_OBJECT).fieldOf("modConfigData", OpenServerConfigSelection::modConfigData),
            OpenServerConfigSelection::new
    );

    @Environment(EnvType.CLIENT)
    public static void handle(OpenServerConfigSelection packet, ClientAccess access) {
        if (ConfigScreenProviders.safelyOpenConfigScreen(packet.modId(), access.runtime().currentScreen, packet.modConfigData())) {
            Owo.LOGGER.warn("Unable to open the selection of configs for the given modid: {}", packet.modId());
        }
    }
}
