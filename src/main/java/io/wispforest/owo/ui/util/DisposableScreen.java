package io.wispforest.owo.ui.util;

import net.minecraft.client.gui.screens.Screen;

/**
 * Screens that wish to be notified when the players navigates back to
 * the game instead of to another screen may implement this interface
 * for a more reliable alternative to {@link Screen#removed()}
 */
public interface DisposableScreen {

    /**
     * Invoked when a best-effort algorithm has determined
     * that the player is navigating to return to the game instead of opening
     * another screen - ensured to be called too often than too rarely
     */
    void dispose();

}
