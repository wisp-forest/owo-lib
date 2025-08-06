package io.wispforest.owo.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.wispforest.owo.Owo;
import io.wispforest.owo.command.debug.OwoDebugCommands;
import io.wispforest.owo.config.OwoConfigCommand;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import io.wispforest.owo.itemgroup.json.OwoItemGroupLoader;
import io.wispforest.owo.moddata.ModDataLoader;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.renderdoc.RenderDoc;
import io.wispforest.owo.renderdoc.RenderdocScreen;
import io.wispforest.owo.ui.core.OwoUIPipelines;
import io.wispforest.owo.ui.hud.Hud;
import io.wispforest.owo.ui.hud.HudInspectorScreen;
import io.wispforest.owo.ui.parsing.ConfigureHotReloadScreen;
import io.wispforest.owo.ui.parsing.UIModelLoader;
import io.wispforest.owo.ui.renderstate.*;
import io.wispforest.owo.ui.renderstate.OwoSpecialGuiElementRenderers;
import io.wispforest.owo.ui.util.NinePatchTexture;
import io.wispforest.owo.util.pond.OwoScreenHandlerExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@ApiStatus.Internal
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

        modBus.addListener(OwoUIPipelines::register);

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

        screenInternalsClientInit();

        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> {
            OwoConfigCommand.register(event.getDispatcher(), event.getBuildContext());
        });

        if (Owo.DEBUG) {
            owoDebugCommandsClientRegister();
        }

        OwoSpecialGuiElementRenderers.init(modBus);

        modBus.addListener(FMLClientSetupEvent.class, event -> {
            ConfigScreenProviders.forEach((modId, screenFactory) -> {
                ModList.get().getModContainerById(modId)
                        .ifPresent(mod -> mod.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, modsScreen) -> screenFactory.apply(modsScreen)));
            });
        });

        modBus.addListener(RegisterGuiLayersEvent.class, event -> {
            event.registerAboveAll(Hud.HUD_LAYER.name(), Hud.HUD_LAYER.layer());
        });
    }

    public static void screenInternalsClientInit() {
        NeoForge.EVENT_BUS.addListener((ScreenEvent.Init.Post event) -> {
            if (event.getScreen() instanceof ScreenHandlerProvider<?> handled)
                ((OwoScreenHandlerExtension) handled.getScreenHandler()).owo$attachToPlayer(MinecraftClient.getInstance().player);
        });
    }

    private static final SuggestionProvider<ServerCommandSource> LOADED_UI_MODELS =
        (context, builder) -> CommandSource.suggestIdentifiers(UIModelLoader.allLoadedModels(), builder);

    private static final SimpleCommandExceptionType NO_SUCH_UI_MODEL = new SimpleCommandExceptionType(Text.literal("No such UI model is loaded"));

    public static void owoDebugCommandsClientRegister() {
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) -> {
            var dispatcher = event.getDispatcher();
            var registryAccess = event.getBuildContext();

            dispatcher.register(literal("owo-hud-inspect")
                .executes(context -> {
                    MinecraftClient.getInstance().setScreen(new HudInspectorScreen());
                    return 0;
                }));

            dispatcher.register(literal("owo-ui-set-reload-path")
                .then(argument("model-id", IdentifierArgumentType.identifier()).suggests(LOADED_UI_MODELS).executes(context -> {
                    var modelId = context.getArgument("model-id", Identifier.class);
                    if (UIModelLoader.getPreloaded(modelId) == null) throw NO_SUCH_UI_MODEL.create();

                    MinecraftClient.getInstance().setScreen(new ConfigureHotReloadScreen(modelId, null));
                    return 0;
                })));

            if (RenderDoc.isAvailable()) {
                dispatcher.register(literal("renderdoc").executes(context -> {
                            MinecraftClient.getInstance().setScreen(new RenderdocScreen());
                            return 1;
                        })
                        .then(literal("comment")
                            .then(argument("capture_index", IntegerArgumentType.integer(0))
                                .then(argument("comment", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        var capture = RenderDoc.getCapture(IntegerArgumentType.getInteger(context, "capture_index"));
                                        if (capture == null) {
                                            context.getSource().sendError(TextOps.concat(Owo.PREFIX, Text.of("no such capture")));
                                            return 0;
                                        }

                                        RenderDoc.setCaptureComments(capture, StringArgumentType.getString(context, "comment"));
                                        context.getSource().sendFeedback(() -> TextOps.concat(Owo.PREFIX, Text.of("comment updated")), false);

                                        return 1;
                                    }))))
                );
            }
        });
    }
}
