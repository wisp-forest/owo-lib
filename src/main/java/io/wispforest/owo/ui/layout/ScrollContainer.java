package io.wispforest.owo.ui.layout;

import io.wispforest.owo.ui.definitions.*;
import io.wispforest.owo.ui.parsing.OwoUIParsing;
import io.wispforest.owo.ui.parsing.OwoUISpec;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ScrollContainer<T extends Component> extends ShrinkWrapParentComponent<T> {

    protected double scrollOffset = 0;
    protected double currentScrollPosition = 0;
    protected int lastScrollPosition = -1;

    protected int scrollbarThiccness = 3;
    protected int scrollbarColor = 0xA0000000;

    protected long lastScrollbarInteractTime = 0;
    protected int scrollbarOffset = 0;
    protected boolean scrollbaring = false;

    protected int maxScroll = 0;
    protected int childSize = 0;

    protected final ScrollDirection direction;

    protected ScrollContainer(ScrollDirection direction, Sizing horizontalSizing, Sizing verticalSizing, T child) {
        super(horizontalSizing, verticalSizing, child);
        this.direction = direction;
    }

    public static <T extends Component> ScrollContainer<T> vertical(Sizing horizontalSizing, Sizing verticalSizing, T child) {
        return new ScrollContainer<>(ScrollDirection.VERTICAL, horizontalSizing, verticalSizing, child);
    }

    public static <T extends Component> ScrollContainer<T> horizontal(Sizing horizontalSizing, Sizing verticalSizing, T child) {
        return new ScrollContainer<>(ScrollDirection.HORIZONTAL, horizontalSizing, verticalSizing, child);
    }

    @Override
    protected void applyHorizontalContentSizing(Sizing sizing) {
        if (this.direction == ScrollDirection.VERTICAL) {
            super.applyHorizontalContentSizing(sizing);
        } else {
            throw new UnsupportedOperationException("Horizontal ScrollContainer cannot be horizontally content-sized");
        }
    }

    @Override
    protected void applyVerticalContentSizing(Sizing sizing) {
        if (this.direction == ScrollDirection.HORIZONTAL) {
            super.applyVerticalContentSizing(sizing);
        } else {
            throw new UnsupportedOperationException("Vertical ScrollContainer cannot be vertically content-sized");
        }
    }

    @Override
    public void layout(Size space) {
        super.layout(space);

        this.maxScroll = Math.max(0, this.direction.sizeGetter.apply(child) - (this.direction.sizeGetter.apply(this) - this.direction.insetGetter.apply(this.padding.get())));
        this.scrollOffset = MathHelper.clamp(this.scrollOffset, 0, this.maxScroll + 1);
        this.childSize = this.direction.sizeGetter.apply(this.child);
        this.lastScrollPosition = -1;
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);

        // Update scroll position and update child
        this.currentScrollPosition += (this.scrollOffset - this.currentScrollPosition) * .5 * delta;

        int newScrollPosition = this.direction.coordinateGetter.apply(this) - (int) this.currentScrollPosition;
        if (newScrollPosition != this.lastScrollPosition) {
            this.direction.coordinateSetter.accept(this.child, newScrollPosition + (this.direction == ScrollDirection.VERTICAL ? this.padding.get().top() : this.padding.get().left()));
            this.lastScrollPosition = newScrollPosition;
        }
        // Draw, adding the fractional part of the offset via matrix translation
        matrices.push();

        double visualOffset = -(this.currentScrollPosition % 1d);
        if (visualOffset > 9999999e-7 || visualOffset < .1e-6) visualOffset = 0;

        matrices.translate(this.direction.choose(visualOffset, 0), this.direction.choose(0, visualOffset), 0);
        this.drawClipped(matrices, !this.allowOverflow, () -> this.child.draw(matrices, mouseX, mouseY, partialTicks, delta));

        matrices.pop();

        // -----

        // Highlight the scrollbar if it's being hovered
        if (this.isInScrollbar(mouseX, mouseY) || this.scrollbaring) {
            this.lastScrollbarInteractTime = System.currentTimeMillis() + 1500;
        }

        var padding = this.padding.get();
        int selfSize = this.direction.sizeGetter.apply(this);
        int contentSize = this.direction.sizeGetter.apply(this) - this.direction.insetGetter.apply(padding);

        // Determine the offset of the scrollbar on the
        // *opposite* axis to the one we scroll on
        this.scrollbarOffset = this.direction == ScrollDirection.VERTICAL
                ? this.x + this.width - padding.right() - scrollbarThiccness
                : this.y + this.height - padding.bottom() - scrollbarThiccness;

        double scrollbarLength = ((float) selfSize / this.childSize) * contentSize;
        double scrollbarPosition = (this.currentScrollPosition / this.maxScroll) * (contentSize - scrollbarLength) + padding.top();

        final var progress = Easing.SINE.apply(MathHelper.clamp(this.lastScrollbarInteractTime - System.currentTimeMillis(), 0, 750) / 750f);
        int alpha = (int) (progress * (this.scrollbarColor >>> 24));

        if (this.direction == ScrollDirection.VERTICAL) {
            DrawableHelper.fill(matrices,
                    this.scrollbarOffset,
                    (int) (this.y + scrollbarPosition),
                    this.scrollbarOffset + this.scrollbarThiccness,
                    (int) (this.y + scrollbarPosition + scrollbarLength),
                    alpha << 24 | (this.scrollbarColor & 0xFFFFFF)
            );
        } else {
            DrawableHelper.fill(matrices,
                    (int) (this.x + scrollbarPosition),
                    this.scrollbarOffset,
                    (int) (this.x + scrollbarPosition + scrollbarLength),
                    this.scrollbarOffset + this.scrollbarThiccness,
                    alpha << 24 | (this.scrollbarColor & 0xFFFFFF)
            );
        }
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return true;
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        if (this.child.onMouseScroll(this.x + mouseX - this.child.x(), this.y + mouseY - this.child.y(), amount)) return true;

        this.scrollOffset = MathHelper.clamp(this.scrollOffset - amount * 10, 0, this.maxScroll + 1);
        this.lastScrollbarInteractTime = System.currentTimeMillis() + 1250;
        return true;
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if (keyCode == this.direction.lessKeycode) {
            this.scrollOffset = MathHelper.clamp(this.scrollOffset - 10, 0, this.maxScroll + 1);
        } else if (keyCode == this.direction.moreKeycode) {
            this.scrollOffset = MathHelper.clamp(this.scrollOffset + 10, 0, this.maxScroll + 1);
        }

        return false;
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        if (!this.scrollbaring && !this.isInScrollbar(this.x + mouseX, this.y + mouseY)) return super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);

        double delta = this.direction.choose(deltaX, deltaY);
        float selfSize = this.direction.sizeGetter.apply(this) - this.direction.insetGetter.apply(this.padding.get());

        this.scrollOffset = MathHelper.clamp(
                this.scrollOffset + delta * ((this.maxScroll + selfSize) / selfSize),
                0,
                this.maxScroll + 1
        );
        this.currentScrollPosition = this.scrollOffset;
        this.scrollbaring = true;

        return true;
    }

    @Override
    public boolean onMouseRelease(double mouseX, double mouseY, int button) {
        this.scrollbaring = false;
        return true;
    }

    @Override
    public @Nullable Component childAt(int x, int y) {
        if (this.isInScrollbar(x, y)) {
            return this;
        } else {
            return super.childAt(x, y);
        }
    }

    protected boolean isInScrollbar(double mouseX, double mouseY) {
        return this.isInBoundingBox(mouseX, mouseY) && this.direction.choose(mouseY, mouseX) >= this.scrollbarOffset;
    }

    public ScrollContainer<T> scrollbarThiccness(int scrollbarThiccness) {
        this.scrollbarThiccness = scrollbarThiccness;
        return this;
    }

    public int scrollbarThiccness() {
        return this.scrollbarThiccness;
    }

    public ScrollContainer<T> scrollbarColor(int scrollbarColor) {
        this.scrollbarColor = scrollbarColor;
        return this;
    }

    public int scrollbarColor() {
        return this.scrollbarColor;
    }

    @Override
    public void parseProperties(OwoUISpec spec, Element element, Map<String, Element> children) {
        super.parseProperties(spec, element, children);
        OwoUIParsing.apply(children, "scrollbar-thiccness", OwoUIParsing::parseUnsignedInt, this::scrollbarThiccness);
        OwoUIParsing.apply(children, "scrollbar-color", OwoUIParsing::parseColor, this::scrollbarColor);
    }

    public enum ScrollDirection {
        VERTICAL(Component::height, Component::setY, Component::y, Insets::vertical, GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_DOWN),
        HORIZONTAL(Component::width, Component::setX, Component::x, Insets::horizontal, GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_RIGHT);

        public final Function<Component, Integer> sizeGetter;
        public final BiConsumer<Component, Integer> coordinateSetter;
        public final Function<ScrollContainer<?>, Integer> coordinateGetter;
        public final Function<Insets, Integer> insetGetter;

        public final int lessKeycode, moreKeycode;

        ScrollDirection(Function<Component, Integer> sizeGetter, BiConsumer<Component, Integer> coordinateSetter, Function<ScrollContainer<?>, Integer> coordinateGetter, Function<Insets, Integer> insetGetter, int lessKeycode, int moreKeycode) {
            this.sizeGetter = sizeGetter;
            this.coordinateSetter = coordinateSetter;
            this.coordinateGetter = coordinateGetter;
            this.insetGetter = insetGetter;
            this.lessKeycode = lessKeycode;
            this.moreKeycode = moreKeycode;
        }

        public double choose(double horizontal, double vertical) {
            return switch (this) {
                case VERTICAL -> vertical;
                case HORIZONTAL -> horizontal;
            };
        }

    }
}
