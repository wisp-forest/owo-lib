package io.wispforest.owo.config.ui.component;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.SelectableContainer;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Range;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SelectableScrollContainer extends ScrollContainer<FlowLayout> {

    // The amount the user has scrolled
    protected double scrolledAmount = 0;
    protected List<RangedComponentSelection> rangeSections = new ArrayList<>();

    protected SelectableScrollContainer(ScrollDirection direction, Sizing horizontalSizing, Sizing verticalSizing, FlowLayout layout) {
        super(direction, horizontalSizing, verticalSizing, layout);
    }

    @Override
    protected ScrollContainer<FlowLayout> scrollTo(Runnable target) {
        return super.scrollTo(() -> {
            target.run();
            this.scrolledAmount = MathHelper.clamp(this.scrollOffset, 0, this.maxScroll + .5);;
        });
    }

    @Override
    public void layout(Size space) {
        super.layout(space);

        this.lastScrollOffset = -1;

        this.rangeSections.clear();

        Map<Component, Integer> componentToSize = new LinkedHashMap<>();
        int totalChildrenSize = 0;

        for (var component : this.child.children()) {
            var size = (int) this.direction.choose(component.width(), component.height());

            totalChildrenSize += size;
            componentToSize.put(component, size);
        }

        var currentOffset = 0.0;

        for (var entry : componentToSize.entrySet()) {
            var size = entry.getValue();

            var start = currentOffset;
            var end = currentOffset += (size / (float) totalChildrenSize) * this.maxScroll;

            this.rangeSections.add(new RangedComponentSelection(entry.getKey(), com.google.common.collect.Range.closedOpen(start, end)));
        }

        this.scrolledAmount = scrollOffset;
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if (this.targetComponent != null && keyCode == GLFW.GLFW_KEY_ENTER) {
            this.focusHandler().focus(this.targetComponent, FocusSource.KEYBOARD_CYCLE);

            return this.targetComponent.onKeyPress(keyCode, scanCode, modifiers);
        }

        return super.onKeyPress(keyCode, scanCode, modifiers);
    }

    @Override
    protected double scrolledAmount() {
        return this.scrolledAmount;
    }

    @Override
    protected double mouseScrollStepAmount() {
        return 1;
    }

    private Component targetComponent = null;

    @Override
    protected void scrollBy(double offset, boolean instant, boolean showScrollbar, boolean scrollStepped) {
        this.scrolledAmount = MathHelper.clamp(this.scrolledAmount + offset, 0, this.maxScroll + .5);

        int i = 0;

        for (var rangeSection : this.rangeSections) {
            if (rangeSection.range().contains(this.scrolledAmount)) {
                i += (scrollStepped ? (int) Math.signum(offset) : 0);

                break;
            } else {
                i++;
            }
        }

        i = Math.clamp(i, 0, this.rangeSections.size() - 1);

        var section = this.rangeSections.get(i);

        var component = section.component();

        if (this.targetComponent instanceof SelectableContainer<?> container) {
            container.setSelected(false);
        }

        this.targetComponent = component;

        if (this.targetComponent instanceof SelectableContainer<?> container) {
            container.setSelected(true);
        }

        var amount = this.direction.choose(
                (this.x /*+ (this.width / 2)*/) - (component.x() - (component.width() / 2)) + component.margins().get().right(),
                (this.y /*+ (this.height / 2)*/) - (component.y() - (component.height() / 2)) + component.margins().get().top());

        this.scrollOffset = MathHelper.clamp(this.scrollOffset - amount, 0, this.maxScroll);

        if (scrollStepped) {
            this.scrolledAmount = (i == this.rangeSections.size() - 1)
                    ? section.range().upperEndpoint() + 0.5
                    : section.range().lowerEndpoint();
        }

        scrollByPost(true, showScrollbar);
    }

    private record RangedComponentSelection(Component component, com.google.common.collect.Range<Double> range){};

    public static SelectableScrollContainer parse(Element element) {
        return element.getAttribute("direction").equals("vertical")
                ? new SelectableScrollContainer(ScrollDirection.VERTICAL, Sizing.content(), Sizing.content(), null)
                : new SelectableScrollContainer(ScrollDirection.HORIZONTAL, Sizing.content(), Sizing.content(), null);
    }

}
