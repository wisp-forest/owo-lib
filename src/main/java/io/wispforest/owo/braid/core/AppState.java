package io.wispforest.owo.braid.core;

import com.google.common.collect.Streams;
import com.mojang.blaze3d.platform.GlStateManager;
import io.wispforest.owo.braid.core.cursor.CursorController;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.instance.*;
import io.wispforest.owo.braid.framework.proxy.BuildScope;
import io.wispforest.owo.braid.framework.proxy.ProxyHost;
import io.wispforest.owo.braid.framework.proxy.SingleChildInstanceWidgetProxy;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Tooltip;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AppState implements InstanceHost, ProxyHost {

    public final @Nullable Logger logger;
    private final MinecraftClient client;
    public final CursorController cursorController;

    private final BuildScope rootBuildScope = new BuildScope();
    private Deque<AnimationCallback> animationCallbacks = new ArrayDeque<>();
    private PriorityQueue<ScheduledCallback> callbacks = new PriorityQueue<>();
    private final RootProxy root;

    private Set<MouseListener> hovered = new HashSet<>();
    private @Nullable MouseListener dragging = null;
    private @Nullable CursorStyle draggingCursorStyle = null;
    private int draggingButton = -1;
    private boolean dragStarted = false;

    private List<KeyboardListener> focused = new ArrayList<>();

    private final BraidHotReloadCallback.Listener reloadListener;

    public AppState(
        @Nullable Logger logger,
        MinecraftClient client,
        Widget root
    ) {
        this.logger = logger;
        this.client = client;
        this.cursorController = new CursorController(client.getWindow().getHandle());

        this.root = new RootWidget(root, this.rootBuildScope).proxy();
        this.root.bootstrap(this, this);
        this.scheduleLayout(this.rootInstance());

        this.reloadListener = BraidHotReloadCallback.register();
    }

    private @Nullable TooltipState activeTooltip;

    public void draw(DrawContext ctx) {
        ctx.push();
        this.rootInstance().transform.transformToParent(ctx.getMatrices());

        var owoContext = OwoUIDrawContext.of(ctx);

        GlStateManager._enableScissorTest();
        this.rootInstance().draw(owoContext);
        GlStateManager._disableScissorTest();

        if (this.activeTooltip != null) {
            owoContext.drawTooltip(this.client.textRenderer, this.activeTooltip.x(), this.activeTooltip.y(), this.activeTooltip.components());
        }

        ctx.pop();
    }

    public void updateWidgetsAndInteractions(double mouseX, double mouseY, float partialTicks, float frameDeltaInTicks) {
        if (this.reloadListener.poll()) {
            this.rebuildRoot();
        }

        if (!this.animationCallbacks.isEmpty()) {
            var callbacksForThisFrame = this.animationCallbacks;
            this.animationCallbacks = new ArrayDeque<>();

            while (!callbacksForThisFrame.isEmpty()) {
                var callback = callbacksForThisFrame.removeFirst();
                callback.run(frameDeltaInTicks);
            }
        }

        var now = Instant.now();
        while (!this.callbacks.isEmpty() && this.callbacks.peek().after().isBefore(now)) {
            this.callbacks.poll().callback().run();
        }

        this.rootBuildScope.rebuildDirtyProxies();
        this.flushLayoutQueue();

        // ---

        var state = this.hitTest(mouseX, mouseY);

        var nowHovered = new HashSet<MouseListener>();
        Streams.stream(state.occludedTrace()).map(Hit::instance).filter(MouseListener.class::isInstance).map(MouseListener.class::cast).forEach(listener -> {
            nowHovered.add(listener);

            if (this.hovered.contains(listener)) {
                this.hovered.remove(listener);
            } else {
                listener.onMouseEnter();
            }
        });

        for (var noLongerHovered : this.hovered) {
            noLongerHovered.onMouseExit();
        }

        this.hovered = nowHovered;

        var tooltipSupplier = state.firstWhere(hit -> hit.instance().widget() instanceof Tooltip);
        if (tooltipSupplier != null) {
            var tooltip = (Tooltip) tooltipSupplier.instance().widget();
            var components = tooltip.tooltip == null
                ? this.client.textRenderer.wrapLines(tooltip.tooltipText, Integer.MAX_VALUE).stream().<TooltipComponent>map(OrderedTextTooltipComponent::new).toList()
                : tooltip.tooltip;

            this.activeTooltip = new TooltipState(components, (int) mouseX, (int) mouseY);
        } else {
            this.activeTooltip = null;
        }

        // ---

        @Nullable CursorStyle activeStyle = null;
        if (this.dragging != null) {
            activeStyle = this.draggingCursorStyle;
        } else {
            var cursorStyleSource = state.firstWhere(
                (hit) ->
                    hit.instance() instanceof MouseListener &&
                        ((MouseListener) hit.instance()).cursorStyleAt(hit.x(), hit.y()) != null
            );

            if (cursorStyleSource != null) {
                activeStyle = ((MouseListener) cursorStyleSource.instance()).cursorStyleAt(
                    cursorStyleSource.x(),
                    cursorStyleSource.y()
                );
            }
        }

        this.cursorController.setStyle(activeStyle != null ? activeStyle : CursorStyle.NONE);
    }

    public void rebuildRoot() {
        var before = Instant.now();

        this.root.reassemble();

        var elapsed = ChronoUnit.MICROS.between(before, Instant.now());
        if (this.logger != null) this.logger.debug("completed full app rebuild in {}us", elapsed);
    }

    public void dispose() {
        this.reloadListener.unregister();

        this.cursorController.dispose();
        this.root.unmount();
    }

    private HitTestState hitTest(double x, double y) {
        var state = new HitTestState();
        this.rootInstance().hitTest(x, y, state);

        return state;
    }

    // ---

    public boolean dispatchMouseDownEvent(double x, double y, int button) {
        var state = this.hitTest(x, y);

        var clicked = state.firstWhere(
            (hit) -> hit.instance() instanceof MouseListener && ((MouseListener) hit.instance()).onMouseDown(hit.x(), hit.y(), button)
        );

        if (clicked != null && this.dragging == null) {
            this.dragging = (MouseListener) clicked.instance();
            this.draggingCursorStyle = ((MouseListener) clicked.instance()).cursorStyleAt(
                clicked.x(),
                clicked.y()
            );
            this.dragStarted = false;
            this.draggingButton = button;
        }

        var nowFocused = new ArrayList<KeyboardListener>();
        Streams.stream(state.occludedTrace()).map(Hit::instance).filter(KeyboardListener.class::isInstance).map(KeyboardListener.class::cast).forEach(listener -> {
            nowFocused.add(listener);

            if (this.focused.contains(listener)) {
                this.focused.remove(listener);
            } else {
                listener.onFocusGained();
            }
        });

        for (var noLongerFocused : this.focused) {
            noLongerFocused.onFocusLost();
        }

        this.focused = nowFocused;

        return true;
    }

    public boolean dispatchMouseDragEvent(double x, double y, double deltaX, double deltaY) {
        if (!(this.dragging instanceof WidgetInstance<?>)) return false;

        if (!this.dragStarted) {
            this.dragging.onMouseDragStart(draggingButton);
            this.dragStarted = true;
        }

        var globalTransform = ((WidgetInstance<?>) this.dragging).computeGlobalTransform();
        var coordinates = new Vector4f((float) x, (float) y, 0, 1);
        globalTransform.transform(coordinates);

        // apply *only the rotation* of the instance's transform
        // to the mouse movement
        var delta = new Vector4f((float) deltaX, (float) deltaY, 0, 0);
        globalTransform.transform(delta);

        this.dragging.onMouseDrag(coordinates.x, coordinates.y, delta.x, delta.y);
        return true;
    }

    public boolean dispatchMouseUpEvent(double x, double y, int button) {
        var state = this.hitTest(x, y);

        var unClicked = state.firstWhere(
            (hit) -> hit.instance() instanceof MouseListener && ((MouseListener) hit.instance()).onMouseUp(hit.x(), hit.y(), button)
        );
        var consumed = unClicked != null;

        if (this.dragStarted && this.dragging != null && this.draggingButton == button) {
            this.dragging.onMouseDragEnd();
            consumed = true;
        }

        this.dragging = null;
        return consumed;
    }

    public boolean dispatchMouseScrollEvent(double x, double y, double xOffset, double yOffset) {
        return this.hitTest(x, y).firstWhere(
            (hit) -> hit.instance() instanceof MouseListener &&
                ((MouseListener) hit.instance()).onMouseScroll(
                    hit.x(),
                    hit.y(),
                    xOffset,
                    yOffset
                )
        ) != null;
    }

    public boolean dispatchKeyDownEvent(int keyCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R && (modifiers & (GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_ALT)) != 0) {
            this.rebuildRoot();
            return true;
        }

        for (var listener : this.focused) {
            if (listener.onKeyDown(keyCode, modifiers)) {
                return true;
            }
        }

        return false;
    }

    public boolean dispatchKeyUpEvent(int keyCode, int modifiers) {
        for (var listener : this.focused) {
            if (listener.onKeyUp(keyCode, modifiers)) {
                return true;
            }
        }

        return false;
    }

    public boolean dispatchCharEvent(int charCode, int modifiers) {
        for (var listener : this.focused) {
            if (listener.onChar(charCode, modifiers)) {
                return true;
            }
        }

        return false;
    }

    // ---

    @Override
    public MinecraftClient client() {
        return this.client;
    }

    public SingleChildWidgetInstance<?> rootInstance() {
        return this.root.instance();
    }

    // ---

    private List<WidgetInstance<?>> layoutQueue = new ArrayList<>();
    private boolean mergeToLayoutQueue = false;

    private void flushLayoutQueue() {
        while (!this.layoutQueue.isEmpty()) {
            var queue = this.layoutQueue;
            this.layoutQueue = new ArrayList<>();

            queue.sort(Comparator.naturalOrder());
            for (var idx = 0; idx < queue.size(); idx++) {
                var instance = queue.get(idx);

                if (this.mergeToLayoutQueue) {
                    this.mergeToLayoutQueue = false;

                    if (!this.layoutQueue.isEmpty()) {
                        this.layoutQueue.addAll(queue.subList(idx, queue.size()));
                        break;
                    }
                }

                if (instance.needsLayout()) {
                    instance.layout(
                        instance.hasParent()
                            ? instance.constraints()
                            : Constraints.tight(Size.of(this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight()))
                    );
                }
            }

            this.mergeToLayoutQueue = false;
        }
    }

    @Override
    public void scheduleLayout(WidgetInstance<?> instance) {
        this.layoutQueue.add(instance);
    }

    @Override
    public void notifySubtreeRebuild() {
        this.mergeToLayoutQueue = true;
    }

    @Override
    public void scheduleAnimationCallback(AnimationCallback callback) {
        this.animationCallbacks.add(callback);
    }

    @Override
    public void scheduleDelayedCallback(Duration delay, Runnable callback) {
        this.callbacks.add(new ScheduledCallback(
            Instant.now().plus(delay),
            callback
        ));
    }
}

record ScheduledCallback(Instant after, Runnable callback) implements Comparable<ScheduledCallback> {
    @Override
    public int compareTo(@NotNull ScheduledCallback o) {
        return this.after.compareTo(o.after);
    }
}

class RootWidget extends SingleChildInstanceWidget {

    public final BuildScope rootBuildScope;

    public RootWidget(Widget child, BuildScope rootBuildScope) {
        super(child);
        this.rootBuildScope = rootBuildScope;
    }

    @Override
    public RootProxy proxy() {
        return new RootProxy(this);
    }

    @Override
    public RootInstance instantiate() {
        return new RootInstance(this);
    }
}

class RootProxy extends SingleChildInstanceWidgetProxy {
    public RootProxy(RootWidget widget) {
        super(widget);
    }

    @Override
    public BuildScope buildScope() {
        return ((RootWidget) this.widget()).rootBuildScope;
    }

    @Override
    public boolean mounted() {
        return this.bootstrapped;
    }

    private boolean bootstrapped = false;

    void bootstrap(InstanceHost instanceHost, ProxyHost proxyHost) {
        this.bootstrapped = true;
        this.lifecycle = Lifecycle.LIVE;

        this.rootSetHost(proxyHost);

        rebuild();
        this.setDepth(0);

        this.instance.setDepth(0);
        this.instance.attachHost(instanceHost);
    }
}

class RootInstance extends SingleChildWidgetInstance<RootWidget> {

    public RootInstance(RootWidget widget) {
        super(widget);
    }

    @Override
    protected void doLayout(Constraints constraints) {
        this.sizeToChild(constraints, this.child);
    }
}

record TooltipState(List<TooltipComponent> components, int x, int y) {}
