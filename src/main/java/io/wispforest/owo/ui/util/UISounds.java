package io.wispforest.owo.ui.util;

import io.wispforest.owo.Owo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public final class UISounds {

    public static final SoundEvent UI_INTERACTION = SoundEvent.of(Owo.id("ui.owo.interaction"));

    private UISounds() {}

    @Environment(EnvType.CLIENT)
    public static void play(SoundEvent event) {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(event, 1));
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
