package io.wispforest.owo.ui.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public final class UISounds {

    public static final SoundEvent UI_INTERACTION = SoundEvent.of(Identifier.of("owo", "ui.owo.interaction"));

    private UISounds() {}

    @Environment(EnvType.CLIENT)
    public static void playButtonSound() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1));
    }

    @Environment(EnvType.CLIENT)
    public static void playInteractionSound() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(UI_INTERACTION, 1));
    }

}
