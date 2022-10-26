package io.wispforest.owo.ui.container;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.class_7833;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollapsibleContainer extends VerticalFlowLayout {

    public static final Surface SURFACE = (matrices, component) -> Drawer.fill(matrices,
            component.x() + 5,
            component.y(),
            component.x() + 6,
            component.y() + component.height(),
            0x77FFFFFF
    );

    protected List<Component> collapsibleChildren = new ArrayList<>();
    protected boolean expanded;

    protected final SpinnyBoiComponent spinnyBoi;
    protected final FlowLayout titleLayout;

    protected CollapsibleContainer(Sizing horizontalSizing, Sizing verticalSizing, Text title, boolean expanded) {
        super(horizontalSizing, verticalSizing);
        this.surface(SURFACE);
        this.padding(Insets.left(15));

        this.titleLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        this.titleLayout.padding(Insets.vertical(5));
        this.titleLayout.margins(Insets.left(-7));
        this.allowOverflow(true);

        this.spinnyBoi = new SpinnyBoiComponent();
        this.titleLayout.child(spinnyBoi);

        title = title.copy().formatted(Formatting.UNDERLINE);
        this.titleLayout.child(Components.label(title).cursorStyle(CursorStyle.HAND));

        this.expanded = expanded;
        this.spinnyBoi.targetRotation = expanded ? 90 : 0;
        this.spinnyBoi.rotation = this.spinnyBoi.targetRotation;

        super.child(this.titleLayout);
    }

    protected void toggleExpansion() {
        if (expanded) {
            this.children.removeAll(collapsibleChildren);
            this.spinnyBoi.targetRotation = 0;
        } else {
            this.children.addAll(this.collapsibleChildren);
            this.spinnyBoi.targetRotation = 90;
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
        final var superResult = super.onMouseDown(mouseX, mouseY, button);

        if (mouseY <= this.titleLayout.fullSize().height() && !superResult) {
            this.toggleExpansion();
            UISounds.playInteractionSound();
            return true;
        } else {
            return superResult;
        }
    }

    @Override
    public FlowLayout child(Component child) {
        this.collapsibleChildren.add(child);
        if (this.expanded) super.child(child);
        return this;
    }

    @Override
    public FlowLayout children(Collection<Component> children) {
        this.collapsibleChildren.addAll(children);
        if (this.expanded) super.children(children);
        return this;
    }

    @Override
    public FlowLayout child(int index, Component child) {
        this.collapsibleChildren.add(index, child);
        if (this.expanded) super.child(index + this.children.size() - this.collapsibleChildren.size(), child);
        return this;
    }

    @Override
    public FlowLayout children(int index, Collection<Component> children) {
        this.collapsibleChildren.addAll(index, children);
        if (this.expanded) super.children(index + this.children.size() - this.collapsibleChildren.size(), children);
        return this;
    }

    @Override
    public FlowLayout removeChild(Component child) {
        this.collapsibleChildren.remove(child);
        return super.removeChild(child);
    }

    public static CollapsibleContainer parse(Element element) {
        var textElement = UIParsing.childElements(element).get("text");
        var title = textElement == null ? Text.empty() : UIParsing.parseText(textElement);

        return element.getAttribute("expanded").equals("true")
                ? Containers.collapsible(Sizing.content(), Sizing.content(), title, true)
                : Containers.collapsible(Sizing.content(), Sizing.content(), title, false);
    }

    protected static class SpinnyBoiComponent extends LabelComponent {

        protected float rotation = 90;
        protected float targetRotation = 90;

        public SpinnyBoiComponent() {
            super(Text.literal(">"));
            this.margins(Insets.horizontal(4));
        }

        @Override
        public void update(float delta, int mouseX, int mouseY) {
            super.update(delta, mouseX, mouseY);
            this.rotation += (this.targetRotation - this.rotation) * delta * .65;
        }

        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
            matrices.push();
            matrices.translate(this.x + this.width / 2f - 1, this.y + this.height / 2f - 1, 0);
            matrices.multiply(class_7833.field_40718.rotationDegrees(this.rotation));
            matrices.translate(-(this.x + this.width / 2f - 1), -(this.y + this.height / 2f - 1), 0);

            super.draw(matrices, mouseX, mouseY, partialTicks, delta);
            matrices.pop();
        }
    }
}
