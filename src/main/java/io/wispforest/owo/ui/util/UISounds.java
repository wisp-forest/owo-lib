package io.wispforest.owo.ui.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class UISounds {

    public static final SoundEvent UI_INTERACTION = new SoundEvent(new Identifier("owo", "ui.owo.interaction"));

    public static void playButtonSound() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1));
    }

    public static void playInteractionSound() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(UI_INTERACTION, 1));
    }

}
