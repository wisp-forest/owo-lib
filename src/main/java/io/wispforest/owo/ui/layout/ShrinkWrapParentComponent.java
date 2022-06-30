package io.wispforest.owo.ui.layout;

import io.wispforest.owo.ui.BaseParentComponent;
import io.wispforest.owo.ui.definitions.Component;
import io.wispforest.owo.ui.definitions.Size;
import io.wispforest.owo.ui.definitions.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public abstract class ShrinkWrapParentComponent<T extends Component> extends BaseParentComponent {

    protected T child;

    protected ShrinkWrapParentComponent(Sizing horizontalSizing, Sizing verticalSizing, T child) {
        super(horizontalSizing, verticalSizing);
        this.child = child;
    }

    @Override
    protected void applyHorizontalContentSizing(Sizing sizing) {
        this.width = this.child.fullSize().width() + this.padding.get().horizontal();
    }

    @Override
    protected void applyVerticalContentSizing(Sizing sizing) {
        this.height = this.child.fullSize().height() + this.padding.get().vertical();
    }

    @Override
    public void layout(Size space) {
        this.child.inflate(this.calculateChildSpace(space));

        final var padding = this.padding.get();
        this.child.mount(this, this.x + child.margins().get().left() + padding.left(), this.y + child.margins().get().top() + padding.top());
    }

    public ShrinkWrapParentComponent<T> child(T newChild) {
        this.child = newChild;
        this.updateLayout();
        return this;
    }

    public T child() {
        return this.child;
    }

    @Override
    public Collection<Component> children() {
        return Collections.singleton(this.child);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        var child = UIParsing.get(children, "child", e -> e).orElseThrow(() -> new UIModelParsingException("Container declared without child element"));

        var childList = UIParsing.<Element>allChildrenOfType(child, Node.ELEMENT_NODE);
        if (childList.size() != 1) throw new UIModelParsingException("Containers must have exactly one child declared");

        this.child((T) model.parseComponent(Component.class, childList.get(0)));
    }
}
