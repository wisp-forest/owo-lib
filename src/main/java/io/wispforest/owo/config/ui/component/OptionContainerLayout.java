package io.wispforest.owo.config.ui.component;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

// TODO move this to UI
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
    protected boolean expanded;

    protected final SpinnyBoiComponent spinnyBoi;
    protected final FlowLayout titleLayout;
    protected final Text title;

    public OptionContainerLayout(Text title, boolean expanded) {
        super(Sizing.fill(100), Sizing.content());
        this.surface(SURFACE);
        this.padding(Insets.left(15));

        this.titleLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        this.titleLayout.padding(Insets.vertical(5));
        this.titleLayout.margins(Insets.left(-7));
        this.allowOverflow(true);

        this.spinnyBoi = new SpinnyBoiComponent();
        this.titleLayout.child(spinnyBoi);

        this.title = title.copy().formatted(Formatting.UNDERLINE);
        this.titleLayout.child(Components.label(this.title).cursorStyle(CursorStyle.HAND));

        this.expanded = expanded;
        this.spinnyBoi.rotation(expanded ? 90 : 0);

        super.child(this.titleLayout);
    }

    protected void toggleExpansion() {
        if (expanded) {
            this.children.removeAll(collapsingChildren);
            this.spinnyBoi.spin(0);
        } else {
            this.children.addAll(this.collapsingChildren);
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

            super.onKeyPress(keyCode, scanCode, modifiers);
            return true;
        }

        return super.onKeyPress(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (mouseY <= this.titleLayout.fullSize().height()) {
            this.toggleExpansion();
            UISounds.playInteractionSound();

            super.onMouseDown(mouseX, mouseY, button);
            return true;
        } else {
            return super.onMouseDown(mouseX, mouseY, button);
        }
    }

    @Override
    public FlowLayout child(Component child) {
        this.collapsingChildren.add(child);

        if (this.expanded) {
            super.child(child);
        }
        return this;
    }

    @Override
    public FlowLayout removeChild(Component child) {
        this.collapsingChildren.remove(child);
        return super.removeChild(child);
    }
}
