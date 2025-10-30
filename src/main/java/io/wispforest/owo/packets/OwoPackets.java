package io.wispforest.owo.packets;

import io.wispforest.owo.config.ConfigSynchronizer;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.packets.c2s.AdjustServerConfig;
import io.wispforest.owo.packets.c2s.AskToOpenServerConfig;
import io.wispforest.owo.packets.s2c.OpenServerConfig;
import io.wispforest.owo.packets.s2c.OpenServerConfigSelection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

public class OwoPackets {

    public static final OwoNetChannel MAIN = OwoNetChannel.create(Identifier.of("owo", "main"));

    public static void initNetworking() {
        MAIN.registerServerbound(AskToOpenServerConfig.class, AskToOpenServerConfig::handle);
        MAIN.registerServerbound(AdjustServerConfig.class, AdjustServerConfig.ENDEC, AdjustServerConfig::handle);

        MAIN.registerClientboundDeferred(OpenServerConfig.class, OpenServerConfig.ENDEC);
        MAIN.registerClientboundDeferred(OpenServerConfigSelection.class, OpenServerConfigSelection.ENDEC);

        ConfigSynchronizer.initNetworking();
    }

    @Environment(EnvType.CLIENT)
    public static void initClientNetworking () {
        MAIN.registerClientbound(OpenServerConfig.class, OpenServerConfig.ENDEC, OpenServerConfig::handle);
        MAIN.registerClientbound(OpenServerConfigSelection.class, OpenServerConfigSelection.ENDEC, OpenServerConfigSelection::handle);

        ConfigSynchronizer.initClientNetworking();
    }
}
