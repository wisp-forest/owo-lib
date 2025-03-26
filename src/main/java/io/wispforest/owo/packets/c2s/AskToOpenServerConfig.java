package io.wispforest.owo.packets.c2s;

import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.network.ServerAccess;
import io.wispforest.owo.packets.OwoPackets;
import io.wispforest.owo.packets.s2c.OpenServerConfig;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record AskToOpenServerConfig(Identifier configId) {

    public static void handle(AskToOpenServerConfig packet, ServerAccess access) {
        if(!access.player().hasPermissionLevel(3)) {
            access.player().sendMessage(Text.of("Unable to open config as player is missing proper Admin Perms (Level 3)."));

            return;
        }

        var wrapper = ConfigWrapper.getConfig(packet.configId());

        if (wrapper == null) {
            access.player().sendMessage(Text.of("Unable to open config as such dose not exist on the server! [Id: " + packet.configId() + "]"));

            return;
        }

        OwoPackets.MAIN.serverHandle(access.player()).send(new OpenServerConfig(packet.configId(), wrapper.saveToObject()));
    }
}
