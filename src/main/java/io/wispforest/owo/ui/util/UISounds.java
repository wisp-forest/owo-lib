package io.wispforest.owo.ui.util;

import io.wispforest.owo.Owo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public final class UISounds {

    public static final SoundEvent UI_INTERACTION = SoundEvent.createVariableRangeEvent(Owo.id("ui.owo.interaction"));

    private UISounds() {}

    @Environment(EnvType.CLIENT)
    public static void play(SoundEvent event) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, 1));
    }

    @Environment(EnvType.CLIENT)
    public static void playButtonSound() {
        play(SoundEvents.UI_BUTTON_CLICK.value());
    }

    @Environment(EnvType.CLIENT)
    public static void playInteractionSound() {
        play(UI_INTERACTION);
    }
}
