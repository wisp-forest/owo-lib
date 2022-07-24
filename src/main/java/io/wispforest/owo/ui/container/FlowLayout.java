package io.wispforest.owo.ui.container;

import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.util.math.MatrixStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class FlowLayout extends BaseParentComponent {

    protected final List<Component> children = new ArrayList<>();
    protected final List<Component> childrenView = Collections.unmodifiableList(this.children);
    protected Size contentSize = Size.zero();

    protected FlowLayout(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing);
    }

    @Override
    protected void applyHorizontalContentSizing(Sizing sizing) {
        this.width = this.contentSize.width() + this.padding.get().horizontal() + sizing.value * 2;
    }

    @Override
    protected void applyVerticalContentSizing(Sizing sizing) {
        this.height = this.contentSize.height() + this.padding.get().vertical() + sizing.value * 2;
    }

    public FlowLayout child(Component child) {
        this.children.add(child);
        this.updateLayout();
        return this;
    }

    public FlowLayout removeChild(Component child) {
        if (this.children.remove(child)) {
            child.dismount(DismountReason.REMOVED);
            this.updateLayout();
        }

        return this;
    }

    @Override
    public List<Component> children() {
        return this.childrenView;
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
        this.drawClipped(matrices, mouseX, mouseY, partialTicks, delta, this.children);
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        final var components = UIParsing
                .get(children, "children", e -> UIParsing.<Element>allChildrenOfType(e, Node.ELEMENT_NODE))
                .orElse(Collections.emptyList());

        for (var child : components) {
            this.child(model.parseComponent(Component.class, child));
        }
    }

    public static FlowLayout parse(Element element) {
        return element.getAttribute("direction").equals("vertical")
                ? Containers.verticalFlow(Sizing.content(), Sizing.content())
                : Containers.horizontalFlow(Sizing.content(), Sizing.content());
    }
}
