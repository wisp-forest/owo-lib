package io.wispforest.owo;

import io.wispforest.owo.command.OwoDebugCommands;
import io.wispforest.owo.itemgroup.json.GroupTabLoader;
import io.wispforest.owo.moddata.ModDataLoader;
import io.wispforest.owo.ops.LootOps;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

public class Owo implements ModInitializer {

    /**
     * Whether oωo debug is enabled, this defaults to {@code true} in a development environment.
     * To override that behaviour, add the {@code -Dowo.forceDisableDebug=true} java argument
     */
    public static final boolean DEBUG;
    public static final Logger LOGGER = LogManager.getLogger("owo");

    static {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            DEBUG = !Boolean.getBoolean("owo.forceDisableDebug");
        } else {
            DEBUG = Boolean.getBoolean("owo.debug");
        }
    }

    @Override
    @ApiStatus.Internal
    public void onInitialize() {
        ModDataLoader.load(new GroupTabLoader());
        LootOps.registerListener();

        if (!DEBUG) return;

        OwoDebugCommands.register();
    }

}