package io.wispforest.owo.ui.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public final class UISounds {

    public static final SoundEvent UI_INTERACTION = SoundEvent.createVariableRangeEvent(Identifier.of("owo", "ui.owo.interaction"));

    private UISounds() {}

    @Environment(EnvType.CLIENT)
    public static void playButtonSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
    }

    @Environment(EnvType.CLIENT)
    public static void playInteractionSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(UI_INTERACTION, 1));
    }

}
