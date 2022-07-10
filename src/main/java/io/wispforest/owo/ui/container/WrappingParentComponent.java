package io.wispforest.owo.ui.container;

import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public abstract class WrappingParentComponent<T extends Component> extends BaseParentComponent {

    protected T child;

    protected WrappingParentComponent(Sizing horizontalSizing, Sizing verticalSizing, T child) {
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

    public WrappingParentComponent<T> child(T newChild) {
        if (this.child != null) {
            this.child.onDismounted(DismountReason.REMOVED);
        }

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

        try {
            var childList = UIParsing.<Element>allChildrenOfType(element, Node.ELEMENT_NODE);
            this.child((T) model.parseComponent(Component.class, childList.get(0)));
        } catch (UIModelParsingException exception) {
            throw new UIModelParsingException("Could not initialize container child", exception);
        }
    }
}
