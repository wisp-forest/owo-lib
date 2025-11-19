package io.wispforest.owo.itemgroup.gui;

import io.wispforest.owo.itemgroup.base.OwoItemGroup;
import io.wispforest.owo.itemgroup.base.OwoItemGroupState;
import io.wispforest.owo.itemgroup.impl.DefaultOwoItemGroupRenderer;
import io.wispforest.owo.itemgroup.util.DisplayContextUtils;
import io.wispforest.owo.mixin.itemgroup.CreativeInventoryScreenAccessor;
import io.wispforest.owo.mixin.itemgroup.ScreenAccessor;
import io.wispforest.owo.ui.core.PositionedRectangle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;


///
/// A Renderer implementation for [OwoItemGroup] used to adjust various rendering of such a ItemGroup. If not set, the
/// base renderer instance used will be of [DefaultOwoItemGroupRenderer].
///
@Environment(EnvType.CLIENT)
public abstract class OwoItemGroupRenderer {

    private final OwoItemGroupState state;

    protected OwoItemGroupRenderer(OwoItemGroupState state) {
        this.state = state;
    }

    ///
    /// @return The current [OwoItemGroupState] tied to a given [ItemGroup] via the [OwoItemGroup] extension
    ///
    protected final OwoItemGroupState state() {
        return state;
    }

    ///
    /// @return The current [OwoItemGroup] extension tied to a given [ItemGroup] via the [OwoItemGroupState]
    ///
    protected final OwoItemGroup getExtension() {
        return state.getExtension();
    }

    //--

    ///
    /// Called at the start of [CreativeInventoryScreen.render()][CreativeInventoryScreen#render(net.minecraft.client.gui.DrawContext, int, int, float)]
    /// and used to set up the renderer state for mouse position and current delta.
    ///
    public void beforeRender(int mouseX, int mouseY, float delta) { }

    ///
    /// Called at the end of [CreativeInventoryScreen.render()][CreativeInventoryScreen#render(net.minecraft.client.gui.DrawContext, int, int, float)]
    /// and used to apply cursor styles or handle tooltip hovering if need be.
    ///
    public void afterRender() { }

    ///
    /// Called when [CreativeInventoryScreen.removed()()][CreativeInventoryScreen#render(net.minecraft.client.gui.DrawContext, int, int, float)]
    /// is invoked though [OwoItemGroupRendererHandler.disposeInstances()][OwoItemGroupRendererHandler#disposeInstances()] to properly handle disposal
    /// of any data before the screen is closed.
    ///
    public void dispose() { }

    ///
    /// Called when the linked [ItemGroup] is opened allowing for the setup of various widgets.
    ///
    protected abstract void init(int x, int y, CreativeInventoryScreen screen);

    ///
    /// Called when a new [ItemGroup] is opened allowing for the given renderer to remove any widgets added like
    /// side buttons or tab buttons.
    ///
    protected void closed(CreativeInventoryScreen screen) { }

    //--

    ///
    /// Called when rendering the background texture
    ///
    /// @param context         The current [DrawContext]
    /// @param x               The current x position of the rendering
    /// @param y               The current y position of the rendering
    /// @param textureAdjuster A consumer allowing for the ability to change what texture is drawn
    ///
    /// @return if the given original renderer method should be invoked
    ///
    public boolean renderBackground(DrawContext context, int x, int y, Consumer<Identifier> textureAdjuster) {
        return false;
    }

    ///
    /// Called when rendering the creative screens page swapping button's texture
    ///
    /// @param context         The current [DrawContext]
    /// @param x               The current x position of the rendering
    /// @param y               The current y position of the rendering
    /// @param textureAdjuster A consumer allowing for the ability to change what texture is drawn
    ///
    /// @return if the given original renderer method should be invoked
    ///
    public boolean renderPageButtons(DrawContext context, int x, int y, Consumer<Identifier> textureAdjuster) {
        return false;
    }

    ///
    /// Called when rendering the scrollbar texture
    ///
    /// @param context         The current [DrawContext]
    /// @param x               The current x position of the rendering
    /// @param y               The current y position of the rendering
    /// @param textureAdjuster A consumer allowing for the ability to change what texture is drawn
    ///
    /// @return if the given original renderer method should be invoked
    ///
    public boolean renderScrollbar(DrawContext context, int x, int y, boolean hasScrollbar, Consumer<Identifier> textureAdjuster) {
        return false;
    }

    ///
    /// Called when rendering the given ItemGroups tab texture
    ///
    /// @param context         The current [DrawContext]
    /// @param x               The current x position of the rendering
    /// @param y               The current y position of the rendering
    /// @param textureAdjuster A consumer allowing for the ability to change what texture is drawn
    ///
    /// @return if the given original renderer method should be invoked
    ///
    public boolean renderTab(DrawContext context, int x, int y, ItemGroup group, Consumer<Identifier> textureAdjuster) {
        return false;
    }

    ///
    /// Called when rendering the given ItemGroups Tab Icon
    ///
    /// @param context         The current [DrawContext]
    /// @param x               The current x position of the rendering
    /// @param y               The current y position of the rendering
    ///
    /// @return if the given original renderer method should be invoked
    ///
    public boolean renderIcon(DrawContext context, int x, int y) {
        return false;
    }

    ///
    /// Called when rendering the given ItemGroups Title
    ///
    /// @param context         The current [DrawContext]
    /// @param x               The current x position of the rendering
    /// @param y               The current y position of the rendering
    /// @param text            The current title text for the group
    /// @param textAdjuster    A consumer allowing for the ability to change what [Text] is drawn
    ///
    /// @return if the given original renderer method should be invoked
    ///
    public boolean renderTitle(DrawContext context, int x, int y, Text text, Consumer<Text> textAdjuster) {
        return false;
    }

    /**
     * Compute a stream of all [Rectangle] for which to generate exclusion areas in a recipe viewer overlay.
     * Called by the REI and EMI plugins
     */
    public Stream<PositionedRectangle> getExclusionZones(int x, int y) {
        return Stream.empty();
    }

    public final <T> Stream<T> getExclusionZones(int x, int y, Function<PositionedRectangle, T> conversionFunc) {
        return getExclusionZones(x, y).map(conversionFunc);
    }

    //--

    public static <T extends ButtonWidget> Consumer<T> createTabSelectAction(OwoItemGroupState state, int tabIdx, CreativeInventoryScreen screen) {
        return button -> {
            var context = DisplayContextUtils.createContext(screen.getScreenHandler().player());

            if (Screen.hasShiftDown()) {
                state.toggleTab(tabIdx, context);
            } else {
                state.selectSingleTab(tabIdx, context);
            }

            if (button instanceof ItemGroupButtonWidget widget) {
                widget.isSelected = true;
            }

            ((ScreenAccessor) screen).owo$clearAndInit();
        };
    }

    public static ItemGroup selectedItemGroup() {
        return CreativeInventoryScreenAccessor.owo$getSelectedTab();
    }
}
