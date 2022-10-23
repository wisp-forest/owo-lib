package io.wispforest.owo.ui.container;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.ui.util.OwoNinePatchRenderers;
import io.wispforest.owo.util.ReflectionUtils;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ScrollContainer<C extends Component> extends WrappingParentComponent<C> {

    protected double scrollOffset = 0;
    protected double currentScrollPosition = 0;
    protected int lastScrollPosition = -1;
    protected int scrollStep = 0;

    protected int fixedScrollbarLength = 0;
    protected double lastScrollbarLength = 0;

    protected Scrollbar scrollbar = Scrollbar.flat(Color.ofArgb(0xA0000000));
    protected int scrollbarThiccness = 3;

    protected long lastScrollbarInteractTime = 0;
    protected int scrollbarOffset = 0;
    protected boolean scrollbaring = false;

    protected int maxScroll = 0;
    protected int childSize = 0;

    protected final ScrollDirection direction;

    protected ScrollContainer(ScrollDirection direction, Sizing horizontalSizing, Sizing verticalSizing, C child) {
        super(horizontalSizing, verticalSizing, child);
        this.direction = direction;
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
        this.scrollOffset = MathHelper.clamp(this.scrollOffset, 0, this.maxScroll + .5);
        this.childSize = this.direction.sizeGetter.apply(this.child);
        this.lastScrollPosition = -1;
    }

    @Override
    protected int childMountX() {
        return (int) (super.childMountX() - this.direction.choose(this.currentScrollPosition, 0));
    }

    @Override
    protected int childMountY() {
        return (int) (super.childMountY() - this.direction.choose(0, this.currentScrollPosition));
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);

        // Update scroll position and update child
        this.currentScrollPosition += (this.scrollOffset - this.currentScrollPosition) * .5 * delta;

        int effectiveScrollOffset = this.scrollStep > 0
                ? ((int) this.scrollOffset / this.scrollStep) * this.scrollStep
                : (int) this.currentScrollPosition;
        if (this.scrollStep > 0 && this.maxScroll - this.scrollOffset == -1) {
            effectiveScrollOffset += this.scrollOffset % this.scrollStep;
        }

        int newScrollPosition = this.direction.coordinateGetter.apply(this) - effectiveScrollOffset;
        if (newScrollPosition != this.lastScrollPosition) {
            this.direction.coordinateSetter.accept(this.child, newScrollPosition + (this.direction == ScrollDirection.VERTICAL
                    ? this.padding.get().top() + this.child.margins().get().top()
                    : this.padding.get().left() + this.child.margins().get().left())
            );
            this.lastScrollPosition = newScrollPosition;
        }

        // Draw, adding the fractional part of the offset via matrix translation
        matrices.push();

        double visualOffset = -(this.currentScrollPosition % 1d);
        if (visualOffset > 9999999e-7 || visualOffset < .1e-6) visualOffset = 0;

        matrices.translate(this.direction.choose(visualOffset, 0), this.direction.choose(0, visualOffset), 0);
        this.drawChildren(matrices, mouseX, mouseY, partialTicks, delta, Collections.singletonList(this.child));

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

        this.lastScrollbarLength = this.fixedScrollbarLength == 0
                ? Math.min(Math.floor(((float) selfSize / this.childSize) * contentSize), contentSize)
                : this.fixedScrollbarLength;
        double scrollbarPosition = this.maxScroll != 0
                ? (this.currentScrollPosition / this.maxScroll) * (contentSize - this.lastScrollbarLength)
                : 0;

        if (this.direction == ScrollDirection.VERTICAL) {
            this.scrollbar.draw(matrices,
                    this.scrollbarOffset,
                    (int) (this.y + scrollbarPosition + padding.top()),
                    this.scrollbarThiccness,
                    (int) (this.lastScrollbarLength),
                    this.scrollbarOffset, this.y + padding.top(),
                    this.scrollbarThiccness, this.height - padding.vertical(),
                    lastScrollbarInteractTime, this.direction,
                    this.maxScroll > 0
            );
        } else {
            this.scrollbar.draw(matrices,
                    (int) (this.x + scrollbarPosition + padding.left()),
                    this.scrollbarOffset,
                    (int) (this.lastScrollbarLength),
                    this.scrollbarThiccness,
                    this.x + padding.left(), this.scrollbarOffset,
                    this.width - padding.horizontal(), this.scrollbarThiccness,
                    lastScrollbarInteractTime, this.direction,
                    this.maxScroll > 0
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

        if (this.scrollStep < 1) {
            this.scrollBy(-amount * 15, false, true);
        } else {
            this.scrollBy(-amount * this.scrollStep, true, true);
        }

        return true;
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (this.isInScrollbar(this.x + mouseX, this.y + mouseY)) {
            super.onMouseDown(mouseX, mouseY, button);
            return true;
        } else {
            return super.onMouseDown(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        if (!this.scrollbaring && !this.isInScrollbar(this.x + mouseX, this.y + mouseY)) return super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);

        double delta = this.direction.choose(deltaX, deltaY);
        double selfSize = this.direction.sizeGetter.apply(this) - this.direction.insetGetter.apply(this.padding.get());
        double scalar = (this.maxScroll) / (selfSize - this.lastScrollbarLength);
        if (Double.isNaN(scalar)) scalar = 0;

        this.scrollBy(delta * scalar, true, false);
        this.scrollbaring = true;

        return true;
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if (keyCode == this.direction.lessKeycode) {
            this.scrollBy(-10, false, true);
        } else if (keyCode == this.direction.moreKeycode) {
            this.scrollBy(10, false, true);
        } else if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            this.scrollBy(this.direction.choose(this.width, this.height) * .8, false, true);
            this.lastScrollbarInteractTime = System.currentTimeMillis() + 1250;
        } else if (keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            this.scrollBy(this.direction.choose(this.width, this.height) * -.8, false, true);
        }

        return false;
    }

    @Override
    public boolean onMouseUp(double mouseX, double mouseY, int button) {
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

    protected void scrollBy(double offset, boolean instant, boolean showScrollbar) {
        this.scrollOffset = MathHelper.clamp(this.scrollOffset + offset, 0, this.maxScroll + .5);
        if (instant) this.currentScrollPosition = this.scrollOffset;
        if (showScrollbar) this.lastScrollbarInteractTime = System.currentTimeMillis() + 1250;
    }

    protected boolean isInScrollbar(double mouseX, double mouseY) {
        return this.isInBoundingBox(mouseX, mouseY) && this.direction.choose(mouseY, mouseX) >= this.scrollbarOffset;
    }

    public ScrollContainer<C> scrollTo(Component component) {
        this.scrollOffset = MathHelper.clamp(this.scrollOffset - (this.y - component.y() + component.margins().get().top()), 0, this.maxScroll);
        return this;
    }

    public ScrollContainer<C> scrollbarThiccness(int scrollbarThiccness) {
        this.scrollbarThiccness = scrollbarThiccness;
        return this;
    }

    public int scrollbarThiccness() {
        return this.scrollbarThiccness;
    }

    public ScrollContainer<C> scrollbar(Scrollbar scrollbar) {
        this.scrollbar = scrollbar;
        return this;
    }

    public Scrollbar scrollbar() {
        return this.scrollbar;
    }

    public ScrollContainer<C> scrollStep(int scrollStep) {
        this.scrollStep = scrollStep;
        return this;
    }

    public int scrollStep() {
        return this.scrollStep;
    }

    /**
     * Set a fixed length for the scrollbar of this
     * container, {@code 0} for dynamic sizing
     */
    public ScrollContainer<C> fixedScrollbarLength(int fixedScrollbarLength) {
        this.fixedScrollbarLength = fixedScrollbarLength;
        return this;
    }

    public int fixedScrollbarLength() {
        return this.fixedScrollbarLength;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "fixed-scrollbar-length", UIParsing::parseUnsignedInt, this::fixedScrollbarLength);
        UIParsing.apply(children, "scrollbar-thiccness", UIParsing::parseUnsignedInt, this::scrollbarThiccness);
        UIParsing.apply(children, "scrollbar", Scrollbar::parse, this::scrollbar);

        UIParsing.apply(children, "scroll-step", UIParsing::parseUnsignedInt, this::scrollStep);

        UIParsing.apply(children, "scrollbar-color", Color::parseAndPack, integer -> {
            Owo.debugWarn(Owo.LOGGER, "A UI model used the deprecated 'scrollbar-color' property. This is superseded by <scrollbar> <flat>{color}</flat> </scrollbar>");
        });
    }

    public static ScrollContainer<?> parse(Element element) {
        return element.getAttribute("direction").equals("vertical")
                ? Containers.verticalScroll(Sizing.content(), Sizing.content(), null)
                : Containers.horizontalScroll(Sizing.content(), Sizing.content(), null);
    }

    @FunctionalInterface
    public interface Scrollbar {

        static Scrollbar flat(Color color) {
            int scrollbarColor = color.argb();

            return (matrices, x, y, width, height, trackX, trackY, trackWidth, trackHeight, lastInteractTime, direction, active) -> {
                if (!active) return;

                final var progress = Easing.SINE.apply(MathHelper.clamp(lastInteractTime - System.currentTimeMillis(), 0, 750) / 750f);
                int alpha = (int) (progress * (scrollbarColor >>> 24));

                DrawableHelper.fill(matrices,
                        x, y, x + width, y + height,
                        alpha << 24 | (scrollbarColor & 0xFFFFFF)
                );
            };
        }

        static Scrollbar vanilla() {
            return (matrices, x, y, width, height, trackX, trackY, trackWidth, trackHeight, lastInteractTime, direction, active) -> {
                OwoNinePatchRenderers.VANILLA_SCROLLBAR_TRACK.draw(matrices, trackX, trackY, trackWidth, trackHeight);

                var renderer = direction == ScrollDirection.VERTICAL
                        ? active ? OwoNinePatchRenderers.VERTICAL_VANILLA_SCROLLBAR : OwoNinePatchRenderers.DISABLED_VERTICAL_VANILLA_SCROLLBAR
                        : active ? OwoNinePatchRenderers.HORIZONTAL_VANILLA_SCROLLBAR : OwoNinePatchRenderers.DISABLED_HORIZONTAL_VANILLA_SCROLLBAR;

                renderer.draw(matrices, x + 1, y + 1, width - 2, height - 2);
            };
        }

        static Scrollbar vanillaFlat() {
            return (matrices, x, y, width, height, trackX, trackY, trackWidth, trackHeight, lastInteractTime, direction, active) -> {
                Drawer.fill(matrices, trackX, trackY, trackX + trackWidth, trackY + trackHeight, Color.BLACK.argb());
                OwoNinePatchRenderers.FLAT_VANILLA_SCROLLBAR.draw(matrices, x, y, width, height);
            };
        }

        void draw(MatrixStack matrixStack, int x, int y, int width, int height, int trackX, int trackY, int trackWidth, int trackHeight,
                  long lastInteractTime, ScrollDirection direction, boolean active);

        static Scrollbar parse(Element element) {
            var children = UIParsing.<Element>allChildrenOfType(element, Node.ELEMENT_NODE);
            if (children.size() > 1) throw new UIModelParsingException("'scrollbar' declaration may only contain a single child");

            var scrollbarElement = children.get(0);
            return switch (scrollbarElement.getNodeName()) {
                case "vanilla" -> vanilla();
                case "vanilla-flat" -> vanillaFlat();
                case "flat" -> flat(Color.parse(scrollbarElement));
                default -> throw new UIModelParsingException("Unknown scrollbar type '" + scrollbarElement.getNodeName() + "'");
            };
        }
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
