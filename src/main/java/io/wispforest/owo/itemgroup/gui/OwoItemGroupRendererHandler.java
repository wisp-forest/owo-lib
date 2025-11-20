package io.wispforest.owo.itemgroup.gui;

import io.wispforest.owo.Owo;
import io.wispforest.owo.itemgroup.OwoItemGroupBuilder;
import io.wispforest.owo.itemgroup.base.OwoItemGroupState;
import io.wispforest.owo.itemgroup.impl.DefaultOwoItemGroupRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

///
/// Acts as a point to registry [Factory]'s to create [OwoItemGroupRenderer] from a given [OwoItemGroupState]
/// and to store the currently active renderer for proper setup and disposal.
///
@Environment(EnvType.CLIENT)
public class OwoItemGroupRendererHandler {

    // TODO: MAYBE NULL IS BAD BUT I THINK SINCE NO CALL IS HAPPENING INSIDE SUCH, ITS FINE AND I WANT SUCH FOR THE BASE RENDERER CLASS So....?
    @ApiStatus.Internal
    public static final OwoItemGroupRenderer EMPTY = new OwoItemGroupRenderer(null) {
        @Override
        protected void init(int x, int y, CreativeInventoryScreen screen) {}
    };

    @Nullable
    private static OwoItemGroupRenderer INSTANCE = null;

    //--

    private static final Map<Identifier, Factory> RENDERERS = new LinkedHashMap<>();

    static {
        registerRenderer(OwoItemGroupBuilder.DEFAULT_RENDERER_ID, DefaultOwoItemGroupRenderer::new);
    }

    ///
    /// Used to register a given [OwoItemGroupRenderer] [Factory] to a given [Identifier] allowing for others to adjust
    /// the used renderer via the [OwoItemGroupBuilder.renderer()][OwoItemGroupBuilder#renderer].
    ///
    public static void registerRenderer(Identifier identifier, Factory factory) {
        if (RENDERERS.containsKey(identifier)) {
            throw new IllegalStateException("An already existing renderer has been registered under the given id: " + identifier);
        }

        RENDERERS.put(identifier, factory);
    }

    public interface Factory {
        OwoItemGroupRenderer createRenderer(OwoItemGroupState state);
    }

    //--

    private static final Map<ItemGroup, OwoItemGroupRenderer> GROUP_TO_RENDERER = new LinkedHashMap<>();

    @ApiStatus.Internal
    public static void setupRenderer(ItemGroup group, int x, int y, CreativeInventoryScreen screen) {
        if (INSTANCE != null) {
            INSTANCE.closed(screen);
        }

        INSTANCE = getRenderer(group);

        INSTANCE.init(x, y, screen);
    }

    @ApiStatus.Internal
    public static OwoItemGroupRenderer getRenderer(ItemGroup group) {
        var state = OwoItemGroupState.get(group, true);

        if (state == null) return EMPTY;

        if (!GROUP_TO_RENDERER.containsKey(group)) {
            var rendererId = state.getExtension().rendererId();

            var factory = RENDERERS.get(rendererId);

            if (factory == null) {
                Owo.LOGGER.warn("Unable to get renderer '{}' for a given ItemGroup '{}', using default renderer!", rendererId, Registries.ITEM_GROUP.getId(group));

                factory = DefaultOwoItemGroupRenderer::new;
            }

            GROUP_TO_RENDERER.put(group, factory.createRenderer(state));
        }

        return GROUP_TO_RENDERER.get(group);
    }

    @ApiStatus.Internal
    public static OwoItemGroupRenderer getSelectedRenderer() {
        if (INSTANCE == null) return EMPTY;

        return INSTANCE;
    }

    @ApiStatus.Internal
    public static void disposeInstances() {
        for (var value : GROUP_TO_RENDERER.values()) {
            value.dispose();
        }
    }
}
