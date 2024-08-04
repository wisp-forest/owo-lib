package io.wispforest.owo.ui.container;

import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.MountingHelper;
import org.apache.commons.lang3.mutable.MutableInt;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.*;

public class StackLayout extends BaseParentComponent {

    protected final List<Component> children = new ArrayList<>();
    protected final List<Component> childrenView = Collections.unmodifiableList(this.children);

    protected Size contentSize = Size.zero();

    protected StackLayout(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing);
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return this.contentSize.width() + this.padding.get().horizontal();
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return this.contentSize.height() + this.padding.get().vertical();
    }

    @Override
    public void layout(Size space) {
        var childSpace = this.calculateChildSpace(space);
        this.children.forEach(child -> child.inflate(childSpace));

        var layoutWidth = new MutableInt();
        var layoutHeight = new MutableInt();

        var layout = new ArrayList<Component>();
        var helper = MountingHelper.mountEarly(this::mountChild, this.childrenView, child -> {
            layout.add(child);
            child.mount(this, this.x + this.padding.get().left() + child.margins().get().left(), this.y + this.padding.get().top() + child.margins().get().top());

            var fullChildSize = child.fullSize();
            layoutWidth.setValue(Math.max(layoutWidth.getValue(), fullChildSize.width()));
            layoutHeight.setValue(Math.max(layoutHeight.getValue(), fullChildSize.height()));
        });

        this.contentSize = Size.of(layoutWidth.intValue(), layoutHeight.intValue());
        this.applySizing();

        var horizontalAlignment = this.horizontalAlignment();
        var verticalAlignment = this.verticalAlignment();

        for (var child : layout) {
            child.updateX(child.baseX() + horizontalAlignment.align(child.fullSize().width(), this.width - this.padding.get().horizontal()));
            child.updateY(child.baseY() + verticalAlignment.align(child.fullSize().height(), this.height - this.padding.get().vertical()));
        }

        helper.mountLate();
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);
        this.drawChildren(context, mouseX, mouseY, partialTicks, delta, this.children);
    }

    /**
     * Add a single child to this layout. If you need to add multiple
     * children, use {@link #children(Collection)} instead
     *
     * @param child The child to append to this layout
     */
    public StackLayout child(Component child) {
        this.children.add(child);
        this.updateLayout();
        return this;
    }

    /**
     * Add a collection of children to this layout. If you only need to
     * add a single child to, use {@link #child(Component)} instead
     *
     * @param children The children to add to this layout
     */
    public StackLayout children(Collection<? extends Component> children) {
        this.children.addAll(children);
        this.updateLayout();
        return this;
    }

    /**
     * Insert a single child into this layout. If you need to insert multiple
     * children, use {@link #children(int, Collection)} instead
     *
     * @param index The index at which to insert the child
     * @param child The child to append to this layout
     */
    public StackLayout child(int index, Component child) {
        this.children.add(index, child);
        this.updateLayout();
        return this;
    }

    /**
     * Insert a collection of children into this layout. If you only need to
     * insert a single child to, use {@link #child(int, Component)} instead
     *
     * @param index    The index at which to begin inserting children
     * @param children The children to add to this layout
     */
    public StackLayout children(int index, Collection<? extends Component> children) {
        this.children.addAll(index, children);
        this.updateLayout();
        return this;
    }

    @Override
    public StackLayout removeChild(Component child) {
        if (this.children.remove(child)) {
            child.dismount(DismountReason.REMOVED);
            this.updateLayout();
        }

        return this;
    }

    /**
     * Remove all children from this layout
     */
    public StackLayout clearChildren() {
        for (var child : this.children) {
            child.dismount(DismountReason.REMOVED);
        }

        this.children.clear();
        this.updateLayout();

        return this;
    }

    @Override
    public List<Component> children() {
        return this.childrenView;
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
