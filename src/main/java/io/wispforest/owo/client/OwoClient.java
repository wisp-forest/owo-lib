package io.wispforest.owo.client;

import io.wispforest.owo.Owo;
import io.wispforest.owo.client.screens.ScreenInternals;
import io.wispforest.owo.command.debug.OwoDebugCommands;
import io.wispforest.owo.config.OwoConfigCommand;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import io.wispforest.owo.itemgroup.json.OwoItemGroupLoader;
import io.wispforest.owo.moddata.ModDataLoader;
import io.wispforest.owo.ui.core.OwoUIPipelines;
import io.wispforest.owo.ui.parsing.UIModelLoader;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@OnlyIn(Dist.CLIENT)
@Mod(value = "owo", dist = Dist.CLIENT)
public class OwoClient {

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

    public OwoClient(IEventBus modBus) {
        ModDataLoader.load(OwoItemGroupLoader.INSTANCE);
        OwoItemGroupLoader.initItemGroupCallback();

        modBus.addListener((AddClientReloadListenersEvent event) -> {
            event.addListener(UIModelLoader.getFabricId(), new UIModelLoader());
            event.addListener(NinePatchTexture.MetadataLoader.getFabricId(), new NinePatchTexture.MetadataLoader());
        });

        OwoUIPipelines.register();

        if (Owo.DEBUG) {
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
        }

        ScreenInternals.Client.init();

        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> {
            OwoConfigCommand.register(event.getDispatcher(), event.getBuildContext());
        });

        if (Owo.DEBUG) {
            OwoDebugCommands.Client.register();
        }

        modBus.addListener(FMLClientSetupEvent.class, event -> {
            ConfigScreenProviders.forEach((modId, screenFactory) -> {
                ModList.get().getModContainerById(modId)
                        .ifPresent(mod -> mod.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, modsScreen) -> screenFactory.apply(modsScreen)));
            });
        });
    }
}
