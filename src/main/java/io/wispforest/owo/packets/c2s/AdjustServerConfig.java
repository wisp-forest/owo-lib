package io.wispforest.owo.packets.c2s;

import blue.endless.jankson.JsonObject;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.network.ServerAccess;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record AdjustServerConfig(Identifier configId, JsonObject configData, boolean restartRequired) {

    public static void handle(AdjustServerConfig packet, ServerAccess access) {
        if(!access.player().hasPermissionLevel(3)) {
            access.player().sendMessage(Text.of("Unable to adjust config as player is missing proper Admin Perms (Level 3)."));

            return;
        }

        var wrapper = ConfigWrapper.getKnownConfigInstances().get(packet.configId());

        if (wrapper == null) {
            access.player().sendMessage(Text.of("Unable to adjust config as such dose not exist on the server! [Id: " + packet.configId() + "]"));

            return;
        }

        wrapper.load(packet.configData, true);

        wrapper.saveToFile();

        if (packet.restartRequired()) {
            access.player().sendMessage(Text.of("Config has been saved but a restart is required to have the given changes take effect."));
        }
    }
}
