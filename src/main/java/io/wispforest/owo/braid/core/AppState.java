package io.wispforest.owo.braid.core;

import com.google.common.collect.Streams;
import com.mojang.blaze3d.platform.GlStateManager;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.core.events.*;
import io.wispforest.owo.braid.framework.instance.*;
import io.wispforest.owo.braid.framework.proxy.BuildScope;
import io.wispforest.owo.braid.framework.proxy.ProxyHost;
import io.wispforest.owo.braid.framework.proxy.SingleChildInstanceWidgetProxy;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Tooltip;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.util.EventSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppState implements InstanceHost, ProxyHost {

    public final @Nullable Logger logger;
    private final MinecraftClient client;

    public final Surface surface;
    public final EventBuffer eventBuffer;

    private final BuildScope rootBuildScope = new BuildScope();
    private Deque<AnimationCallback> animationCallbacks = new LinkedList<>();
    private final PriorityQueue<ScheduledCallback> callbacks = new PriorityQueue<>();
    private Deque<Runnable> postLayoutCallbacks = new LinkedList<>();
    private final RootProxy root;

    private final Vector2d cursorPosition = new Vector2d();

    private Set<MouseListener> hovered = new HashSet<>();
    private final WeakHashMap<MouseListener, MousePosition> mousePositions = new WeakHashMap<>();
    private @Nullable MouseListener dragging = null;
    private @Nullable CursorStyle draggingCursorStyle = null;
    private int draggingButton = -1;
    private boolean dragStarted = false;

    private List<KeyboardListener> focused = new ArrayList<>();

    private final BraidHotReloadCallback.Listener reloadListener;
    private final EventSource<?>.Subscription resizeSubscription;
    private boolean running = true;

    public AppState(
        @Nullable Logger logger,
        MinecraftClient client,
        Surface surface,
        EventBuffer eventBuffer,
        Widget root
    ) {
        this.logger = logger;
        this.client = client;

        this.surface = surface;
        this.eventBuffer = eventBuffer;

        this.root = new RootWidget(root, this.rootBuildScope).proxy();
        this.root.bootstrap(this, this);
        this.scheduleLayout(this.rootInstance());

        this.reloadListener = BraidHotReloadCallback.register();
        this.resizeSubscription = this.surface.onResize().subscribe((newWidth, newHeight) -> {
            this.rootInstance().markNeedsLayout();
        });
    }

    public boolean running() {
        return this.running;
    }

    private @Nullable TooltipState activeTooltip;

    public void draw(DrawContext ctx) {
        this.surface.beginRendering();

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
        ctx.draw();

        this.surface.endRendering();
    }

    public void updateWidgetsAndInteractions(float partialTicks, float frameDeltaInTicks) {
        this.pollAndDispatchEvents();

        var state = this.hitTest();

        var nowHovered = new HashSet<MouseListener>();
        Streams.stream(state.occludedTrace()).filter(hit -> hit.instance() instanceof MouseListener).forEach(hit -> {
            var listener = (MouseListener) hit.instance();

            nowHovered.add(listener);

            if (this.hovered.contains(listener)) {
                this.hovered.remove(listener);
            } else {
                listener.onMouseEnter();
            }

            var mousePosition = this.mousePositions.getOrDefault(listener, MousePosition.ORIGIN);
            if (mousePosition.x() != hit.x() || mousePosition.y() != hit.y()) {
                listener.onMouseMove(hit.x(), hit.y());
                this.mousePositions.put(listener, new MousePosition(hit.x(), hit.y()));
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

            this.activeTooltip = new TooltipState(components, (int) this.cursorPosition.x, (int) this.cursorPosition.y);
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

        this.surface.setCursorStyle(activeStyle != null ? activeStyle : CursorStyle.NONE);

        // ---

        if (this.reloadListener.poll()) {
            this.rebuildRoot();
        }

        if (!this.animationCallbacks.isEmpty()) {
            var callbacksForThisFrame = this.animationCallbacks;
            this.animationCallbacks = new LinkedList<>();

            while (!callbacksForThisFrame.isEmpty()) {
                callbacksForThisFrame.poll().run(frameDeltaInTicks);
            }
        }

        var now = Instant.now();
        while (!this.callbacks.isEmpty() && this.callbacks.peek().after().isBefore(now)) {
            this.callbacks.poll().callback().run();
        }

        this.rootBuildScope.rebuildDirtyProxies();
        this.flushLayoutQueue();

        if (!this.postLayoutCallbacks.isEmpty()) {
            var callbacksForThisFrame = this.postLayoutCallbacks;
            this.postLayoutCallbacks = new LinkedList<>();

            while (!callbacksForThisFrame.isEmpty()) {
                callbacksForThisFrame.poll().run();
            }
        }
    }

    private void pollAndDispatchEvents() {
        var events = this.eventBuffer.poll();

        for (var event : events) {
            switch (event) {
                case MouseButtonPressEvent(int button) -> {
                    var state = this.hitTest();

                    this.updateFocus(
                        Streams.stream(state.occludedTrace())
                            .map(Hit::instance)
                            .filter(KeyboardListener.class::isInstance)
                            .map(KeyboardListener.class::cast)
                            .findFirst()
                            .orElse(null)
                    );

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
                }
                case MouseMoveEvent(double x, double y, double deltaX, double deltaY) -> {
                    this.cursorPosition.x = x;
                    this.cursorPosition.y = y;

                    if (!(this.dragging instanceof WidgetInstance<?>)) break;

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
                }
                case MouseButtonReleaseEvent(int button) -> {
                    var state = this.hitTest();
                    state.firstWhere(
                        (hit) -> hit.instance() instanceof MouseListener && ((MouseListener) hit.instance()).onMouseUp(hit.x(), hit.y(), button)
                    );

                    if (this.draggingButton == button) {
                        if (this.dragStarted && this.dragging != null) {
                            this.dragging.onMouseDragEnd();
                        }

                        this.dragging = null;
                    }
                }
                case MouseScrollEvent(double xOffset, double yOffset) -> {
                    this.hitTest().firstWhere(
                        (hit) -> hit.instance() instanceof MouseListener &&
                            ((MouseListener) hit.instance()).onMouseScroll(
                                hit.x(),
                                hit.y(),
                                xOffset,
                                yOffset
                            )
                    );
                }
                case KeyPressEvent(int keyCode, int scancode, KeyModifiers modifiers) -> {
                    if (keyCode == GLFW.GLFW_KEY_R && modifiers.shift() && modifiers.alt()) {
                        this.rebuildRoot();
                        break;
                    }

                    for (var listener : this.focused) {
                        if (listener.onKeyDown(keyCode, modifiers)) {
                            break;
                        }
                    }
                }
                case KeyReleaseEvent(int keycode, int scancode, KeyModifiers modifiers) -> {
                    for (var listener : this.focused) {
                        if (listener.onKeyUp(keycode, modifiers)) {
                            break;
                        }
                    }
                }
                case CharInputEvent(char codepoint, KeyModifiers modifiers) -> {
                    for (var listener : this.focused) {
                        if (listener.onChar(codepoint, modifiers)) {
                            break;
                        }
                    }
                }
                case FilesDroppedEvent filesDroppedEvent -> {}
                case CloseEvent ignored -> {
                    this.running = false;
                }
            }
        }
    }

    public void rebuildRoot() {
        var before = Instant.now();

        this.root.reassemble();

        var elapsed = ChronoUnit.MICROS.between(before, Instant.now());
        if (this.logger != null) this.logger.debug("completed full app rebuild in {}us", elapsed);
    }

    public void dispose() {
        this.reloadListener.unregister();
        this.resizeSubscription.cancel();

        this.surface.dispose();
        this.root.unmount();
    }

    private HitTestState hitTest() {
        return this.hitTest(this.cursorPosition.x, this.cursorPosition.y);
    }

    public HitTestState hitTest(double x, double y) {
        var state = new HitTestState();
        this.rootInstance().hitTest(x, y, state);

        return state;
    }

    // ---

    private void updateFocus(@Nullable KeyboardListener focusTarget) {
        var nowFocused = focusTarget != null
            ? Stream.concat(Stream.of(focusTarget), ((WidgetInstance<?>) focusTarget).ancestors().stream().filter(KeyboardListener.class::isInstance).map(KeyboardListener.class::cast)).collect(Collectors.toList())
            : List.<KeyboardListener>of();

        for (var listener : nowFocused) {
            if (this.focused.contains(listener)) {
                this.focused.remove(listener);
            } else {
                listener.onFocusGained();
            }
        }

        for (var noLongerFocused : this.focused) {
            noLongerFocused.onFocusLost();
        }

        this.focused = nowFocused;
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
                            : Constraints.tight(Size.of(this.surface.width(), this.surface.height()))
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
    public void moveFocusTo(KeyboardListener focusTarget) {
        this.updateFocus(focusTarget);
    }

    @Override
    public void scheduleAnimationCallback(AnimationCallback callback) {
        this.animationCallbacks.offer(callback);
    }

    @Override
    public void scheduleDelayedCallback(Duration delay, Runnable callback) {
        this.callbacks.add(new ScheduledCallback(
            Instant.now().plus(delay),
            callback
        ));
    }

    @Override
    public void schedulePostLayoutCallback(Runnable callback) {
        this.postLayoutCallbacks.offer(callback);
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

class RootInstance extends SingleChildWidgetInstance.ShrinkWrap<RootWidget> {

    public RootInstance(RootWidget widget) {
        super(widget);
    }
}

record TooltipState(List<TooltipComponent> components, int x, int y) {}

record MousePosition(double x, double y) {
    public static final MousePosition ORIGIN = new MousePosition(0, 0);
}
