package io.wispforest.owo.config.ui;

import io.wispforest.owo.ui.Drawer;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.definitions.Component;
import io.wispforest.owo.ui.definitions.Insets;
import io.wispforest.owo.ui.definitions.Sizing;
import io.wispforest.owo.ui.definitions.Surface;
import io.wispforest.owo.ui.layout.FlowLayout;
import io.wispforest.owo.ui.layout.Layouts;
import io.wispforest.owo.ui.layout.VerticalFlowLayout;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class OptionContainerLayout extends VerticalFlowLayout {

    public static final Surface SURFACE = (matrices, component) -> {
        Drawer.fill(matrices,
                component.x() + 5,
                component.y(),
                component.x() + 6,
                component.y() + component.height(),
                0x77FFFFFF
        );
    };

    protected List<Component> collapsingChildren = new ArrayList<>();
    protected boolean expanded = true;

    protected final SpinnyBoiComponent spinnyBoi;
    protected final FlowLayout titleLayout;
    protected final Text title;

    protected OptionContainerLayout(Text title) {
        super(Sizing.fill(100), Sizing.content());
        this.surface(SURFACE);
        this.padding(Insets.left(10));

        this.titleLayout = Layouts.horizontalFlow(Sizing.content(), Sizing.content());
        this.titleLayout.padding(Insets.vertical(5));

        this.spinnyBoi = new SpinnyBoiComponent();
        this.titleLayout.child(spinnyBoi);

        this.title = title;
        this.titleLayout.child(Components.label(this.title));

        super.child(this.titleLayout);
    }

    protected void toggleExpansion() {
        if (expanded) {
            this.children.removeAll(collapsingChildren);
            this.spinnyBoi.spin(0);
        } else {
            for (var child : this.collapsingChildren) this.children.add(0, child);
            this.spinnyBoi.spin(90);
        }
        this.updateLayout();

        this.expanded = !this.expanded;
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return source == FocusSource.KEYBOARD_CYCLE;
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.toggleExpansion();
            return true;
        }

        return super.onKeyPress(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button) {
        if (mouseY <= this.titleLayout.fullSize().height()) {
            this.toggleExpansion();
            return true;
        }

        return super.onMouseClick(mouseX, mouseY, button);
    }

    @Override
    public FlowLayout child(Component child) {
        this.collapsingChildren.add(child);
        return super.child(child);
    }
}
