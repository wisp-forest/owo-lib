package com.glisco.owo.sounds;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * Wrapper for AbstractSoundInstance
 */
public class OwoSoundInstance implements SoundInstance {

    protected Sound sound;
    protected final SoundCategory category;
    protected final Identifier id;
    protected float volume;
    protected float pitch;
    protected double x;
    protected double y;
    protected double z;
    protected boolean repeat;
    protected int repeatDelay;
    protected AttenuationType attenuationType;
    protected boolean relative;

    /**
     * Creates a new OwoSoundInstance
     * @param sound The OwoSoundEvent to wrap.
     */
    protected OwoSoundInstance(OwoSoundEvent sound, float volume, float pitch) {
        this.category = sound.getCategory();
        this.id = sound.getId();
        this.setVolume(volume);
        this.setPitch(pitch);
    }

    /**
     * Create a OwoSoundInstance from a vanilla SoundEvent.
     * @param event The SoundEvent to wrap.
     * @param category The category of the sound.
     * @return An OwoSoundInstance
     */
    public static OwoSoundInstance from(SoundEvent event, SoundCategory category, float volume, float pitch) {
        return new OwoSoundInstance(OwoSoundEvent.from(event, category), volume, pitch);
    }

    /**
     * Play the OwoSoundInstance.
     * @param client The current minecraft client.
     */
    public void play(MinecraftClient client) {
        client.getSoundManager().play(this);
    }

    /**
     * Play the OwoSoundInstance.
     * @param client The current minecraft client.
     * @param delay The delay, in ticks, until the sound is played.
     */
    public void play(MinecraftClient client, int delay) {
        client.getSoundManager().play(this, delay);
    }

    // Utils

    @Override
    public String toString() {
        return "OwoSoundInstance{" +
                "sound=" + sound +
                ", category=" + category +
                ", id=" + id +
                ", volume=" + volume +
                ", pitch=" + pitch +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", repeat=" + repeat +
                ", repeatDelay=" + repeatDelay +
                ", attenuationType=" + attenuationType +
                ", relative=" + relative +
                '}';
    }

    // Getters and setters

    @Override
    public Sound getSound() {
        return this.sound;
    }
    @Override
    public SoundCategory getCategory() {
        return this.category;
    }
    @Override
    public boolean isRepeatable() {
        return this.repeat;
    }
    @Override
    public int getRepeatDelay() {
        return this.repeatDelay;
    }
    @Override
    public float getVolume() {
        return this.volume * this.sound.getVolume();
    }
    @Override
    public float getPitch() {
        return this.pitch * this.sound.getPitch();
    }
    @Override
    public double getX() {
        return this.x;
    }
    @Override
    public double getY() {
        return this.y;
    }
    @Override
    public double getZ() {
        return this.z;
    }
    @Override
    public AttenuationType getAttenuationType() {
        return this.attenuationType;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public WeightedSoundSet getSoundSet(SoundManager soundManager) {
        WeightedSoundSet weightedSoundSet = soundManager.get(this.id);
        if (weightedSoundSet == null) this.sound = SoundManager.MISSING_SOUND;
        else this.sound = weightedSoundSet.getSound();
        return weightedSoundSet;
    }

    public boolean isRelative() {
        return this.relative;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public void setRepeatDelay(int repeatDelay) {
        this.repeatDelay = repeatDelay;
    }

    public void setAttenuationType(AttenuationType attenuationType) {
        this.attenuationType = attenuationType;
    }

    public void setRelative(boolean relative) {
        this.relative = relative;
    }

}
