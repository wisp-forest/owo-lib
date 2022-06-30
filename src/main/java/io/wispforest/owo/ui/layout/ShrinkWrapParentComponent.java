package io.wispforest.owo.ui.layout;

import io.wispforest.owo.ui.BaseParentComponent;
import io.wispforest.owo.ui.definitions.Component;
import io.wispforest.owo.ui.definitions.Size;
import io.wispforest.owo.ui.definitions.Sizing;
import io.wispforest.owo.ui.parsing.OwoUIParsing;
import io.wispforest.owo.ui.parsing.OwoUISpec;
import io.wispforest.owo.ui.parsing.UIParsingException;
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
    public void parseProperties(OwoUISpec spec, Element element, Map<String, Element> children) {
        super.parseProperties(spec, element, children);
        var child = OwoUIParsing.get(children, "child", e -> e).orElseThrow(() -> new UIParsingException("Container declared without child element"));

        var childList = OwoUIParsing.<Element>allChildrenOfType(child, Node.ELEMENT_NODE);
        if (childList.size() != 1) throw new UIParsingException("Containers must have exactly one child declared");

        this.child((T) spec.parseComponent(Component.class, childList.get(0)));
    }
}
