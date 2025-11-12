package io.wispforest.owo.braid.framework.instance;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.BraidDrawContext;
import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.widget.InstanceWidget;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.util.NinePatchTexture;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.util.*;

public abstract class WidgetInstance<T extends InstanceWidget> implements Comparable<WidgetInstance<?>> {
    public static final int FLAG_HIT_TEST_BOUNDARY = 0b1;

    public final WidgetTransform transform = this.createTransform();

    public @Nullable Object parentData;
    public int flags = 0;
    private int depth = 0;

    private InstanceHost host;
    private WidgetInstance<?> parent;

    protected T widget;

    // ---

    public boolean debugHighlighted = false;
    public boolean debugDrawVisualizers = false;

    public boolean debugParentHasDependency() {
        //noinspection OptionalAssignedToNull
        return !this.intrinsicSizeCache.isEmpty() || this.baselineOffsetCache != null;
    }

    // ---

    private @Nullable Constraints constraints;
    private boolean needsLayout = false;
    private @Nullable WidgetInstance<?> relayoutBoundary;

    public WidgetInstance(T widget) {
        this.widget = widget;
    }

    protected WidgetTransform createTransform() {
        return new WidgetTransform();
    }

    // ---

    public final Size layout(Constraints constraints) {
        if (!this.needsLayout && Objects.equals(constraints, this.constraints)) {
            return this.transform.toSize();
        }

        this.constraints = constraints;
        this.relayoutBoundary = constraints.isTight() || this.parent == null ? this : this.parent.relayoutBoundary;

        this.doLayout(constraints);
        this.needsLayout = false;

        return this.transform.toSize();
    }

    protected abstract void doLayout(Constraints constraints);

    protected abstract double measureIntrinsicWidth(double height);
    protected abstract double measureIntrinsicHeight(double width);

    private final Object2DoubleMap<IntrinsicCacheKey> intrinsicSizeCache = new Object2DoubleOpenHashMap<>();

    public double getIntrinsicWidth(double height) {
        return this.intrinsicSizeCache.computeIfAbsent(new IntrinsicCacheKey(LayoutAxis.HORIZONTAL, height), ($) -> this.measureIntrinsicWidth(height));
    }

    public double getIntrinsicHeight(double width) {
        return this.intrinsicSizeCache.computeIfAbsent(new IntrinsicCacheKey(LayoutAxis.VERTICAL, width), ($) -> this.measureIntrinsicHeight(width));
    }

    protected abstract OptionalDouble measureBaselineOffset();

    private @Nullable OptionalDouble baselineOffsetCache;
    public OptionalDouble getBaselineOffset() {
        //noinspection OptionalAssignedToNull
        if (this.baselineOffsetCache != null) return this.baselineOffsetCache;
        return this.baselineOffsetCache = this.measureBaselineOffset();
    }

    // ---

    public abstract void draw(BraidDrawContext ctx);

    public abstract void visitChildren(Visitor visitor);

    // ---

    public void attachHost(InstanceHost host) {
        this.host = host;

        var callback = POST_ATTACH_CALLBACKS.remove(this);
        if (callback != null) callback.run();

        this.visitChildren(child -> child.attachHost(host));
    }

    protected <W extends @Nullable WidgetInstance<?>> W adopt(W child) {
        if (child == null || ((WidgetInstance<?>) child).parent == this) return child;

        child.setDepth(this.depth + 1);
        ((WidgetInstance<?>) child).parent = this;
        if (this.host != null) {
            child.attachHost(this.host);
        }

        return child;
    }

    // ---

    public List<InspectorProperty> debugListInspectorProperties() {
        return List.of();
    }

    public boolean debugHasVisualizers() {
        return false;
    }

    protected void debugDrawVisualizers(BraidDrawContext ctx) {}

    // ---

    protected void drawChild(BraidDrawContext ctx, WidgetInstance<?> child) {
        ctx.push();
        child.transform.transformToParent(ctx.getMatrices());
        child.draw(ctx);

        if (child.debugHasVisualizers() && child.debugDrawVisualizers) {
            child.debugDrawVisualizers(ctx);
        }

        if (child.debugHighlighted) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            NinePatchTexture.draw(
                Owo.id("braid_debug_highlighted"),
                ctx,
                0, 0, (int) child.transform.width(), (int) child.transform.height(),
                Color.ofRgb(0x00FFD1)
            );
        }

        ctx.pop();
    }

    protected void sizeToChild(Constraints constraints, @Nullable WidgetInstance<?> child) {
        if (child == null) {
            this.transform.setSize(constraints.minSize());
        } else {
            var childSize = child.layout(constraints);
            this.transform.setSize(childSize);
        }
    }

    public void clearLayoutCache(boolean recursive) {
        this.needsLayout = true;

        if (recursive) {
            this.visitChildren(child -> child.clearLayoutCache(true));
        }
    }

    @SuppressWarnings("OptionalAssignedToNull")
    public void markNeedsLayout() {
        this.needsLayout = true;

        var parentHasDependency = !this.intrinsicSizeCache.isEmpty() || this.baselineOffsetCache != null;
        this.intrinsicSizeCache.clear();
        this.baselineOffsetCache = null;

        if (!parentHasDependency && this.isRelayoutBoundary()) {
            if (this.host != null) this.host.scheduleLayout(this);
        } else {
            if (this.parent != null) this.parent.markNeedsLayout();
        }
    }

    private boolean debugDisposed = false;

    @MustBeInvokedByOverriders
    public void dispose() {
        Preconditions.checkState(!this.debugDisposed, "tried to dispose a widget instance twice");
        this.debugDisposed = true;

        this.parent = null;
    }

    // ---

    public List<WidgetInstance<?>> ancestors() {
        var result = new ArrayList<WidgetInstance<?>>();
        var ancestor = this.parent;

        while (ancestor != null) {
            result.add(ancestor);
            ancestor = ancestor.parent;
        }

        return result;
    }

    public void hitTest(double x, double y, HitTestState state) {
        if (this.hitTestSelf(x, y)) {
            state.addHit(this, x, y);
        }

        var coordinates = new Vector3d();
        this.visitChildren(child -> {
            coordinates.set(x, y, 0);
            child.transform.toWidgetCoordinates(coordinates);

            child.hitTest(coordinates.x, coordinates.y, state);
        });
    }

    protected boolean hitTestSelf(double x, double y) {
        return x >= 0 && x < this.transform.width && y >= 0 && y < this.transform.height;
    }

    public Matrix4f computeGlobalTransform() {
        return this.computeTransformFrom(null);
    }

    public Matrix4f computeTransformFrom(@Nullable WidgetInstance<?> ancestor) {
        var result = new Matrix4f();

        this.transform.transformToWidget(result);

        for (var step : this.ancestors()) {
            if (step == ancestor) break;
            step.transform.transformToWidget(result);
        }

        return result;
    }

    public Box computeGlobalBounds() {
        var global = this.parent != null ? this.parent.computeGlobalTransform().invert() : new Matrix4f();

        var min = global.transformPosition(new Vector3f((float) this.transform.x, (float) this.transform.y, 0));
        var max = global.transformPosition(new Vector3f((float) (this.transform.x + this.transform.width), (float) (this.transform.y + this.transform.height), 0));

        return new Box(min.x, min.y, min.z, max.x, max.y, max.z);
    }

    public Vector3d computeGlobalPosition() {
        var global = this.parent != null ? this.parent.computeGlobalTransform().invert() : new Matrix4f();

        var pos = global.transformPosition(new Vector3f((float) this.transform.x, (float) this.transform.y, 0));
        return new Vector3d(pos.x, pos.y, pos.z);
    }

    // ---


    public @Nullable Constraints constraints() {
        return this.constraints;
    }

    public int depth() {
        return this.depth;
    }

    public void setDepth(int depth) {
        if (this.depth == depth) return;

        this.depth = depth;
        this.visitChildren(child -> child.setDepth(this.depth + 1));
    }

    /// To prevent excessive IDE warnings, the return type of this
    /// getter is not annotated `@Nullable` even though if it is called
    /// before this instance is adopted it will (correctly) return null
    public InstanceHost host() {
        return this.host;
    }

    public boolean needsLayout() {
        return this.needsLayout;
    }

    public boolean isRelayoutBoundary() {
        return this.relayoutBoundary == this;
    }

    public boolean hasParent() {
        return this.parent != null;
    }

    public void setWidget(T widget) {
        this.widget = widget;
    }

    public T widget() {
        return this.widget;
    }

    public WidgetInstance<?> parent() {
        return this.parent;
    }

    // ---

    private static final WeakHashMap<WidgetInstance<?>, Runnable> POST_ATTACH_CALLBACKS = new WeakHashMap<>();
    public static void addPostAttachCallback(WidgetInstance<?> instance, Runnable callback) {
        POST_ATTACH_CALLBACKS.put(instance, callback);
    }

    // ---

    @Override
    public int compareTo(@NotNull WidgetInstance<?> o) {
        return Integer.compare(this.depth, o.depth);
    }

    // ---

    @FunctionalInterface
    public interface Visitor {
        void visit(WidgetInstance<?> child);
    }
}

record IntrinsicCacheKey(LayoutAxis axis, double crossExtent) {}

//enum Visitors implements WidgetInstance.Visitor {
//    MARK_NEEDS_LAYOUT(WidgetInstance::markNeedsLayout);
//
//    private final WidgetInstance.Visitor delegate;
//
//    Visitors(WidgetInstance.Visitor delegate) {
//        this.delegate = delegate;
//    }
//
//    @Override
//    public void visit(WidgetInstance<?> child) {
//        this.delegate.visit(child);
//    }
//}
