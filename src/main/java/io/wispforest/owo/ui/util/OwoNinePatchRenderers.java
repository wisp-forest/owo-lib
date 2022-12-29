package io.wispforest.owo.ui.util;

import io.wispforest.owo.ui.core.Size;
import net.minecraft.util.Identifier;

public final class OwoNinePatchRenderers {

    private OwoNinePatchRenderers() {}

    // Scrollbar central

    private static final Identifier SCROLLBAR_TEXTURE = new Identifier("owo", "textures/gui/scrollbar.png");

    public static final NinePatchRenderer VERTICAL_VANILLA_SCROLLBAR
            = new NinePatchRenderer(SCROLLBAR_TEXTURE, 0, 0, Size.square(2), Size.of(8, 12), Size.of(64, 32), true);

    public static final NinePatchRenderer DISABLED_VERTICAL_VANILLA_SCROLLBAR
            = new NinePatchRenderer(SCROLLBAR_TEXTURE, 13, 0, Size.square(2), Size.of(8, 12), Size.of(64, 32), true);

    public static final NinePatchRenderer HORIZONTAL_VANILLA_SCROLLBAR
            = new NinePatchRenderer(SCROLLBAR_TEXTURE, 0, 16, Size.square(2), Size.of(8, 12), Size.of(64, 32), true);

    public static final NinePatchRenderer DISABLED_HORIZONTAL_VANILLA_SCROLLBAR
            = new NinePatchRenderer(SCROLLBAR_TEXTURE, 13, 16, Size.square(2), Size.of(8, 12), Size.of(64, 32), true);

    public static final NinePatchRenderer VANILLA_SCROLLBAR_TRACK
            = new NinePatchRenderer(SCROLLBAR_TEXTURE, 26, 0, Size.square(1), Size.of(4, 30), Size.of(64, 32), false);

    public static final NinePatchRenderer FLAT_VANILLA_SCROLLBAR
            = new NinePatchRenderer(SCROLLBAR_TEXTURE, 33, 0, Size.square(1), Size.of(14, 30), Size.of(64, 32), false);

    // Panel outpost

    public static final NinePatchRenderer LIGHT_PANEL = new NinePatchRenderer(Drawer.PANEL_TEXTURE, Size.square(5), Size.square(16), false);
    public static final NinePatchRenderer DARK_PANEL = new NinePatchRenderer(Drawer.DARK_PANEL_TEXTURE, Size.square(5), Size.square(16), false);

    // Button Headquarters

    private static final Identifier BUTTON_TEXTURE = new Identifier("owo", "textures/gui/buttons.png");

    public static final NinePatchRenderer ACTIVE_BUTTON
            = new NinePatchRenderer(BUTTON_TEXTURE, 0, 0, Size.square(3), Size.square(58), Size.of(64, 192), true);

    public static final NinePatchRenderer HOVERED_BUTTON
            = new NinePatchRenderer(BUTTON_TEXTURE, 0, 64, Size.square(3), Size.square(58), Size.of(64, 192), true);

    public static final NinePatchRenderer BUTTON_DISABLED
            = new NinePatchRenderer(BUTTON_TEXTURE, 0, 128, Size.square(3), Size.square(58), Size.of(64, 192), true);
}
