package io.wispforest.owo.ui.container;

import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.util.math.MatrixStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.*;

public class GridLayout extends BaseParentComponent {

    protected final int rows, columns;

    protected final Component[] children;
    protected final List<Component> nonNullChildren = new ArrayList<>();

    protected Size contentSize = Size.zero();

    protected GridLayout(Sizing horizontalSizing, Sizing verticalSizing, int rows, int columns) {
        super(horizontalSizing, verticalSizing);

        this.rows = rows;
        this.columns = columns;

        this.children = new Component[rows * columns];
    }

    @Override
    protected void applyHorizontalContentSizing(Sizing sizing) {
        this.width = this.contentSize.width() + this.padding.get().right() + sizing.value;
    }

    @Override
    protected void applyVerticalContentSizing(Sizing sizing) {
        this.height = this.contentSize.height() + this.padding.get().bottom() + sizing.value;
    }

    @Override
    public void layout(Size space) {
        int[] columnSizes = new int[this.columns];
        int[] rowSizes = new int[this.rows];

        var childSpace = this.calculateChildSpace(space);
        for (var child : this.children) {
            if (child != null) {
                child.inflate(childSpace);
            }
        }

        this.determineSizes(columnSizes, false);
        this.determineSizes(rowSizes, true);

        var mountingOffset = this.childMountingOffset();
        var layoutX = new MutableInt(this.x + mountingOffset.width());
        var layoutY = new MutableInt(this.y + mountingOffset.height());

        for (int row = 0; row < this.rows; row++) {
            layoutX.setValue(this.x + mountingOffset.width());

            for (int column = 0; column < this.columns; column++) {
                int columnSize = columnSizes[column];
                int rowSize = rowSizes[row];

                this.mountChild(this.getChild(row, column), childSpace, child -> {
                    child.mount(
                            this,
                            layoutX.intValue() + child.margins().get().left() + this.horizontalAlignment().align(child.fullSize().width(), columnSize),
                            layoutY.intValue() + child.margins().get().top() + this.verticalAlignment().align(child.fullSize().height(), rowSize)
                    );
                });


                layoutX.add(columnSizes[column]);
            }

            layoutY.add(rowSizes[row]);
        }

        this.contentSize = Size.of(layoutX.intValue() - this.x, layoutY.intValue() - this.y);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);

        this.drawClipped(matrices, !this.allowOverflow, () -> {
            for (int i = this.nonNullChildren.size() - 1; i >= 0; i--) {
                this.nonNullChildren.get(i).draw(matrices, mouseX, mouseY, partialTicks, delta);
            }
        });
    }

    protected @Nullable Component getChild(int row, int column) {
        return this.children[row * this.columns + column];
    }

    protected void determineSizes(int[] sizes, boolean rows) {
        if ((rows ? this.verticalSizing : this.horizontalSizing).get().method != Sizing.Method.CONTENT) {
            Arrays.fill(sizes, (rows ? this.height - this.padding().get().vertical() : this.width - this.padding().get().horizontal()) / (rows ? this.rows : this.columns));
        } else {
            for (int row = 0; row < this.rows; row++) {
                for (int column = 0; column < this.columns; column++) {
                    final var child = this.getChild(row, column);
                    if (child == null) continue;

                    if (rows) {
                        sizes[row] = Math.max(sizes[row], child.fullSize().height());
                    } else {
                        sizes[column] = Math.max(sizes[column], child.fullSize().width());
                    }
                }
            }
        }
    }

    public GridLayout child(Component child, int row, int column) {
        var previousChild = this.getChild(row, column);
        this.children[row * this.columns + column] = child;

        if (previousChild != child) {
            this.nonNullChildren.remove(previousChild);
            this.nonNullChildren.add(0, child);

            this.updateLayout();
        }

        return this;
    }

    public GridLayout removeChild(int row, int column) {
        var currentChild = getChild(row, column);
        if (currentChild != null) {
            currentChild.onDismounted(DismountReason.REMOVED);

            this.nonNullChildren.remove(currentChild);
            this.updateLayout();
        }

        return this;
    }

    @Override
    public Collection<Component> children() {
        return this.nonNullChildren;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        final var components = UIParsing
                .get(children, "children", e -> UIParsing.<Element>allChildrenOfType(e, Node.ELEMENT_NODE))
                .orElse(Collections.emptyList());

        for (var child : components) {
            UIParsing.expectAttributes(child, "row", "column");

            int row = UIParsing.parseUnsignedInt(child.getAttributeNode("row"));
            int column = UIParsing.parseUnsignedInt(child.getAttributeNode("column"));

            final var existingChild = this.getChild(row, column);
            if (existingChild != null) {
                throw new UIModelParsingException("Tried to populate cell " + row + "," + column + " in grid layout twice. " +
                        "Present component: " + existingChild.getClass().getSimpleName() + "\nNew element: " + child.getNodeName());
            }

            this.child(model.parseComponent(Component.class, child), row, column);
        }
    }

    public static GridLayout parse(Element element) {
        UIParsing.expectAttributes(element, "rows", "columns");

        int rows = UIParsing.parseSignedInt(element.getAttributeNode("rows"));
        int columns = UIParsing.parseSignedInt(element.getAttributeNode("columns"));

        return new GridLayout(Sizing.content(), Sizing.content(), rows, columns);
    }
}
