package io.wispforest.uwu.rei;

import io.wispforest.owo.braid.core.BraidScreen;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.DisplayBoundsProvider;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.Nullable;

public class UwuReiPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new UiCategory());
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.add(new UiCategory.UiDisplay());
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerDecider(new DisplayBoundsProvider<BraidScreen>() {
            @Override
            public @Nullable Rectangle getScreenBounds(BraidScreen screen) {
                return new Rectangle(screen.width / 4, screen.height / 4, screen.width / 2, screen.height / 2);
            }

            @Override
            public <R extends Screen> boolean isHandingScreen(Class<R> screen) {
                return BraidScreen.class.isAssignableFrom(screen);
            }

            @Override
            public <R extends Screen> InteractionResult shouldScreenBeOverlaid(R screen) {
                return InteractionResult.SUCCESS;
            }
        });
    }
}
