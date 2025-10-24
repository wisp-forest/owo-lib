package io.wispforest.owo.mixin.braid;

import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.util.pond.OwoScreenExtension;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Screen.class, priority = 1100)
public abstract class ScreenMixin implements OwoScreenExtension {

    @Unique
    private @Nullable AppState braidLayersState;

    @Override
    public void owo$setBraidLayersState(AppState state) {
        this.braidLayersState = state;
    }

    @Override
    public @Nullable AppState owo$getBraidLayersState() {
        return this.braidLayersState;
    }
}
