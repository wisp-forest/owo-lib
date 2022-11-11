package io.wispforest.owo.client;

import io.wispforest.owo.Owo;
import io.wispforest.owo.client.screens.ScreenInternals;
import io.wispforest.owo.command.debug.OwoDebugCommands;
import io.wispforest.owo.itemgroup.json.OwoItemGroupLoader;
import io.wispforest.owo.moddata.ModDataLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public class OwoClient implements ClientModInitializer {

    private static final String LINUX_RENDERDOC_WARNING = """

            ========================================
            Ignored 'owo.renderdocPath' property as this Minecraft instance is not running on Windows.
            Please populate the LD_PRELOAD environment variable instead
            ========================================""";

    private static final String MAC_RENDERDOC_WARNING = """

            ========================================
            Ignored 'owo.renderdocPath' property as this Minecraft instance is not running on Windows.
            RenderDoc is not supported on macOS
            ========================================""";

    private static final String GENERIC_RENDERDOC_WARNING = """

            ========================================
            Ignored 'owo.renderdocPath' property as this Minecraft instance is not running on Windows.
            ========================================""";

    @Override
    public void onInitializeClient() {
        ModDataLoader.load(OwoItemGroupLoader.INSTANCE);

        final var renderdocPath = System.getProperty("owo.renderdocPath");
        if (renderdocPath != null) {
            if (Util.getOperatingSystem() == Util.OperatingSystem.WINDOWS) {
                System.load(renderdocPath);
            } else {
                Owo.LOGGER.warn(switch (Util.getOperatingSystem()) {
                    case LINUX -> LINUX_RENDERDOC_WARNING;
                    case OSX -> MAC_RENDERDOC_WARNING;
                    default -> GENERIC_RENDERDOC_WARNING;
                });
            }
        }

        ScreenInternals.Client.init();

        if (!Owo.DEBUG) return;
        OwoDebugCommands.Client.register();
    }
}
