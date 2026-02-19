package io.wispforest.owo;

import io.wispforest.owo.client.screens.MenuNetworkingInternals;
import io.wispforest.owo.command.debug.OwoDebugCommands;
import io.wispforest.owo.network.neoforge.NeoOwoNetworking;
import io.wispforest.owo.ops.LootOps;
import io.wispforest.owo.util.OwoFreezer;
import io.wispforest.owo.util.RecipeRemainderStorage;
import io.wispforest.owo.util.Wisdom;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static io.wispforest.owo.ops.TextOps.withColor;

@Mod("owo")
public class Owo {

    public static final String MOD_ID = "owo";
    /**
     * Whether oωo debug is enabled, this defaults to {@code true} in a development environment.
     * To override that behavior, add the {@code -Dowo.debug=false} java argument
     */
    public static final boolean DEBUG;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Component PREFIX = Component.empty().withStyle(ChatFormatting.GRAY)
        .append(withColor("o", 0x3955e5))
        .append(withColor("ω", 0x13a6f0))
        .append(withColor("o", 0x3955e5))
        .append(Component.literal(" > ").withStyle(ChatFormatting.GRAY));

    static {
        boolean debug = !FMLLoader.getCurrent().isProduction();
        if (System.getProperty("owo.debug") != null) debug = Boolean.getBoolean("owo.debug");
        if (Boolean.getBoolean("owo.forceDisableDebug")) {
            LOGGER.warn("Deprecated system property 'owo.forceDisableDebug=true' was used - use 'owo.debug=false' instead");
            debug = false;
        }

        DEBUG = debug;
    }

    @Nullable
    private static IEventBus MOD_BUS = null;

    public Owo(IEventBus modBus) {
        MOD_BUS = modBus;

        LootOps.registerListener();

        modBus.addListener((FMLLoadCompleteEvent event) -> OwoFreezer.freeze());

        Wisdom.spread();

        modBus.addListener(NeoOwoNetworking::onNetworkRegister);
        NeoForge.EVENT_BUS.addListener(RecipeRemainderStorage::addReloadListener);

        if (DEBUG) {
            OwoDebugCommands.register(modBus);
        }
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
        return ServerLifecycleHooks.getCurrentServer();
    }

    // "eh it's only like 10-15 of them what's the big deal" - glisco, while writing the 52nd hardcoded Identifier.of("owo", ...)
    @ApiStatus.Internal
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static IEventBus getModBus() {
        return Objects.requireNonNull(MOD_BUS, "Mod bus attempted to be gotten before time!");
    }
}
