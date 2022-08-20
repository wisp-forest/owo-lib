package io.wispforest.uwu.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;

public class UwuReiPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new UiCategory());
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.add(new UiCategory.UiDisplay());
    }

}
