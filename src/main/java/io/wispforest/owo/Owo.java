package io.wispforest.owo;

import io.wispforest.owo.client.screens.ScreenInternals;
import io.wispforest.owo.command.debug.OwoDebugCommands;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.ops.LootOps;
import io.wispforest.owo.util.OwoFreezer;
import io.wispforest.owo.util.Wisdom;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import static io.wispforest.owo.ops.TextOps.withColor;

@Mod("owo")
public class Owo {

    /**
     * Whether oωo debug is enabled, this defaults to {@code true} in a development environment.
     * To override that behavior, add the {@code -Dowo.debug=false} java argument
     */
    public static final boolean DEBUG;
    public static final Logger LOGGER = LogManager.getLogger("owo");

    public static final Text PREFIX = Text.empty().formatted(Formatting.GRAY)
            .append(withColor("o", 0x3955e5))
            .append(withColor("ω", 0x13a6f0))
            .append(withColor("o", 0x3955e5))
            .append(Text.literal(" > ").formatted(Formatting.GRAY));

    static {
        boolean debug = !FMLLoader.isProduction();
        if (System.getProperty("owo.debug") != null) debug = Boolean.getBoolean("owo.debug");
        if (Boolean.getBoolean("owo.forceDisableDebug")) {
            LOGGER.warn("Deprecated system property 'owo.forceDisableDebug=true' was used - use 'owo.debug=false' instead");
            debug = false;
        }

        DEBUG = debug;
    }

    public Owo(IEventBus modBus) {
        LootOps.registerListener();
        ScreenInternals.init();

        modBus.addListener((FMLLoadCompleteEvent event) -> OwoFreezer.freeze());

        modBus.addListener(FMLCommonSetupEvent.class, event -> {
            ConfigScreen.forEachProvider((modId, screenFactory) -> {
                ModList.get().getModContainerById(modId)
                    .ifPresent(mod -> mod.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, modsScreen) -> screenFactory.apply(modsScreen)));
            });
        });

        Wisdom.spread();

        if (!DEBUG) return;

        OwoDebugCommands.register(modBus);
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

}