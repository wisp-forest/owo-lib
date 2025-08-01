package io.wispforest.owo.packets.c2s;

import blue.endless.jankson.JsonObject;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.format.jankson.JanksonEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.network.ServerAccess;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record AdjustServerConfig(Identifier configId, JsonObject configData, boolean shouldRestart, boolean shouldReload) {

    public static final StructEndec<AdjustServerConfig> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.IDENTIFIER.fieldOf("config_id", AdjustServerConfig::configId),
            JanksonEndec.INSTANCE.xmap(jsonElement -> (JsonObject) jsonElement, jsonObject -> jsonObject).fieldOf("config_data", AdjustServerConfig::configData),
            Endec.BOOLEAN.fieldOf("should_restart", AdjustServerConfig::shouldRestart),
            Endec.BOOLEAN.fieldOf("should_reload", AdjustServerConfig::shouldReload),
            AdjustServerConfig::new
    );

    public static void handle(AdjustServerConfig packet, ServerAccess access) {
        if(!access.player().hasPermissionLevel(3)) {
            access.player().sendMessage(Text.translatable("text.owo.config.server_config.invalid_permissions"));

            return;
        }

        var wrapper = ConfigWrapper.getKnownConfigInstances().get(packet.configId());

        if (wrapper == null) {
            access.player().sendMessage(Text.translatable("text.owo.config.server_config.invalid_config", packet.configId.toString()));

            return;
        }

        wrapper.load(packet.configData, true);

        wrapper.saveToFile();

        if (packet.shouldRestart()) {
            access.player().sendMessage(Text.translatable("text.owo.config.server_config.restart_recommendation"));
        } else if(packet.shouldReload()) {
            access.player().sendMessage(Text.translatable("text.owo.config.server_config.reload_recommendation"));
        }
    }
}
