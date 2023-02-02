package io.wispforest.owo.ui.container;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.MountingHelper;
import net.minecraft.client.util.math.MatrixStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.*;

public class FlowLayout extends BaseParentComponent {

    protected final List<Component> children = new ArrayList<>();
    protected final List<Component> childrenView = Collections.unmodifiableList(this.children);
    protected final Algorithm algorithm;

    protected Size contentSize = Size.zero();
    protected int gap = 0;

    protected FlowLayout(Sizing horizontalSizing, Sizing verticalSizing, Algorithm algorithm) {
        super(horizontalSizing, verticalSizing);
        this.algorithm = algorithm;
    }

    /**
     * @deprecated Use {@link FlowLayout#FlowLayout(Sizing, Sizing, Algorithm)} instead
     * and provide the proper layout algorithm
     */
    @Deprecated(forRemoval = true)
    protected FlowLayout(Sizing horizontalSizing, Sizing verticalSizing) {
        this(horizontalSizing, verticalSizing, container -> {
            throw new IllegalStateException("Deprecated FlowLayout constructor used, no layout algorithm provided");
        });

        Owo.debugWarn(Owo.LOGGER, "Deprecated FlowLayout constructor invoked without providing a layout algorithm, this will crash");
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
        this.algorithm.layout(this);
    }

    /**
     * Add a single child to this layout. If you need to add multiple
     * children, use {@link #children(Collection)} instead
     *
     * @param child The child to append to this layout
     */
    public FlowLayout child(Component child) {
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
    public FlowLayout children(Collection<Component> children) {
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
    public FlowLayout child(int index, Component child) {
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
    public FlowLayout children(int index, Collection<Component> children) {
        this.children.addAll(index, children);
        this.updateLayout();
        return this;
    }

    @Override
    public FlowLayout removeChild(Component child) {
        if (this.children.remove(child)) {
            child.dismount(DismountReason.REMOVED);
            this.updateLayout();
        }

        return this;
    }

    /**
     * Remove all children from this layout
     */
    public FlowLayout clearChildren() {
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

    /**
     * Set the gap, in logical pixels, this layout
     * should insert between all child components
     */
    public FlowLayout gap(int gap) {
        int oldGap = this.gap;
        if (oldGap != gap) {
            this.gap = gap;
            this.updateLayout();
        }
        return this;
    }

    /**
     * @return The gap, in logical pixels, this layout
     * inserts between all child components
     */
    public int gap() {
        return this.gap;
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
        this.drawChildren(matrices, mouseX, mouseY, partialTicks, delta, this.children);
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        UIParsing.apply(children, "gap", UIParsing::parseSignedInt, this::gap);

        final var components = UIParsing
                .get(children, "children", e -> UIParsing.<Element>allChildrenOfType(e, Node.ELEMENT_NODE))
                .orElse(Collections.emptyList());

        for (var child : components) {
            this.child(model.parseComponent(Component.class, child));
        }
    }

    public static FlowLayout parse(Element element) {
        UIParsing.expectAttributes(element, "direction");

        return element.getAttribute("direction").equals("vertical")
                ? Containers.verticalFlow(Sizing.content(), Sizing.content())
                : Containers.horizontalFlow(Sizing.content(), Sizing.content());
    }

    @FunctionalInterface
    public interface Algorithm {
        void layout(FlowLayout container);

        Algorithm HORIZONTAL = container -> {
            var layoutWidth = new MutableInt(0);
            var layoutHeight = new MutableInt(0);

            final var layout = new ArrayList<Component>();
            final var padding = container.padding.get();
            final var childSpace = container.calculateChildSpace(container.space);

            var mountState = MountingHelper.mountEarly(container::mountChild, container.children, childSpace, child -> {
                layout.add(child);

                child.inflate(childSpace);
                child.mount(container,
                        container.x + padding.left() + child.margins().get().left() + layoutWidth.intValue(),
                        container.y + padding.top() + child.margins().get().top());

                final var childSize = child.fullSize();
                layoutWidth.add(childSize.width() + container.gap);
                if (childSize.height() > layoutHeight.intValue()) {
                    layoutHeight.setValue(childSize.height());
                }
            });

            layoutWidth.subtract(container.gap);

            container.contentSize = Size.of(layoutWidth.intValue(), layoutHeight.intValue());
            container.applySizing();

            if (container.verticalAlignment() != VerticalAlignment.TOP) {
                for (var component : layout) {
                    component.updateY(component.y() + container.verticalAlignment().align(component.fullSize().height(), container.height - padding.vertical()));
                }
            }

            if (container.horizontalAlignment() != HorizontalAlignment.LEFT) {
                for (var component : layout) {
                    if (container.horizontalAlignment() == HorizontalAlignment.CENTER) {
                        component.updateX(component.x() + (container.width - padding.horizontal() - layoutWidth.intValue()) / 2);
                    } else {
                        component.updateX(component.x() + (container.width - padding.horizontal() - layoutWidth.intValue()));
                    }
                }
            }

            mountState.mountLate();
        };

        Algorithm VERTICAL = container -> {
            var layoutHeight = new MutableInt(0);
            var layoutWidth = new MutableInt(0);

            final var layout = new ArrayList<Component>();
            final var padding = container.padding.get();
            final var childSpace = container.calculateChildSpace(container.space);

            var mountState = MountingHelper.mountEarly(container::mountChild, container.children, childSpace, child -> {
                layout.add(child);

                child.inflate(childSpace);
                child.mount(container,
                        container.x + padding.left() + child.margins().get().left(),
                        container.y + padding.top() + child.margins().get().top() + layoutHeight.intValue());

                final var childSize = child.fullSize();
                layoutHeight.add(childSize.height() + container.gap);
                if (childSize.width() > layoutWidth.intValue()) {
                    layoutWidth.setValue(childSize.width());
                }
            });

            layoutHeight.subtract(container.gap);

            container.contentSize = Size.of(layoutWidth.intValue(), layoutHeight.intValue());
            container.applySizing();

            if (container.horizontalAlignment() != HorizontalAlignment.LEFT) {
                for (var component : layout) {
                    component.updateX(component.x() + container.horizontalAlignment().align(component.fullSize().width(), container.width - padding.horizontal()));
                }
            }

            if (container.verticalAlignment() != VerticalAlignment.TOP) {
                for (var component : layout) {
                    if (container.verticalAlignment() == VerticalAlignment.CENTER) {
                        component.updateY(component.y() + (container.height - padding.vertical() - layoutHeight.intValue()) / 2);
                    } else {
                        component.updateY(component.y() + (container.height - padding.vertical() - layoutHeight.intValue()));
                    }
                }
            }

            mountState.mountLate();
        };
    }
}
