package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.braid.core.BraidWindow;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;
import io.wispforest.owo.braid.widgets.eventstream.BraidEventSource;
import io.wispforest.owo.braid.widgets.eventstream.BraidEventStream;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BraidInspector {

    public final AppState subject;
    public WidgetProxy rootProxy;
    public WidgetInstance<?> rootInstance;

    private final BraidEventStream<Unit> refreshEvents = new BraidEventStream<>();
    private final BraidEventStream<PickEvent> pickEvents = new BraidEventStream<>();
    private final BraidEventStream<RevealEvent> revealEvents = new BraidEventStream<>();

    private boolean active = false;
    @Nullable AppState currentApp;
    @Nullable BraidWindow currentWindow;

    public BraidInspector(AppState subject) {
        this.subject = subject;
    }

    public BraidEventSource<PickEvent> onPick() {
        return this.pickEvents.source();
    }

    public void pick(PickEvent event) {
        this.pickEvents.sink().onEvent(event);
        if (currentWindow != null && event == PickEvent.START) GLFW.glfwIconifyWindow(currentWindow.handle);
    }

    public BraidEventSource<Unit> onRefresh() {
        return this.refreshEvents.source();
    }

    public BraidEventSource<RevealEvent> onReveal() {
        return this.revealEvents.source();
    }

    public void activate() {
        if (this.rootProxy == null || this.rootInstance == null) {
            throw new IllegalStateException("cannot activate the braid inspector before the root proxy and instance have been set");
        }

        if (this.currentApp != null) {
            GLFW.glfwShowWindow(this.currentWindow.handle);
            return;
        }

        if (this.active) return;
        this.active = true;

        var result = BraidWindow.open(
            "braid inspector",
            900,
            550,
            new InspectorWidget(this.rootProxy, this.rootInstance, this)
        );

        GLFW.glfwSetWindowAttrib(result.window().handle, GLFW.GLFW_FLOATING, GLFW.GLFW_TRUE);

        this.currentApp = result.state();
        this.currentWindow = result.window();

        this.currentApp.onTerminate(() -> {
            this.pick(PickEvent.STOP);
            this.currentApp = null;
            this.currentWindow = null;
            this.active = false;
        });
    }

    public void revealInstance(WidgetInstance<?> instance) {
        if (!this.active) return;
        this.revealEvents.sink().onEvent(new RevealEvent(instance));
        if (currentWindow != null) GLFW.glfwShowWindow(currentWindow.handle);
    }

    public void refresh() {
        this.refreshEvents.sink().onEvent(Unit.INSTANCE);
    }

    public void close() {
        if (this.currentApp == null) return;
        this.currentApp.scheduleShutdown();
    }

    public enum PickEvent {START, STOP}
}
