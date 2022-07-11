package io.wispforest.owo.ui.base;

import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.FocusHandler;
import io.wispforest.owo.util.Observable;
import org.jetbrains.annotations.Nullable;

/**
 * The reference implementation of the {@link Component} interface,
 * ideally you should extend this when making your own components
 */
public abstract class BaseComponent implements Component {

    @Nullable protected ParentComponent parent = null;
    @Nullable protected String id = null;

    protected AnimatableProperty<Insets> margins = AnimatableProperty.of(Insets.none());

    protected AnimatableProperty<Positioning> positioning = AnimatableProperty.of(Positioning.layout());
    protected AnimatableProperty<Sizing> horizontalSizing = AnimatableProperty.of(Sizing.content());
    protected AnimatableProperty<Sizing> verticalSizing = AnimatableProperty.of(Sizing.content());

    protected int x, y;
    protected int width, height;

    protected Size space = Size.zero();

    protected BaseComponent() {
        Observable.observeAll(this::notifyParentIfMounted, margins, positioning, horizontalSizing, verticalSizing);
    }

    /**
     * Set the horizontal size of this component, based on its content
     */
    protected void applyHorizontalContentSizing(Sizing sizing) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not support horizontal Sizing.content()");
    }

    /**
     * Set the vertical size of this component, based on its content
     */
    protected void applyVerticalContentSizing(Sizing sizing) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not support vertical Sizing.content()");
    }

    @Override
    public void inflate(Size space) {
        this.space = space;

        final var horizontalSizing = this.horizontalSizing.get();
        final var verticalSizing = this.verticalSizing.get();

        final var margins = this.margins.get();

        if (horizontalSizing.method == Sizing.Method.CONTENT) {
            verticalSizing.inflate(space.height() - margins.vertical(), height -> this.height = height, this::applyVerticalContentSizing);
            horizontalSizing.inflate(space.width() - margins.horizontal(), width -> this.width = width, this::applyHorizontalContentSizing);
        } else {
            horizontalSizing.inflate(space.width() - margins.horizontal(), width -> this.width = width, this::applyHorizontalContentSizing);
            verticalSizing.inflate(space.height() - margins.vertical(), height -> this.height = height, this::applyVerticalContentSizing);
        }
    }

    protected void notifyParentIfMounted() {
        if (!this.hasParent()) return;
        this.parent.onChildMutated(this);
    }

    @Override
    public void mount(ParentComponent parent, int x, int y) {
        this.parent = parent;
        this.moveTo(x, y);
    }

    @Override
    public void onDismounted(DismountReason reason) {
        this.parent = null;
    }

    @Override
    public ParentComponent parent() {
        return this.parent;
    }

    @Override
    public @Nullable FocusHandler focusHandler() {
        return this.hasParent() ? this.parent.focusHandler() : null;
    }

    @Override
    public BaseComponent positioning(Positioning positioning) {
        this.positioning.set(positioning);
        return this;
    }

    @Override
    public AnimatableProperty<Positioning> positioning() {
        return this.positioning;
    }

    @Override
    public BaseComponent margins(Insets margins) {
        this.margins.set(margins);
        return this;
    }

    @Override
    public AnimatableProperty<Insets> margins() {
        return this.margins;
    }

    @Override
    public Component horizontalSizing(Sizing horizontalSizing) {
        this.horizontalSizing.set(horizontalSizing);
        return this;
    }

    @Override
    public AnimatableProperty<Sizing> horizontalSizing() {
        return this.horizontalSizing;
    }

    @Override
    public Component verticalSizing(Sizing verticalSizing) {
        this.verticalSizing.set(verticalSizing);
        return this;
    }

    @Override
    public AnimatableProperty<Sizing> verticalSizing() {
        return this.verticalSizing;
    }

    @Override
    public Component id(@Nullable String id) {
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
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int y() {
        return this.y;
    }

    @Override
    public void setY(int y) {
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
