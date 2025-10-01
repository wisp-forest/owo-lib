package io.wispforest.owo.braid.widgets.window;

import io.wispforest.owo.braid.core.Listenable;
import io.wispforest.owo.braid.core.Size;

public class WindowController extends Listenable {
    private double x = 0;
    private double y = 0;
    private Size size = Size.zero();

    private boolean collapsed = false;

    public void setX(double x) {
        this.x = x;
        this.notifyListeners();
    }

    public double x() {
        return this.x;
    }

    public void setY(double y) {
        this.y = y;
        this.notifyListeners();
    }

    public double y() {
        return this.y;
    }

    public void setSize(Size size) {
        this.size = size;
        this.notifyListeners();
    }

    public Size size() {
        return this.size;
    }

    public boolean toggleCollapsed() {
        this.setCollapsed(!this.collapsed);
        return this.collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        this.notifyListeners();
    }

    public boolean collapsed() {
        return this.collapsed;
    }
}
