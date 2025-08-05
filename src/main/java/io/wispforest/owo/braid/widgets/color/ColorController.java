package io.wispforest.owo.braid.widgets.color;

import io.wispforest.owo.braid.core.Listenable;
import io.wispforest.owo.ui.core.Color;
import org.jetbrains.annotations.Nullable;

public class ColorController extends Listenable {

    protected float hue;
    protected float saturation;
    protected float value;

    protected float alpha;

    public ColorController(float hue, float saturation, float value, float alpha) {
        this.hue = hue;
        this.saturation = saturation;
        this.value = value;
        this.alpha = alpha;
    }

    public ColorController(Color color) {
        this(color.hsv()[0], color.hsv()[1], color.hsv()[2], color.alpha());
    }

    public void set(
        @Nullable Float hue, @Nullable Float saturation, @Nullable Float value, @Nullable Float alpha
    ) {
        boolean changed = false;

        if (hue != null && this.hue != hue) {
            this.hue = hue;
            changed = true;
        }

        if (saturation != null && this.saturation != saturation) {
            this.saturation = saturation;
            changed = true;
        }

        if (value != null && this.value != value) {
            this.value = value;
            changed = true;
        }

        if (alpha != null && this.alpha != alpha) {
            this.alpha = alpha;
            changed = true;
        }

        if (changed) this.notifyListeners();
    }

    public void setColor(Color color) {
        var hsv = color.hsv();
        this.set(hsv[0], hsv[1], hsv[2], color.alpha());
    }

    public Color getColor() {
        return Color.ofHsv(this.hue, this.saturation, this.value, this.alpha);
    }

    public void setHue(float hue) {
        this.set(hue, null, null, null);
    }

    public float getHue() {
        return this.hue;
    }

    public void setSaturation(float saturation) {
        this.set(null, saturation, null, null);
    }

    public float getSaturation() {
        return this.saturation;
    }

    public void setValue(float value) {
        this.set(null, null, value, null);
    }

    public float getValue() {
        return this.value;
    }

    public void setAlpha(float alpha) {
        this.set(null, null, null, alpha);
    }

    public float getAlpha() {
        return this.alpha;
    }
}
