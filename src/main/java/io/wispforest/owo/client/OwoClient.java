package io.wispforest.owo.client;


import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.BraidRenderPipelines;
import io.wispforest.owo.braid.display.BraidDisplay;
import io.wispforest.owo.client.screens.MenuNetworkingInternals;
import io.wispforest.owo.command.debug.OwoDebugCommands;
import io.wispforest.owo.config.OwoConfigCommand;
import io.wispforest.owo.itemgroup.json.OwoItemGroupLoader;
import io.wispforest.owo.moddata.ModDataLoader;
import io.wispforest.owo.ui.core.OwoUIPipelines;
import io.wispforest.owo.ui.hud.Hud;
import io.wispforest.owo.ui.parsing.UIModelLoader;
import io.wispforest.owo.ui.renderstate.OwoSpecialGuiElementRenderers;
import io.wispforest.owo.ui.util.NinePatchTexture;
//import net.fabricmc.api.ClientModInitializer;
import io.wispforest.owo.neoforge.env.EnvType;
import io.wispforest.owo.neoforge.env.Environment;
//import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.client.renderer.RenderPipelines;
//import net.minecraft.server.packs.PackType;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class OwoClient /*implements ClientModInitializer*/ {

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
        modBus.<FMLClientSetupEvent>addListener((event) -> this.onInitializeClient(modBus));
    }

    /*@Override*/
    public void onInitializeClient(IEventBus modBus) {
        ModDataLoader.load(OwoItemGroupLoader.INSTANCE);

        modBus.<AddClientReloadListenersEvent>addListener((event) -> {
            event.addListener(UIModelLoader.getFabricId(), new UIModelLoader());
            event.addListener(NinePatchTexture.MetadataLoader.getFabricId(), new NinePatchTexture.MetadataLoader());
        });

        modBus.<RegisterRenderPipelinesEvent>addListener((event) -> {
            OwoUIPipelines.register(event);
            BraidRenderPipelines.register(event);
            event.registerPipeline(BraidDisplay.PIPELINE);
        });

        final var renderdocPath = System.getProperty("owo.renderdocPath");
        if (renderdocPath != null) {
            if (Util.getPlatform() == Util.OS.WINDOWS) {
                System.load(renderdocPath);
            } else {
                Owo.LOGGER.warn(switch (Util.getPlatform()) {
                    case LINUX -> LINUX_RENDERDOC_WARNING;
                    case OSX -> MAC_RENDERDOC_WARNING;
                    default -> GENERIC_RENDERDOC_WARNING;
                });
            }
        }

        modBus.<RegisterClientPayloadHandlersEvent>addListener(event -> {
            event.register();
        });
        MenuNetworkingInternals.Client.init();

        NeoForge.EVENT_BUS.<RegisterClientCommandsEvent>addListener((event) -> {
            var dispatcher = event.getDispatcher(); var access = event.getBuildContext();
            OwoConfigCommand.register(dispatcher, access);
        });

        if (Owo.DEBUG) {
            OwoDebugCommands.Client.register();
        }

        modBus.addListener(OwoSpecialGuiElementRenderers::init);
        Hud.init(modBus);
    }
}
