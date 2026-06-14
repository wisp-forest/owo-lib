package io.wispforest.owo;

import io.wispforest.owo.client.screens.MenuNetworkingInternals;
import io.wispforest.owo.command.debug.OwoDebugCommands;
import io.wispforest.owo.config.ConfigSynchronizer;
import io.wispforest.owo.neoforge.api.ArgumentTypeRegistry;
import io.wispforest.owo.network.OwoHandshake;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.ops.LootOps;
import io.wispforest.owo.text.CustomTextRegistry;
import io.wispforest.owo.text.InsertingTextContent;
import io.wispforest.owo.util.OwoFreezer;
import io.wispforest.owo.util.Wisdom;
//import net.fabricmc.api.ModInitializer;
//import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
//import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.wispforest.owo.ops.TextOps.withColor;

@Mod(value = Owo.MOD_ID)
public class Owo /*implements ModInitializer*/ {

    public static final String MOD_ID = "owo";
    /**
     * Whether oωo debug is enabled, this defaults to {@code true} in a development environment.
     * To override that behavior, add the {@code -Dowo.debug=false} java argument
     */
    public static final boolean DEBUG;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static MinecraftServer SERVER;

    public static final Component PREFIX = Component.empty().withStyle(ChatFormatting.GRAY)
        .append(withColor("o", 0x3955e5))
        .append(withColor("ω", 0x13a6f0))
        .append(withColor("o", 0x3955e5))
        .append(Component.literal(" > ").withStyle(ChatFormatting.GRAY));

    static {
        boolean debug = !FMLEnvironment.isProduction();
        if (System.getProperty("owo.debug") != null) debug = Boolean.getBoolean("owo.debug");
        if (Boolean.getBoolean("owo.forceDisableDebug")) {
            LOGGER.warn("Deprecated system property 'owo.forceDisableDebug=true' was used - use 'owo.debug=false' instead");
            debug = false;
        }

        DEBUG = debug;
    }

    public Owo(IEventBus modBus) {
        modBus.<FMLCommonSetupEvent>addListener((event) -> this.onInitialize(modBus));
    }

    /*@Override*/
    @ApiStatus.Internal
    public void onInitialize(IEventBus modBus) {
        LootOps.registerListener();
        CustomTextRegistry.register("index", InsertingTextContent.CODEC);


        NeoForge.EVENT_BUS.<ServerStartingEvent>addListener((event) -> SERVER = event.getServer());
        NeoForge.EVENT_BUS.<ServerStoppingEvent>addListener((_) -> SERVER = null);

        Wisdom.spread();

        if (!DEBUG) return;

        OwoDebugCommands.register();

        modBus.<RegisterPayloadHandlersEvent>addListener(event -> {
            var registrar = event.registrar("1.0.0");

            MenuNetworkingInternals.init(registrar);
            ConfigSynchronizer.init(registrar);
            OwoHandshake.init(modBus, registrar);
            OwoNetChannel.init(registrar);
        });

        modBus.<FMLLoadCompleteEvent>addListener(EventPriority.LOW, event -> {
            OwoFreezer.freeze();
        });
        ArgumentTypeRegistry.init(modBus);
    }

    @ApiStatus.Internal
    public static void debugWarn(Logger logger, String message) {
        if (!DEBUG) return;
        logger.warn(message);
    }

    @ApiStatus.Internal
    public static void debugWarn(Logger logger, String message, Object... params) {
        if (!DEBUG) return;
        logger.warn(message, params);
    }

    /**
     * @return The currently active minecraft server instance. If running
     * on a physical client, this will return the integrated server while in
     * a local singleplayer world and {@code null} otherwise
     */
    public static MinecraftServer currentServer() {
        return SERVER;
    }

    // "eh it's only like 10-15 of them what's the big deal" - glisco, while writing the 52nd hardcoded Identifier.of("owo", ...)
    @ApiStatus.Internal
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
