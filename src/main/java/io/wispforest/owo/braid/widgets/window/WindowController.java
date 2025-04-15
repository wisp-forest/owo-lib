package io.wispforest.owo.braid.widgets.window;

import io.wispforest.owo.braid.core.Size;

public class WindowController {
    public double x = 0;
    public double y = 0;
    public boolean expanded = true;
    public Size size;

    public WindowController(Size size) {
        this.size = size;
    }
}
