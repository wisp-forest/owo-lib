package io.wispforest.owo.braid.core;

import com.google.common.collect.Streams;
import io.wispforest.owo.braid.core.cursor.CursorController;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.instance.*;
import io.wispforest.owo.braid.framework.proxy.BuildScope;
import io.wispforest.owo.braid.framework.proxy.ProxyHost;
import io.wispforest.owo.braid.framework.proxy.SingleChildInstanceWidgetProxy;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AppState implements InstanceHost, ProxyHost {

    public final @Nullable Logger logger;
    private final MinecraftClient client;
    public final CursorController cursorController;

    private final BuildScope rootBuildScope = new BuildScope();
    private Deque<AnimationCallback> callbacks = new ArrayDeque<>();
    private final RootProxy root;

    private Set<MouseListener> hovered = new HashSet<>();
    private @Nullable MouseListener dragging = null;
    private @Nullable CursorStyle draggingCursorStyle = null;
    private boolean dragStarted = false;
    private @Nullable KeyboardListener focused = null;

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

    public void draw(DrawContext ctx) {
        ctx.push();
        this.rootInstance().transform.transformToParent(ctx.getMatrices());
        this.rootInstance().draw(ctx);
        ctx.pop();
    }

    public void updateWidgetsAndInteractions(double mouseX, double mouseY, float partialTicks, float frameDeltaInTicks) {
        if (this.reloadListener.poll()) {
            this.rebuildRoot();
        }

        if (!this.callbacks.isEmpty()) {
            var callbacksForThisFrame = this.callbacks;
            this.callbacks = new ArrayDeque<>();

            while (!callbacksForThisFrame.isEmpty()) {
                var callback = callbacksForThisFrame.removeFirst();
                callback.run(frameDeltaInTicks);
            }
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

    public boolean dispatchMouseDownEvent(double x, double y) {
        var state = this.hitTest(x, y);

        var clicked = state.firstWhere(
            (hit) -> hit.instance() instanceof MouseListener && ((MouseListener) hit.instance()).onMouseDown(hit.x(), hit.y())
        );

        if (clicked != null) {
            this.dragging = (MouseListener) clicked.instance();
            this.draggingCursorStyle = ((MouseListener) clicked.instance()).cursorStyleAt(
                clicked.x(),
                clicked.y()
            );
            this.dragStarted = false;
        }

        var focusHit = state.firstWhere((hit) -> hit.instance() instanceof KeyboardListener);
        var nowFocused = focusHit != null ? (KeyboardListener) focusHit.instance() : null;

        if (nowFocused != this.focused) {
            if (this.focused != null) this.focused.onFocusLost();
            this.focused = nowFocused;
            if (this.focused != null) this.focused.onFocusGained();
        }

        return clicked != null || focusHit != null;
    }

    public boolean dispatchMouseDragEvent(double deltaX, double deltaY) {
        if (!(this.dragging instanceof WidgetInstance<?>)) return false;

        if (!this.dragStarted) {
            this.dragging.onMouseDragStart();
            this.dragStarted = true;
        }

        var globalTransform = ((WidgetInstance<?>) this.dragging).computeGlobalTransform();
        var coordinates = new Vector4f((float) this.client.mouse.getX(), (float) this.client.mouse.getY(), 0, 1);
        globalTransform.transform(coordinates);

        // apply *only the rotation* of the instance's transform
        // to the mouse movement
        var delta = new Vector4f((float) deltaX, (float) deltaY, 0, 0);
        globalTransform.transform(delta);

        this.dragging.onMouseDrag(coordinates.x, coordinates.y, delta.x, delta.y);
        return true;
    }

    public boolean dispatchMouseUpEvent() {
        var consumed = false;

        if (this.dragStarted && this.dragging != null) {
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
        var consumed = false;
        if (keyCode == GLFW.GLFW_KEY_R && (modifiers & (GLFW.GLFW_MOD_SHIFT | GLFW.GLFW_MOD_ALT)) != 0) {
            this.rebuildRoot();
            consumed = true;
        }

        if (this.focused != null) this.focused.onKeyDown(keyCode, modifiers);
        return this.focused != null || consumed;
    }

    public boolean dispatchKeyUpEvent(int keyCode, int modifiers) {
        if (this.focused != null) this.focused.onKeyUp(keyCode, modifiers);
        return this.focused != null;
    }

    public boolean dispatchCharEvent(int charCode, int modifiers) {
        if (this.focused != null) this.focused.onChar(charCode, modifiers);
        return this.focused != null;
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
        this.callbacks.add(callback);
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