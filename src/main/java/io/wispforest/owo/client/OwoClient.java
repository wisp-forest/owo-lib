package io.wispforest.owo.client;

import io.wispforest.owo.Owo;
import io.wispforest.owo.client.screens.ScreenInternals;
import io.wispforest.owo.command.debug.OwoDebugCommands;
import io.wispforest.owo.config.OwoConfigCommand;
import io.wispforest.owo.itemgroup.json.OwoItemGroupLoader;
import io.wispforest.owo.moddata.ModDataLoader;
import io.wispforest.owo.shader.BlurProgram;
import io.wispforest.owo.shader.GlProgram;
import io.wispforest.owo.ui.parsing.UIModelLoader;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
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

    public static final GlProgram HSV_PROGRAM = new GlProgram(Identifier.of("owo", "spectrum"), VertexFormats.POSITION_COLOR);
    public static final BlurProgram BLUR_PROGRAM = new BlurProgram();

    @Override
    public void onInitializeClient() {
        ModDataLoader.load(OwoItemGroupLoader.INSTANCE);

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new UIModelLoader());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new NinePatchTexture.MetadataLoader());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override public Identifier getFabricId() { return Identifier.of("owo", "after_shader_load"); }
            @Override public void reload(ResourceManager manager) { GlProgram.forEachProgram(Runnable::run); }
        });

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

        ClientCommandRegistrationCallback.EVENT.register(OwoConfigCommand::register);

        if (Owo.DEBUG) {
            OwoDebugCommands.Client.register();
        }
    }
}
