package io.wispforest.owo.ui.layout;

import io.wispforest.owo.ui.BaseParentComponent;
import io.wispforest.owo.ui.definitions.Component;
import io.wispforest.owo.ui.definitions.Size;
import io.wispforest.owo.ui.definitions.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.util.math.MatrixStack;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.*;

public abstract class FlowLayout extends BaseParentComponent {

    protected final List<Component> children = new ArrayList<>();
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

    @Override
    public Collection<Component> children() {
        return this.children;
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);

        this.drawClipped(matrices, !this.allowOverflow, () -> {
            for (var child : children) {
                child.draw(matrices, mouseX, mouseY, partialTicks, delta);
            }
        });
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
}
