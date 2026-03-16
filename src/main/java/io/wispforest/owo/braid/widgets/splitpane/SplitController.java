package io.wispforest.owo.braid.widgets.splitpane;

import io.wispforest.owo.braid.core.Listenable;
import net.minecraft.util.Mth;

public class SplitController extends Listenable {

    double[] ratios;

    public SplitController(int paneCount) {
        this.ratios = new double[paneCount - 1];
        for (var i = 0; i < this.ratios.length; i++) {
            this.ratios[i] = (double) (i + 1) / paneCount;
        }
    }

    public SplitController(double... initialRatios) {
        this.ratios = initialRatios.clone();
    }

    public double getRatio(int index) {
        return this.ratios[index];
    }

    public void setRatio(int index, double ratio) {
        ratio = Mth.clamp(ratio, 0, 1);
        if (this.ratios[index] == ratio) return;
        this.ratios[index] = ratio;
        this.notifyListeners();
    }
}
