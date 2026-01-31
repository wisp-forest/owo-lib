package io.wispforest.owo.braid.core;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.opengl.GlStateManager;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.core.events.*;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.instance.*;
import io.wispforest.owo.braid.framework.proxy.BuildScope;
import io.wispforest.owo.braid.framework.proxy.ProxyHost;
import io.wispforest.owo.braid.framework.proxy.SingleChildInstanceWidgetProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Tooltip;
import io.wispforest.owo.braid.widgets.basic.VisitorWidget;
import io.wispforest.owo.braid.widgets.eventstream.BraidEventStream;
import io.wispforest.owo.braid.widgets.focus.FocusClickArea;
import io.wispforest.owo.braid.widgets.focus.RootFocusScope;
import io.wispforest.owo.braid.widgets.inspector.BraidInspector;
import io.wispforest.owo.braid.widgets.inspector.InstancePicker;
import io.wispforest.owo.util.EventSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;

public class AppState implements InstanceHost, ProxyHost {

    public final @Nullable Logger logger;
    private final Minecraft client;

    public final Surface surface;
    public final EventBinding eventBinding;

    private final BuildScope rootBuildScope = new BuildScope();
    private Deque<AnimationCallback> animationCallbacks = new LinkedList<>();
    private final PriorityQueue<ScheduledCallback> callbacks = new PriorityQueue<>();
    private Deque<Runnable> postLayoutCallbacks = new LinkedList<>();
    private final String name;
    private final RootProxy root;

    private final Vector2d cursorPosition = new Vector2d();

    private Set<MouseListener> hovered = new HashSet<>();
    private final WeakHashMap<MouseListener, MousePosition> mousePositions = new WeakHashMap<>();
    private @Nullable MouseListener dragging = null;
    private @Nullable CursorStyle draggingCursorStyle = null;
    private int draggingButton = -1;
    private KeyModifiers draggingModifiers = null;
    private boolean dragStarted = false;

    private static final Duration MIN_GRACE_PERIOD = Duration.ofMillis(200);
    private static final Duration MAX_GRACE_PERIOD = Duration.ofMillis(500);
    private static final int SCROLL_MOVEMENT_THRESHOLD = 5;

    private @Nullable HitTestState scrollHit = null;
    private Vector2d scrollPos = new Vector2d();
    private Instant lastScrollTime = Instant.EPOCH;

    private final BraidEventStream<RootFocusScope.KeyDownEvent> keyDownStream = new BraidEventStream<>();
    private final BraidEventStream<RootFocusScope.KeyUpEvent> keyUpStream = new BraidEventStream<>();
    private final BraidEventStream<RootFocusScope.CharEvent> charStream = new BraidEventStream<>();

    private final BraidHotReloadCallback.Listener reloadListener;
    private final EventSource<?>.Subscription resizeSubscription;
    private final List<Runnable> onTerminate = new ArrayList<>();
    private boolean running = true;

    private final BraidInspector inspector = new BraidInspector(this);

    public AppState(
        @Nullable Logger logger,
        @Nullable String name,
        Minecraft client,
        Surface surface,
        EventBinding eventBinding,
        Widget root
    ) {
        this.logger = logger;
        this.client = client;

        this.surface = surface;
        this.eventBinding = eventBinding;

        this.name = name != null ? name : root.getClass().getName();
        this.root = new RootWidget(
            new AppWidget(
                this,
                new InstancePicker(
                    this.inspector.onPick(),
                    this.inspector::revealInstance,
                    new RootFocusScope(
                        this.keyDownStream.source(),
                        this.keyUpStream.source(),
                        this.charStream.source(),
                        new UserRoot(
                            widgetProxy -> inspector.rootProxy = widgetProxy,
                            widgetInstance -> inspector.rootInstance = widgetInstance,
                            root
                        )
                    )
                )
            ),
            this.rootBuildScope
        ).proxy();
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

    public void onTerminate(Runnable callback) {
        this.onTerminate.add(callback);
    }

    public void scheduleShutdown() {
        this.running = false;
        this.onTerminate.forEach(Runnable::run);
    }

    public void activateInspector() {
        this.inspector.activate();
    }

    private @Nullable TooltipState activeTooltip;

    public void draw(GuiGraphics graphics) {
        this.surface.beginRendering();

        graphics.push();
        this.rootInstance().transform.transformToParent(graphics.pose());

        var braidContext = BraidGraphics.create(graphics, this.surface);

        GlStateManager._enableScissorTest();
        this.rootInstance().draw(braidContext);
        GlStateManager._disableScissorTest();

        if (this.activeTooltip != null) {
            if (this.activeTooltip.components() != null) braidContext.drawTooltip(this.client.font, this.activeTooltip.x(), this.activeTooltip.y(), this.activeTooltip.components());
            if (this.activeTooltip.style() != null) graphics.renderComponentHoverEffect(this.client.font, this.activeTooltip.style(), this.activeTooltip.x(), this.activeTooltip.y());
        }

        graphics.pop();

        this.surface.endRendering();
    }

    public void processEvents(float frameDeltaInTicks) {
        this.pollAndDispatchEvents();

        var state = this.hitTest();

        var tooltipSupplier = state.firstWhere(hit -> hit.instance() instanceof TooltipProvider);
        if (tooltipSupplier != null) {
            var tooltip = (TooltipProvider) tooltipSupplier.instance();
            var components = tooltip.getTooltipComponentsAt(tooltipSupplier.x(), tooltipSupplier.y());
            var style = tooltip.getStyleAt(tooltipSupplier.x(), tooltipSupplier.y());

            if (components != null || style != null) this.activeTooltip = new TooltipState(components, style, (int) this.cursorPosition.x, (int) this.cursorPosition.y);
        } else {
            this.activeTooltip = null;
        }

        // ---

        var nowHovered = new HashSet<MouseListener>();
        for (var hit : Iterables.filter(state.occludedTrace(), hit -> hit.instance() instanceof MouseListener)) {
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
        }

        for (var noLongerHovered : this.hovered) {
            noLongerHovered.onMouseExit();
        }

        this.hovered = nowHovered;

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
                callbacksForThisFrame.poll().run(Duration.ofMillis((long) (frameDeltaInTicks * 50)));
            }
        }

        var now = Instant.now();
        while (!this.callbacks.isEmpty() && this.callbacks.peek().after().isBefore(now)) {
            this.callbacks.poll().callback().run();
        }

        var anyTreeMutations = false;

        anyTreeMutations |= this.rootBuildScope.rebuildDirtyProxies();
        anyTreeMutations |= this.flushLayoutQueue();

        if (anyTreeMutations) {
            this.inspector.refresh();
        }

        if (!this.postLayoutCallbacks.isEmpty()) {
            var callbacksForThisFrame = this.postLayoutCallbacks;
            this.postLayoutCallbacks = new LinkedList<>();

            while (!callbacksForThisFrame.isEmpty()) {
                callbacksForThisFrame.poll().run();
            }
        }
    }

    private void pollAndDispatchEvents() {
        var events = this.eventBinding.poll();

        for (var slot : events) {
            switch (slot.event) {
                case MouseButtonPressEvent(int button, KeyModifiers modifiers) -> {
                    this.scrollHit = null;
                    var state = this.hitTest();

                    state.firstWhere(hit -> {
                        if (!(hit.instance() instanceof FocusClickArea.Instance instance)) return false;

                        instance.widget().clickCallback.run();
                        return true;
                    });

                    var clicked = state.firstWhere(
                        (hit) -> hit.instance() instanceof MouseListener && ((MouseListener) hit.instance()).onMouseDown(hit.x(), hit.y(), button, modifiers)
                    );

                    if (clicked != null) {
                        slot.markHandled();

                        if (this.dragging == null) {
                            this.dragging = (MouseListener) clicked.instance();
                            this.draggingCursorStyle = ((MouseListener) clicked.instance()).cursorStyleAt(
                                clicked.x(),
                                clicked.y()
                            );
                            this.dragStarted = false;
                            this.draggingButton = button;
                            this.draggingModifiers = modifiers;
                        }
                    }
                }
                case MouseMoveEvent(double x, double y) -> {
                    slot.markHandled();

                    var deltaX = x - this.cursorPosition.x;
                    var deltaY = y - this.cursorPosition.y;
                    if (deltaX == 0 && deltaY == 0) break;

                    this.cursorPosition.x = x;
                    this.cursorPosition.y = y;
                    if (this.cursorPosition.distance(this.scrollPos) > SCROLL_MOVEMENT_THRESHOLD) this.scrollHit = null;

                    if (!(this.dragging instanceof WidgetInstance<?>)) break;

                    if (!this.dragStarted) {
                        this.dragging.onMouseDragStart(draggingButton, draggingModifiers);
                        this.dragStarted = true;
                    }

                    var globalTransform = ((WidgetInstance<?>) this.dragging).computeGlobalTransform();
                    var coordinates = new Vector2f((float) x, (float) y);
                    globalTransform.transformPosition(coordinates);

                    // apply *only the rotation* of the instance's transform
                    // to the mouse movement
                    var delta = new Vector2f((float) deltaX, (float) deltaY);
                    globalTransform.transformDirection(delta);

                    this.dragging.onMouseDrag(coordinates.x, coordinates.y, delta.x, delta.y);
                }
                case MouseButtonReleaseEvent(int button, KeyModifiers modifiers) -> {
                    this.scrollHit = null;
                    var state = this.hitTest();
                    var unClicked = state.firstWhere(
                        (hit) -> hit.instance() instanceof MouseListener && ((MouseListener) hit.instance()).onMouseUp(hit.x(), hit.y(), button, modifiers)
                    );

                    if (unClicked != null) {
                        slot.markHandled();
                    }

                    if (this.draggingButton == button) {
                        if (this.dragStarted && this.dragging != null) {
                            this.dragging.onMouseDragEnd();
                        }

                        this.dragging = null;
                    }
                }
                case MouseScrollEvent(double xOffset, double yOffset) -> {
                    var now = Instant.now();
                    var grace = this.cursorPosition.distance(this.scrollPos) > SCROLL_MOVEMENT_THRESHOLD ? MIN_GRACE_PERIOD : MAX_GRACE_PERIOD;
                    if (this.scrollHit == null || now.minus(grace).isAfter(this.lastScrollTime) ) this.scrollHit = this.hitTest();
                    this.lastScrollTime = now;
                    this.scrollPos = new Vector2d(this.cursorPosition);
                    var scrolled = this.scrollHit.firstWhere(
                        (hit) -> hit.instance() instanceof MouseListener &&
                            ((MouseListener) hit.instance()).onMouseScroll(
                                hit.x(),
                                hit.y(),
                                xOffset,
                                yOffset
                            )
                    );

                    if (scrolled != null) {
                        slot.markHandled();
                    }
                }
                case KeyPressEvent(int keyCode, int scancode, KeyModifiers modifiers) -> {
                    if (keyCode == GLFW.GLFW_KEY_R && modifiers.shift() && modifiers.alt()) {
                        this.rebuildRoot();
                        slot.markHandled();

                        break;
                    }

                    if (keyCode == GLFW.GLFW_KEY_I && modifiers.ctrl() && modifiers.shift()) {
                        this.inspector.activate();
                        slot.markHandled();

                        break;
                    }

                    var event = new RootFocusScope.KeyDownEvent(keyCode, modifiers);
                    this.keyDownStream.sink().onEvent(event);

                    if (event.handled()) {
                        slot.markHandled();
                    }
                }
                case KeyReleaseEvent(int keycode, int scancode, KeyModifiers modifiers) -> {
                    var event = new RootFocusScope.KeyUpEvent(keycode, modifiers);
                    this.keyUpStream.sink().onEvent(event);

                    if (event.handled()) {
                        slot.markHandled();
                    }
                }
                case CharInputEvent(char codepoint, KeyModifiers modifiers) -> {
                    var event = new RootFocusScope.CharEvent(codepoint, modifiers);
                    this.charStream.sink().onEvent(event);

                    if (event.handled()) {
                        slot.markHandled();
                    }
                }
                case FilesDroppedEvent filesDroppedEvent -> {}
                case CloseEvent ignored -> {
                    slot.markHandled();
                    this.scheduleShutdown();
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
        this.inspector.close();

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

    @Override
    public Minecraft client() {
        return this.client;
    }

    public SingleChildWidgetInstance<?> rootInstance() {
        return this.root.instance();
    }

    // ---

    private List<WidgetInstance<?>> layoutQueue = new ArrayList<>();
    private boolean mergeToLayoutQueue = false;

    private boolean flushLayoutQueue() {
        if (this.layoutQueue.isEmpty()) return false;

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

        return true;
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
        this.animationCallbacks.offer(callback);
    }

    @Override
    public long scheduleDelayedCallback(Duration delay, Runnable callback) {
        var id = ScheduledCallback.nextId++;
        this.callbacks.add(new ScheduledCallback(
            Instant.now().plus(delay),
            callback, id
        ));
        return id;
    }

    @Override
    public void cancelDelayedCallback(long id) {
        this.callbacks.removeIf(scheduledCallback -> scheduledCallback.id() == id);
    }

    @Override
    public void schedulePostLayoutCallback(Runnable callback) {
        this.postLayoutCallbacks.offer(callback);
    }

    @Override
    public Vector2dc cursorPosition() {
        return this.cursorPosition;
    }

    @Override
    public String toString() {
        return String.format("%s (AppState@%s)", this.name, Integer.toHexString(hashCode()));
    }

    // ---

    public static String formatName(String category, Widget userRoot) {
        var classPath = userRoot.getClass().getName().split("\\.");
        return String.format("%s[%s]", category, classPath[classPath.length - 1]);
    }

    public static String formatName(String category, Widget userRoot, String... attributes) {
        var classPath = userRoot.getClass().getName().split("\\.");
        return String.format("%s[%s, %s]", category, String.join(", ", attributes), classPath[classPath.length - 1]);
    }

    public static AppState of(BuildContext context) {
        //noinspection DataFlowIssue
        return context.getAncestor(AppWidget.class).app;
    }
}

record ScheduledCallback(Instant after, Runnable callback, long id) implements Comparable<ScheduledCallback> {
    //"fuck you we starting at 7" -chyz
    public static long nextId = 7;

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

class UserRoot extends VisitorWidget {

    public final Consumer<WidgetProxy> proxyCallback;
    public final Consumer<WidgetInstance<?>> instanceCallback;

    public UserRoot(Consumer<WidgetProxy> proxyCallback, Consumer<WidgetInstance<?>> instanceCallback, Widget child) {
        super(child);
        this.proxyCallback = proxyCallback;
        this.instanceCallback = instanceCallback;
    }

    private static final Visitor<UserRoot> VISITOR = (widget, instance) -> {
        widget.instanceCallback.accept(instance);
    };

    @Override
    public Proxy<?> proxy() {
        var proxy = new Proxy<>(this, VISITOR);
        this.proxyCallback.accept(proxy);

        return proxy;
    }
}

class AppWidget extends InheritedWidget {

    public final AppState app;

    protected AppWidget(AppState app, Widget child) {
        super(child);
        this.app = app;
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        if (((AppWidget) newWidget).app != this.app) {
            throw new UnsupportedOperationException("changing the AppState of a widget tree is not supported");
        }

        return false;
    }
}

record TooltipState(@Nullable List<ClientTooltipComponent> components, @Nullable Style style, int x, int y) {}

record MousePosition(double x, double y) {
    public static final MousePosition ORIGIN = new MousePosition(0, 0);
}
