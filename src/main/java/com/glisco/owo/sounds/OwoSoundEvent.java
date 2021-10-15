package com.glisco.owo.sounds;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class OwoSoundEvent extends SoundEvent {
    private SoundCategory category;

    public OwoSoundEvent(Identifier id, @Nullable() SoundCategory category) {
        super(id);
        this.category = (category != null) ? category : SoundCategory.MASTER;
    }

    /**
     * Create a OwoSoundEvent from a vanilla sound event.
     * @param event The SoundEvent
     * @param category The category of the sound. Nullable
     * @return The OwoSoundEvent
     */
    public static OwoSoundEvent from(SoundEvent event, @Nullable() SoundCategory category) {
        return new OwoSoundEvent(event.getId(), category);
    }

    /**
     * Play the OwoSoundEvent in a world at a specified location.
     * @param world The world to play the SoundEvent in.
     * @param x The x position of the sound.
     * @param y The y position of the sound.
     * @param z The z position of the sound.
     */
    public void play(World world, double x, double y, double z) {
        world.playSound(x, y, z, this, this.category, 1F, 1F, false);
    }

    /**
     * Play the OwoSoundEvent in a world at a specified location, with a specified volume.
     * @param world The world to play the SoundEvent in.
     * @param x The x position of the sound.
     * @param y The y position of the sound.
     * @param z The z position of the sound.
     * @param volume The volume of the sound.
     */
    public void play(World world, double x, double y, double z, int volume) {
        world.playSound(x, y, z, this, this.category, 1F, 1F, false);
    }

    /**
     * Play the OwoSoundEvent in a world at a specified location, with a specified volume and pitch.
     * @param world The world to play the SoundEvent in.
     * @param x The x position of the sound.
     * @param y The y position of the sound.
     * @param z The z position of the sound.
     * @param volume The volume of the sound.
     * @param pitch The pitch of the sound.
     */
    public void play(World world, double x, double y, double z, float volume, float pitch) {
        world.playSound(x, y, z, this, this.category, volume, pitch, false);
    }

    /**
     * Play the OwoSoundEvent in a world at a specified location using distance mechanics, with a specified volume and pitch.
     * @param world The world to play the SoundEvent in.
     * @param x The x position of the sound.
     * @param y The y position of the sound.
     * @param z The z position of the sound.
     * @param volume The volume of the sound.
     * @param pitch The pitch of the sound.
     */
    public void play(World world, double x, double y, double z, float volume, float pitch, boolean useDistance) {
        world.playSound(x, y, z, this, this.category, volume, pitch, useDistance);
    }

    /**
     * Play the OwoSoundEvent without a linked world. Useful for GUIs or anything that doesn't take place inside a world.
     * @param client The current minecraft client.
     */
    public void playUnlinked(MinecraftClient client, float volume, float pitch) {
        client.getSoundManager().play(new OwoSoundInstance(this, volume, pitch));
    }

    /**
     * Play the OwoSoundEvent without a linked world. Useful for GUIs or anything that doesn't take place inside a world.
     * @param client The current minecraft client.
     * @param delay The delay, in ticks, until the sound is played.
     */
    public void playUnlinked(MinecraftClient client, float volume, float pitch, int delay) {
        client.getSoundManager().play(new OwoSoundInstance(this, volume, pitch), delay);
    }
    
    // Getters and setters.

    public SoundCategory getCategory() {
        return category;
    }

    public void setCategory(SoundCategory category) {
        this.category = category;
    }
}
