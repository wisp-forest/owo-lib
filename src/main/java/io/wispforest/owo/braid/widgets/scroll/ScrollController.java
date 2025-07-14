package io.wispforest.owo.braid.widgets.scroll;

import io.wispforest.owo.braid.core.Listenable;
import net.minecraft.util.math.MathHelper;

public class ScrollController extends Listenable {

    protected double offset = 0;
    protected double maxOffset = 0;

    public void setOffset(double offset) {
        if (this.offset == offset) {
            return;
        }

        this.offset = MathHelper.clamp(offset, 0, this.maxOffset);
        this.notifyListeners();
    }

    public double offset() {
        return this.offset;
    }

    boolean setMaxOffset(double maxOffset) {
        if (this.maxOffset == maxOffset) {
            return false;
        }

        this.maxOffset = maxOffset;
        this.offset = MathHelper.clamp(this.offset, 0, this.maxOffset);

        return true;
    }

    boolean maxOffsetNotificationScheduled = false;
    void sendMaxOffsetNotification() {
        this.notifyListeners();
        this.maxOffsetNotificationScheduled = false;
    }

    public double maxOffset() {
        return this.maxOffset;
    }
}
