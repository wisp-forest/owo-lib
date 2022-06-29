package io.wispforest.owo.ui;

import io.wispforest.owo.ui.definitions.Component;
import io.wispforest.owo.ui.definitions.ParentComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class FocusHandler {

    protected final ParentComponent root;
    @Nullable protected Component focused = null;

    public FocusHandler(ParentComponent root) {
        this.root = root;
    }

    public void updateClickFocus(double mouseX, double mouseY) {
        var clicked = this.root.childAt((int) mouseX, (int) mouseY);
        this.focus(clicked != null && clicked.canFocus(Component.FocusSource.MOUSE_CLICK) ? clicked : null, Component.FocusSource.MOUSE_CLICK);
    }

    @Contract(pure = true)
    public @Nullable Component focused() {
        return this.focused;
    }

    public void cycle(boolean forwards) {
        var allChildren = new ArrayList<Component>();
        this.root.collectChildren(allChildren);

        allChildren.removeIf(component -> !component.canFocus(Component.FocusSource.KEYBOARD_CYCLE));
        if (allChildren.isEmpty()) return;

        int index = allChildren.indexOf(this.focused) + (forwards ? 1 : -1);
        if (index >= allChildren.size()) index -= allChildren.size();
        if (index < 0) index += allChildren.size();

        this.focus(allChildren.get(index), Component.FocusSource.KEYBOARD_CYCLE);
    }

    public void focus(@Nullable Component component, Component.FocusSource source) {
        if (this.focused != component) {
            if (this.focused != null) {
                this.focused.onFocusLost();
            }

            if ((this.focused = component) != null) {
                this.focused.onFocusGained(source);
            }
        }
    }

}
