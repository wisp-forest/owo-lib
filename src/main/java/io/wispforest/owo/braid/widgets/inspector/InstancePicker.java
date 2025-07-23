package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.instance.HitTestState;
import io.wispforest.owo.braid.framework.instance.WidgetInstance;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Builder;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import io.wispforest.owo.util.EventSource;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class InstancePicker extends StatefulWidget {

    public final EventSource<BraidEventStream.Listener<Unit>> activateEvents;
    public final PickCallback pickCallback;
    public final Widget child;

    public InstancePicker(EventSource<BraidEventStream.Listener<Unit>> activateEvents, PickCallback pickCallback, Widget child) {
        this.activateEvents = activateEvents;
        this.pickCallback = pickCallback;
        this.child = child;
    }

    @Override
    public WidgetState<InstancePicker> createState() {
        return new State();
    }

    public static class State extends StreamListenerState<InstancePicker> {

        private BuildContext childContext;
        private @Nullable WidgetInstance<?> pickedInstance;

        private boolean picking = false;

        @Override
        public void init() {
            this.streamListen(widget -> widget.activateEvents, unit -> {
                this.setState(() -> this.picking = true);
            });
        }

        @Override
        public Widget build(BuildContext context) {
            var children = new ArrayList<Widget>();

            children.add(new StackBase(
                new Builder(childContext -> {
                    this.childContext = childContext;
                    return this.widget().child;
                })
            ));

            if (this.picking) {
                children.add(new MouseArea(
                    widget -> widget
                        .moveCallback((toX, toY) -> {
                            var hitTest = new HitTestState();
                            this.childContext.instance().hitTest(toX, toY, hitTest);

                            if (this.pickedInstance != null) this.pickedInstance.debugHighlighted = false;

                            this.pickedInstance = hitTest.anyHit() ? hitTest.firstHit().instance() : null;
                            if (this.pickedInstance != null) this.pickedInstance.debugHighlighted = true;
                        })
                        .clickCallback((x, y, button) -> {
                            if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                                if (this.pickedInstance != null) {
                                    this.pickedInstance.debugHighlighted = false;
                                    this.widget().pickCallback.onPick(this.pickedInstance);
                                }

                                this.setState(() -> this.picking = false);
                            }

                            return true;
                        }),
                    new Padding(Insets.none())
                ));
            }

            return new Stack(children);
        }
    }

    @FunctionalInterface
    public interface PickCallback {
        void onPick(WidgetInstance<?> pickedInstance);
    }
}