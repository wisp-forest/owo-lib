package io.wispforest.owo.ui.base;

import io.wispforest.owo.ui.container.WrappingParentUIComponent;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.FocusHandler;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The reference implementation of the {@link ParentUIComponent} interface,
 * serving as a base for all parent components on owo-ui. If you need your own parent
 * component, it is often beneficial to subclass one of owo-ui's existing layout classes,
 * especially {@link WrappingParentUIComponent} is often useful
 */
public abstract class BaseParentUIComponent extends BaseUIComponent implements ParentUIComponent {

    protected final Observable<VerticalAlignment> verticalAlignment = Observable.of(VerticalAlignment.TOP);
    protected final Observable<HorizontalAlignment> horizontalAlignment = Observable.of(HorizontalAlignment.LEFT);

    protected final AnimatableProperty<Insets> padding = AnimatableProperty.of(Insets.none());

    protected @Nullable FocusHandler focusHandler = null;
    protected @Nullable ArrayList<Runnable> taskQueue = null;

    protected Surface surface = Surface.BLANK;
    protected boolean allowOverflow = false;

    protected BaseParentUIComponent(Sizing horizontalSizing, Sizing verticalSizing) {
        this.horizontalSizing.set(horizontalSizing);
        this.verticalSizing.set(verticalSizing);

        Observable.observeAll(this::updateLayout, horizontalAlignment, verticalAlignment, padding);
    }

    @Override
    public final void update(float delta, int mouseX, int mouseY) {
        ParentUIComponent.super.update(delta, mouseX, mouseY);
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
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        this.surface.draw(graphics, this);
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
    public ParentUIComponent verticalAlignment(VerticalAlignment alignment) {
        this.verticalAlignment.set(alignment);
        return this;
    }

    @Override
    public VerticalAlignment verticalAlignment() {
        return this.verticalAlignment.get();
    }

    @Override
    public ParentUIComponent horizontalAlignment(HorizontalAlignment alignment) {
        this.horizontalAlignment.set(alignment);
        return this;
    }

    @Override
    public HorizontalAlignment horizontalAlignment() {
        return this.horizontalAlignment.get();
    }

    @Override
    public ParentUIComponent padding(Insets padding) {
        this.padding.set(padding);
        this.updateLayout();
        return this;
    }

    @Override
    public AnimatableProperty<Insets> padding() {
        return this.padding;
    }

    @Override
    public ParentUIComponent allowOverflow(boolean allowOverflow) {
        this.allowOverflow = allowOverflow;
        return this;
    }

    @Override
    public boolean allowOverflow() {
        return this.allowOverflow;
    }

    @Override
    public ParentUIComponent surface(Surface surface) {
        this.surface = surface;
        return this;
    }

    @Override
    public Surface surface() {
        return this.surface;
    }

    @Override
    public void mount(ParentUIComponent parent, int x, int y) {
        super.mount(parent, x, y);
        if (parent == null && this.focusHandler == null) {
            this.focusHandler = new FocusHandler(this);
            this.taskQueue = new ArrayList<>();
        }
    }

    @Override
    public void inflate(Size space) {
        if (this.space.equals(space) && !this.dirty) return;
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

        this.dirty = true;
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
    public void onChildMutated(UIComponent child) {
        this.updateLayout();
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        if (this.focusHandler != null) {
            this.focusHandler.updateClickFocus(this.x + click.x(), this.y + click.y());
        }

        return ParentUIComponent.super.onMouseDown(click, doubled)
            || super.onMouseDown(click, doubled);
    }

    @Override
    public boolean onMouseUp(MouseButtonEvent click) {
        if (this.focusHandler != null && this.focusHandler.focused() != null) {
            final var focused = this.focusHandler.focused();
            return focused.onMouseUp(new MouseButtonEvent(this.x + click.x() - focused.x(), this.y + click.y() - focused.y(), click.buttonInfo()));
        } else {
            return super.onMouseUp(click);
        }
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        return ParentUIComponent.super.onMouseScroll(mouseX, mouseY, amount) || super.onMouseScroll(mouseX, mouseY, amount);
    }

    @Override
    public boolean onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
        if (this.focusHandler != null && this.focusHandler.focused() != null) {
            final var focused = this.focusHandler.focused();
            return focused.onMouseDrag(new MouseButtonEvent(this.x + click.x() - focused.x(), this.y + click.y() - focused.y(), click.buttonInfo()), deltaX, deltaY);
        } else {
            return super.onMouseDrag(click, deltaX, deltaY);
        }
    }

    @Override
    public boolean onKeyPress(KeyEvent input) {
        if (this.focusHandler == null) return false;

        if (input.isCycleFocus()) {
            this.focusHandler.cycle(!input.hasShiftDown());
        } else if ((input.isUp() || input.isDown() || input.isLeft() || input.isRight()) && input.hasAltDown()) {
            this.focusHandler.moveFocus(input.key());
        } else if (this.focusHandler.focused() != null) {
            return this.focusHandler.focused().onKeyPress(input);
        }

        return super.onKeyPress(input);
    }

    @Override
    public boolean onCharTyped(CharacterEvent input) {
        if (this.focusHandler == null) return false;

        if (this.focusHandler.focused() != null) {
            return this.focusHandler.focused().onCharTyped(input);
        }

        return super.onCharTyped(input);
    }

    @Override
    public void updateX(int x) {
        int offset = x - this.x;
        super.updateX(x);

        for (var child : this.children()) {
            child.updateX(child.baseX() + offset);
        }
    }

    @Override
    public void updateY(int y) {
        int offset = y - this.y;
        super.updateY(y);

        for (var child : this.children()) {
            child.updateY(child.baseY() + offset);
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
     * @param layoutFunc The mounting function for components which follow the layout
     */
    protected void mountChild(@Nullable UIComponent child, Consumer<UIComponent> layoutFunc) {
        if (child == null) return;

        final var positioning = child.positioning().get();
        final var componentMargins = child.margins().get();
        final var padding = this.padding.get();

        switch (positioning.type) {
            case LAYOUT -> layoutFunc.accept(child);
            case ABSOLUTE -> child.mount(
                this,
                this.x + positioning.x + componentMargins.left() + padding.left(),
                this.y + positioning.y + componentMargins.top() + padding.top()
            );
            case RELATIVE -> child.mount(
                this,
                this.x + padding.left() + componentMargins.left() + Math.round((positioning.x / 100f) * (this.width() - child.fullSize().width() - padding.horizontal())),
                this.y + padding.top() + componentMargins.top() + Math.round((positioning.y / 100f) * (this.height() - child.fullSize().height() - padding.vertical()))
            );
            case ACROSS -> child.mount(
                this,
                this.x + padding.left() + componentMargins.left() + Math.round((positioning.x / 100f) * (this.width() - padding.horizontal())),
                this.y + padding.top() + componentMargins.top() + Math.round((positioning.y / 100f) * (this.height() - padding.vertical()))
            );
        }
    }

    /**
     * Draw the children of this component along with
     * their focus outline and tooltip, optionally clipping
     * them if {@link #allowOverflow} is {@code false}
     *
     * @param children The list of children to draw
     */
    protected void drawChildren(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta, List<? extends UIComponent> children) {
        if (!this.allowOverflow) {
            var padding = this.padding.get();
            context.enableScissor(this.x + padding.left(), this.y + padding.top(), this.x + padding.left() + this.width - padding.horizontal(), this.y + padding.top() + this.height - padding.vertical());
        }

        var focusHandler = this.focusHandler();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < children.size(); i++) {
            final var child = children.get(i);

            if (!context.intersectsScissor(child)) continue;

            child.draw(context, mouseX, mouseY, partialTicks, delta);
            if (focusHandler.lastFocusSource() == FocusSource.KEYBOARD_CYCLE && focusHandler.focused() == child) {
                child.drawFocusHighlight(context, mouseX, mouseY, partialTicks, delta);
            }
        }

        if (!this.allowOverflow) {
            context.disableScissor();
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
            Mth.lerpInt(this.horizontalSizing.get().contentFactor(), this.width - padding.horizontal(), thisSpace.width() - padding.horizontal()),
            Mth.lerpInt(this.verticalSizing.get().contentFactor(), this.height - padding.vertical(), thisSpace.height() - padding.vertical())
        );
    }

    @Override
    public BaseParentUIComponent positioning(Positioning positioning) {
        return (BaseParentUIComponent) super.positioning(positioning);
    }

    @Override
    public BaseParentUIComponent margins(Insets margins) {
        return (BaseParentUIComponent) super.margins(margins);
    }
}
