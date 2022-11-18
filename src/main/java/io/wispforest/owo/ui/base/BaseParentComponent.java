package io.wispforest.owo.ui.base;

import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.FocusHandler;
import io.wispforest.owo.ui.util.ScissorStack;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The reference implementation of the {@link ParentComponent} interface,
 * serving as a base for all parent components on owo-ui. If you need your own parent
 * component, it is often beneficial to subclass one of owo-ui's existing layout classes,
 * especially {@link io.wispforest.owo.ui.container.WrappingParentComponent} is often useful
 */
public abstract class BaseParentComponent extends BaseComponent implements ParentComponent {

    protected Observable<VerticalAlignment> verticalAlignment = Observable.of(VerticalAlignment.TOP);
    protected Observable<HorizontalAlignment> horizontalAlignment = Observable.of(HorizontalAlignment.LEFT);

    protected AnimatableProperty<Insets> padding = AnimatableProperty.of(Insets.none());

    protected @Nullable FocusHandler focusHandler = null;
    protected @Nullable ArrayList<Runnable> taskQueue = null;

    protected Surface surface = Surface.BLANK;
    protected boolean allowOverflow = false;

    protected BaseParentComponent(Sizing horizontalSizing, Sizing verticalSizing) {
        this.horizontalSizing.set(horizontalSizing);
        this.verticalSizing.set(verticalSizing);

        Observable.observeAll(this::updateLayout, horizontalAlignment, verticalAlignment, padding);
    }

    @Override
    public final void update(float delta, int mouseX, int mouseY) {
        ParentComponent.super.update(delta, mouseX, mouseY);
        super.update(delta, mouseX, mouseY);
        this.parentUpdate(delta, mouseX, mouseY);

        if (this.taskQueue != null) {
            this.taskQueue.forEach(Runnable::run);
            this.taskQueue.clear();
        }
    }

    /**
     * Update the state of this component before drawing
     * the next frame. This method is separated from
     * {@link #update(float, int, int)} to enforce the task
     * queue always being run last
     *
     * @param delta  The duration of the last frame, in partial ticks
     * @param mouseX The mouse pointer's x-coordinate
     * @param mouseY The mouse pointer's y-coordinate
     */
    protected void parentUpdate(float delta, int mouseX, int mouseY) {}

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        this.surface.draw(matrices, this);
    }

    @Override
    public void queue(Runnable task) {
        if (this.taskQueue == null) {
            this.parent.queue(task);
        } else {
            this.taskQueue.add(task);
        }
    }

    @Override
    public @Nullable FocusHandler focusHandler() {
        if (this.focusHandler == null) {
            return super.focusHandler();
        } else {
            return this.focusHandler;
        }
    }

    @Override
    public ParentComponent verticalAlignment(VerticalAlignment alignment) {
        this.verticalAlignment.set(alignment);
        return this;
    }

    @Override
    public VerticalAlignment verticalAlignment() {
        return this.verticalAlignment.get();
    }

    @Override
    public ParentComponent horizontalAlignment(HorizontalAlignment alignment) {
        this.horizontalAlignment.set(alignment);
        return this;
    }

    @Override
    public HorizontalAlignment horizontalAlignment() {
        return this.horizontalAlignment.get();
    }

    @Override
    public ParentComponent padding(Insets padding) {
        this.padding.set(padding);
        this.updateLayout();
        return this;
    }

    @Override
    public AnimatableProperty<Insets> padding() {
        return this.padding;
    }

    @Override
    public ParentComponent allowOverflow(boolean allowOverflow) {
        this.allowOverflow = allowOverflow;
        return this;
    }

    @Override
    public boolean allowOverflow() {
        return this.allowOverflow;
    }

    @Override
    public ParentComponent surface(Surface surface) {
        this.surface = surface;
        return this;
    }

    @Override
    public Surface surface() {
        return this.surface;
    }

    @Override
    public void mount(ParentComponent parent, int x, int y) {
        super.mount(parent, x, y);
        if (parent == null && this.focusHandler == null) {
            this.focusHandler = new FocusHandler(this);
            this.taskQueue = new ArrayList<>();
        }
    }

    @Override
    public void inflate(Size space) {
        this.space = space;

        for (var child : this.children()) {
            child.dismount(DismountReason.LAYOUT_INFLATION);
        }

        super.inflate(space);
        this.layout(space);
        super.inflate(space);
    }

    protected void updateLayout() {
        if (!this.mounted) return;

        if (this.batchedEvents > 0) {
            this.batchedEvents++;
            return;
        }

        var previousSize = this.fullSize();
        this.inflate(this.space);

        if (!previousSize.equals(this.fullSize()) && this.parent != null) {
            this.parent.onChildMutated(this);
        }
    }

    @Override
    protected void runAndDeferEvents(Runnable action) {
        try {
            this.batchedEvents = 1;
            action.run();
        } finally {
            if (this.batchedEvents > 1) {
                this.batchedEvents = 0;
                this.updateLayout();
            } else {
                this.batchedEvents = 0;
            }
        }
    }

    @Override
    public void onChildMutated(Component child) {
        this.updateLayout();
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        if (this.focusHandler != null) {
            this.focusHandler.updateClickFocus(this.x + mouseX, this.y + mouseY);
        }

        return ParentComponent.super.onMouseDown(mouseX, mouseY, button)
                || super.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    public boolean onMouseUp(double mouseX, double mouseY, int button) {
        if (this.focusHandler != null && this.focusHandler.focused() != null) {
            final var focused = this.focusHandler.focused();
            return focused.onMouseUp(this.x + mouseX - focused.x(), this.y + mouseY - focused.y(), button);
        } else {
            return super.onMouseUp(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        return ParentComponent.super.onMouseScroll(mouseX, mouseY, amount) || super.onMouseScroll(mouseX, mouseY, amount);
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        if (this.focusHandler != null && this.focusHandler.focused() != null) {
            final var focused = this.focusHandler.focused();
            return focused.onMouseDrag(this.x + mouseX - focused.x(), this.y + mouseY - focused.y(), deltaX, deltaY, button);
        } else {
            return super.onMouseDrag(mouseX, mouseY, deltaX, deltaY, button);
        }
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if (this.focusHandler == null) return false;

        if (keyCode == GLFW.GLFW_KEY_TAB) {
            this.focusHandler.cycle((modifiers & GLFW.GLFW_MOD_SHIFT) == 0);
        } else if ((keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_UP)
                && (modifiers & GLFW.GLFW_MOD_ALT) != 0) {
            this.focusHandler.moveFocus(keyCode);
        } else if (this.focusHandler.focused() != null) {
            return this.focusHandler.focused().onKeyPress(keyCode, scanCode, modifiers);
        }

        return super.onKeyPress(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean onCharTyped(char chr, int modifiers) {
        if (this.focusHandler == null) return false;

        if (this.focusHandler.focused() != null) {
            return this.focusHandler.focused().onCharTyped(chr, modifiers);
        }

        return super.onCharTyped(chr, modifiers);
    }

    @Override
    public void updateX(int x) {
        int offset = x - this.x;
        super.updateX(x);

        for (var child : this.children()) {
            child.updateX(child.x() + offset);
        }
    }

    @Override
    public void updateY(int y) {
        int offset = y - this.y;
        super.updateY(y);

        for (var child : this.children()) {
            child.updateY(child.y() + offset);
        }
    }

    /**
     * @return The offset from the origin of this component
     * at which children can start to be mounted. Accumulates
     * padding as well as padding from content sizing
     */
    protected Size childMountingOffset() {
        var padding = this.padding.get();
        return Size.of(padding.left(), padding.top());
    }

    /**
     * Mount a child using the given mounting function if its positioning
     * is equal to {@link Positioning#layout()}, or according to its
     * intrinsic positioning otherwise
     *
     * @param child      The child to mount
     * @param space      The available space for the to expand into
     * @param layoutFunc The mounting function for components which follow the layout
     */
    protected void mountChild(@Nullable Component child, Size space, Consumer<Component> layoutFunc) {
        if (child == null) return;

        final var positioning = child.positioning().get();
        final var componentMargins = child.margins().get();
        final var padding = this.padding.get();

        switch (positioning.type) {
            case LAYOUT -> layoutFunc.accept(child);
            case ABSOLUTE -> {
                child.inflate(space);
                child.mount(
                        this,
                        this.x + positioning.x + componentMargins.left() + padding.left(),
                        this.y + positioning.y + componentMargins.top() + padding.top()
                );
            }
            case RELATIVE -> {
                child.inflate(space);
                child.mount(
                        this,
                        this.x + padding.left() + componentMargins.left() + Math.round((positioning.x / 100f) * (this.width() - child.fullSize().width() - padding.horizontal())),
                        this.y + padding.top() + componentMargins.top() + Math.round((positioning.y / 100f) * (this.height() - child.fullSize().height() - padding.vertical()))
                );
            }
        }
    }

    /**
     * Draw the children of this component along with
     * their focus outline and tooltip, optionally clipping
     * them if {@link #allowOverflow} is {@code false}
     *
     * @param children The list of children to draw
     */
    protected void drawChildren(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta, List<Component> children) {
        if (!this.allowOverflow) {
            var padding = this.padding.get();
            ScissorStack.push(this.x + padding.left(), this.y + padding.top(), this.width - padding.horizontal(), this.height - padding.vertical(), matrices);
        }

        var focusHandler = this.focusHandler();
        for (var child : children) {
            if (!ScissorStack.isVisible(child, matrices)) continue;
            matrices.translate(0, 0, child.zIndex());

            child.draw(matrices, mouseX, mouseY, partialTicks, delta);
            if (focusHandler.lastFocusSource() == FocusSource.KEYBOARD_CYCLE && focusHandler.focused() == child) {
                child.drawFocusHighlight(matrices, mouseX, mouseY, partialTicks, delta);
            }

            matrices.translate(0, 0, -child.zIndex());
        }

        if (!this.allowOverflow) {
            ScissorStack.pop();
        }
    }

    /**
     * Calculate the space for child inflation. If a given axis
     * is content-sized, return the respective value from {@code thisSpace}
     *
     * @param thisSpace The space for layout inflation of this widget
     * @return The available space for child inflation
     */
    protected Size calculateChildSpace(Size thisSpace) {
        final var padding = this.padding.get();

        return Size.of(
                this.horizontalSizing.get().isContent() ? thisSpace.width() - padding.horizontal() : this.width - padding.horizontal(),
                this.verticalSizing.get().isContent() ? thisSpace.height() - padding.vertical() : this.height - padding.vertical()
        );
    }

    @Override
    public BaseParentComponent positioning(Positioning positioning) {
        return (BaseParentComponent) super.positioning(positioning);
    }

    @Override
    public BaseParentComponent margins(Insets margins) {
        return (BaseParentComponent) super.margins(margins);
    }
}
