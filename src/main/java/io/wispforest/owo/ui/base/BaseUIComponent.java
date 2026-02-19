package io.wispforest.owo.ui.base;

import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.event.*;
import io.wispforest.owo.ui.util.FocusHandler;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * The reference implementation of the {@link UIComponent} interface,
 * ideally you should extend this when making your own components
 */
public abstract class BaseUIComponent implements UIComponent {

    @Nullable protected ParentUIComponent parent = null;
    @Nullable protected String id = null;

    protected boolean mounted = false;

    protected int batchedEvents = 0;

    protected final AnimatableProperty<Insets> margins = AnimatableProperty.of(Insets.none());

    protected final AnimatableProperty<Positioning> positioning = AnimatableProperty.of(Positioning.layout());
    protected final AnimatableProperty<Sizing> horizontalSizing = AnimatableProperty.of(Sizing.content());
    protected final AnimatableProperty<Sizing> verticalSizing = AnimatableProperty.of(Sizing.content());

    protected final EventStream<MouseDown> mouseDownEvents = MouseDown.newStream();
    protected final EventStream<MouseUp> mouseUpEvents = MouseUp.newStream();
    protected final EventStream<MouseScroll> mouseScrollEvents = MouseScroll.newStream();
    protected final EventStream<MouseDrag> mouseDragEvents = MouseDrag.newStream();
    protected final EventStream<KeyPress> keyPressEvents = KeyPress.newStream();
    protected final EventStream<CharTyped> charTypedEvents = CharTyped.newStream();
    protected final EventStream<FocusGained> focusGainedEvents = FocusGained.newStream();
    protected final EventStream<FocusLost> focusLostEvents = FocusLost.newStream();

    protected final EventStream<MouseEnter> mouseEnterEvents = MouseEnter.newStream();
    protected final EventStream<MouseLeave> mouseLeaveEvents = MouseLeave.newStream();

    protected boolean hovered = false;
    protected boolean dirty = false;

    protected CursorStyle cursorStyle = CursorStyle.NONE;
    protected List<ClientTooltipComponent> tooltip = List.of();

    protected int x, y;
    protected int width, height;

    protected Size space = Size.zero();

    protected BaseUIComponent() {
        Observable.observeAll(this::notifyParentIfMounted, margins, positioning, horizontalSizing, verticalSizing);
    }

    /**
     * @return The horizontal size this component needs to fit its contents
     */
    protected int determineHorizontalContentSize(Sizing sizing) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not support Sizing.content() on the horizontal axis");
    }

    /**
     * @return The vertical size this component needs to fit its contents
     */
    protected int determineVerticalContentSize(Sizing sizing) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not support Sizing.content() on the vertical axis");
    }

    @Override
    public void inflate(Size space) {
        this.space = space;
        this.applySizing();
        this.dirty = false;
    }

    /**
     * Calculate and apply the sizing of this component
     * according to the last known expansion space
     */
    protected void applySizing() {
        final var horizontalSizing = this.horizontalSizing.get();
        final var verticalSizing = this.verticalSizing.get();

        final var margins = this.margins.get();

        this.width = horizontalSizing.inflate(this.space.width() - margins.horizontal(), this::determineHorizontalContentSize);
        this.height = verticalSizing.inflate(this.space.height() - margins.vertical(), this::determineVerticalContentSize);
    }

    protected void notifyParentIfMounted() {
        if (!this.hasParent()) return;

        if (this.batchedEvents > 0) {
            this.batchedEvents++;
            return;
        }

        this.dirty = true;
        this.parent.onChildMutated(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends UIComponent> C configure(Consumer<C> closure) {
        try {
            this.runAndDeferEvents(() -> closure.accept((C) this));
        } catch (ClassCastException theUserDidBadItWasNotMyFault) {
            throw new IllegalArgumentException(
                    "Invalid target class passed when configuring component of type " + this.getClass().getSimpleName(),
                    theUserDidBadItWasNotMyFault
            );
        }

        return (C) this;
    }

    protected void runAndDeferEvents(Runnable action) {
        try {
            this.batchedEvents = 1;
            action.run();
        } finally {
            if (this.batchedEvents > 1) {
                this.batchedEvents = 0;
                this.notifyParentIfMounted();
            } else {
                this.batchedEvents = 0;
            }
        }
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        UIComponent.super.update(delta, mouseX, mouseY);

        boolean nowHovered = this.isInBoundingBox(mouseX, mouseY);
        if (this.hovered != nowHovered) {
            this.updateHoveredState(mouseX, mouseY, nowHovered);
        }
    }

    protected void updateHoveredState(int mouseX, int mouseY, boolean nowHovered) {
        this.hovered = nowHovered;

        if (nowHovered) {
            if (this.root() == null || this.root().childAt(mouseX, mouseY) != this) {
                this.hovered = false;
                return;
            }

            this.mouseEnterEvents.sink().onMouseEnter();
        } else {
            this.mouseLeaveEvents.sink().onMouseLeave();
        }
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        return this.mouseDownEvents.sink().onMouseDown(click, doubled);
    }

    @Override
    public EventSource<MouseDown> mouseDown() {
        return this.mouseDownEvents.source();
    }

    @Override
    public boolean onMouseUp(MouseButtonEvent click) {
        return this.mouseUpEvents.sink().onMouseUp(click);
    }

    @Override
    public EventSource<MouseUp> mouseUp() {
        return this.mouseUpEvents.source();
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        return this.mouseScrollEvents.sink().onMouseScroll(mouseX, mouseY, amount);
    }

    @Override
    public EventSource<MouseScroll> mouseScroll() {
        return this.mouseScrollEvents.source();
    }

    @Override
    public boolean onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
        return this.mouseDragEvents.sink().onMouseDrag(click, deltaX, deltaY);
    }

    @Override
    public EventSource<MouseDrag> mouseDrag() {
        return this.mouseDragEvents.source();
    }

    @Override
    public boolean onKeyPress(KeyEvent input) {
        return this.keyPressEvents.sink().onKeyPress(input);
    }

    @Override
    public EventSource<KeyPress> keyPress() {
        return this.keyPressEvents.source();
    }

    @Override
    public boolean onCharTyped(CharacterEvent input) {
        return this.charTypedEvents.sink().onCharTyped(input);
    }

    @Override
    public EventSource<CharTyped> charTyped() {
        return this.charTypedEvents.source();
    }

    @Override
    public void onFocusGained(FocusSource source) {
        this.focusGainedEvents.sink().onFocusGained(source);
    }

    @Override
    public EventSource<FocusGained> focusGained() {
        return this.focusGainedEvents.source();
    }

    @Override
    public void onFocusLost() {
        this.focusLostEvents.sink().onFocusLost();
    }

    @Override
    public EventSource<FocusLost> focusLost() {
        return this.focusLostEvents.source();
    }

    @Override
    public EventSource<MouseEnter> mouseEnter() {
        return this.mouseEnterEvents.source();
    }

    @Override
    public EventSource<MouseLeave> mouseLeave() {
        return this.mouseLeaveEvents.source();
    }

    @Override
    public CursorStyle cursorStyle() {
        return this.cursorStyle;
    }

    @Override
    public BaseUIComponent cursorStyle(CursorStyle style) {
        this.cursorStyle = style;
        return this;
    }

    @Override
    public UIComponent tooltip(List<ClientTooltipComponent> tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    @Override
    public List<ClientTooltipComponent> tooltip() {
        return this.tooltip;
    }

    @Override
    public void mount(ParentUIComponent parent, int x, int y) {
        this.parent = parent;
        this.mounted = true;
        this.moveTo(x, y);
    }

    @Override
    public void dismount(DismountReason reason) {
        this.parent = null;
        this.mounted = false;
    }

    @Override
    public ParentUIComponent parent() {
        return this.parent;
    }

    @Override
    public @Nullable FocusHandler focusHandler() {
        return this.hasParent() ? this.parent.focusHandler() : null;
    }

    @Override
    public BaseUIComponent positioning(Positioning positioning) {
        this.positioning.set(positioning);
        return this;
    }

    @Override
    public AnimatableProperty<Positioning> positioning() {
        return this.positioning;
    }

    @Override
    public BaseUIComponent margins(Insets margins) {
        this.margins.set(margins);
        return this;
    }

    @Override
    public AnimatableProperty<Insets> margins() {
        return this.margins;
    }

    @Override
    public UIComponent horizontalSizing(Sizing horizontalSizing) {
        this.horizontalSizing.set(horizontalSizing);
        return this;
    }

    @Override
    public AnimatableProperty<Sizing> horizontalSizing() {
        return this.horizontalSizing;
    }

    @Override
    public UIComponent verticalSizing(Sizing verticalSizing) {
        this.verticalSizing.set(verticalSizing);
        return this;
    }

    @Override
    public AnimatableProperty<Sizing> verticalSizing() {
        return this.verticalSizing;
    }

    @Override
    public UIComponent id(@Nullable String id) {
        this.id = id;
        return this;
    }

    @Override
    public @Nullable String id() {
        return this.id;
    }

    @Override
    public int x() {
        return this.x;
    }

    @Override
    public void updateX(int x) {
        this.x = x;
    }

    @Override
    public int y() {
        return this.y;
    }

    @Override
    public void updateY(int y) {
        this.y = y;
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return this.height;
    }
}
