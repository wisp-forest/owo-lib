package io.wispforest.owo.braid.widgets.focus;

import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.StatefulProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.CustomDraw;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FocusScope extends Focusable {

    public FocusScope(WidgetSetupCallback<FocusScope> setupCallback, Widget child) {
        super(widget -> setupCallback.setup((FocusScope) widget), child);
    }

    @Override
    public WidgetProxy proxy() {
        return new FocusScopeProxy(this);
    }

    @Override
    public WidgetState<FocusScope> createState() {
        return new State();
    }

    public static class State extends Focusable.State<FocusScope> {

        Supplier<List<Focusable.State<?>>> descendants;

        private List<Focusable.State<?>> focusedDescendants = new ArrayList<>();
        private @Nullable FocusEntry previousPrimaryFocus;
        private final Deque<FocusEntry> previouslyFocusedScopes = new ArrayDeque<>();

        public void updateFocus(@Nullable Focusable.State<?> primary, @Nullable FocusLevel level) {
            var currentPrimaryFocus = !this.focusedDescendants.isEmpty() ? this.focusedDescendants.getFirst() : null;
            if (primary == currentPrimaryFocus && (primary != null ? primary.level : null) == level) {
                return;
            }

            if (level != null && primary != null) {
                this.requestFocus(level);
            }

            var nowFocused = primary != null
                ? Stream.concat(Stream.of(primary), primary.ancestors()).takeWhile(state -> state != this).collect(Collectors.toList())
                : new ArrayList<Focusable.State<?>>();

            for (var state : nowFocused) {
                if (this.focusedDescendants.contains(state)) {
                    this.focusedDescendants.remove(state);

                    if (state.level != level) {
                        state.onFocusChange(level);
                    }
                } else {
                    state.onFocusChange(level);
                }
            }

            if (!this.focusedDescendants.isEmpty() && this.focusedDescendants.getFirst() instanceof State scope && !nowFocused.contains(scope)) {
                this.previouslyFocusedScopes.add(new FocusEntry(scope, scope.level));
            } else if (nowFocused.isEmpty() || !(nowFocused.getFirst() instanceof State)) {
                previouslyFocusedScopes.clear();
            }

            for (var noLongerFocused : this.focusedDescendants) {
                noLongerFocused.onFocusChange(null);
            }

            this.focusedDescendants = nowFocused;
        }

        void onFocusableDisposed(Focusable.State<?> descendant) {
            if (!this.focusedDescendants.isEmpty() && descendant == this.focusedDescendants.getFirst() && !this.previouslyFocusedScopes.isEmpty()) {
                var entry = this.previouslyFocusedScopes.removeLast();
                updateFocus(entry.state(), entry.level());
            }

            this.focusedDescendants.remove(descendant);
            this.previouslyFocusedScopes.removeIf(entry -> entry.state() == descendant);
        }

        @Override
        public Focusable.State<?> primaryFocus() {
            if (this.level != null) {
                var candidate = !this.focusedDescendants.isEmpty() ? this.focusedDescendants.getFirst() : null;
                if (candidate instanceof State) candidate = candidate.primaryFocus();

                return candidate != null ? candidate : this;
            } else {
                return super.primaryFocus();
            }
        }

        @Override
        public void traverseFocus(FocusTraversalDirection direction) {
            var descendants = this.descendants.get();

            var searchStartIdx = !this.focusedDescendants.isEmpty()
                ? descendants.indexOf(this.focusedDescendants.getFirst())
                : (direction == FocusTraversalDirection.BACKWARDS ? 0 : -1);
            var offset = direction == FocusTraversalDirection.BACKWARDS ? -1 : 1;

            var nextFocusIdx = searchStartIdx;
            do {
                nextFocusIdx = (nextFocusIdx + offset) % descendants.size();
            } while (descendants.get(nextFocusIdx).widget().skipTraversal());

            this.updateFocus(descendants.get(nextFocusIdx), FocusLevel.HIGHLIGHT);
        }

        @Override
        void onFocusChange(@Nullable FocusLevel newLevel) {
            var previousLevel = this.level;
            super.onFocusChange(newLevel);

            if (previousLevel != null && newLevel == null) {
                var primaryFocus = !this.focusedDescendants.isEmpty() ? this.focusedDescendants.getFirst() : null;
                this.previousPrimaryFocus = primaryFocus != null ? new FocusEntry(primaryFocus, primaryFocus.level) : null;

                this.updateFocus(null, null);
            } else if (previousLevel == null && newLevel != null && this.previousPrimaryFocus != null) {
                this.updateFocus(this.previousPrimaryFocus.state(), this.previousPrimaryFocus.level());
            }
        }

        @Override
        boolean onKeyDown(int keyCode, KeyModifiers modifiers) {
            for (var descendant : this.focusedDescendants) {
                if (descendant.onKeyDown(keyCode, modifiers)) {
                    return true;
                }
            }

            // TODO(glisco): replace with intents
            if (keyCode == GLFW.GLFW_KEY_TAB) {
                this.traverseFocus(modifiers.shift() ? FocusTraversalDirection.BACKWARDS : FocusTraversalDirection.FORWARDS);
                return true;
            }

            return super.onKeyDown(keyCode, modifiers);
        }

        @Override
        boolean onKeyUp(int keyCode, KeyModifiers modifiers) {
            for (var descendant : this.focusedDescendants) {
                if (descendant.onKeyUp(keyCode, modifiers)) {
                    return true;
                }
            }

            return super.onKeyUp(keyCode, modifiers);
        }

        @Override
        boolean onChar(int charCode, KeyModifiers modifiers) {
            for (var descendant : this.focusedDescendants) {
                if (descendant.onChar(charCode, modifiers)) {
                    return true;
                }
            }

            return super.onChar(charCode, modifiers);
        }

        @Override
        void onClick() {
            super.onClick();
            this.updateFocus(null, null);
        }

        @Override
        public Widget build(BuildContext context) {
            return new Stack(
                new StackBase(
                    new FocusStateProvider<>(
                        this,
                        State.class,
                        this.level,
                        super.build(context)
                    )
                ),
                new CustomDraw((ctx, transform) -> {
                    if (this.focusedDescendants.isEmpty()) return;

                    var instance = this.focusedDescendants.getFirst().context().instance();
                    var drawTransform = instance.parent().computeTransformFrom(this.context().instance()).invert();

                    var boxMin = drawTransform.transformPosition(instance.transform.aabb().getMinPos().toVector3f());
                    var boxMax = drawTransform.transformPosition(instance.transform.aabb().getMaxPos().toVector3f());

                    var box = new Box(new Vec3d(boxMin), new Vec3d(boxMax));

                    ctx.push();
                    ctx.translate(box.minX, box.minY, box.minZ);

                    NinePatchTexture.draw(
                        Identifier.of("owo", "braid_debug_focused"),
                        ctx,
                        0, 0, (int) (box.maxX - box.minX), (int) (box.maxY - box.minY),
                        Color.ofHsv(this.focusedDescendants.getFirst().debugDepth() / 8f % 1f, .75f, 1)
                    );

                    ctx.pop();
                })
            );
        }

        // ---

        static @Nullable FocusScope.State maybeOf(BuildContext context) {
            var provider = context.getAncestor(FocusStateProvider.class, FocusStateProvider.keyOf(State.class));
            if (provider == null) return null;

            return (State) provider.state;
        }
    }
}

class FocusScopeProxy extends StatefulProxy {
    public FocusScopeProxy(FocusScope widget) {
        super(widget);
    }

    @Override
    public void mount(WidgetProxy parent, @Nullable Object slot) {
        super.mount(parent, slot);
        ((FocusScope.State) this.state()).descendants = () -> {
            var descendants = new ArrayList<Focusable.State<?>>();
            this.visitChildren(child -> collectFocusDescendants(child, descendants));

            return descendants;
        };
    }

    private static void collectFocusDescendants(WidgetProxy proxy, List<Focusable.State<?>> into) {
        if (proxy instanceof StatefulProxy stateful && stateful.state() instanceof Focusable.State<?> state) {
            into.add(state);

            if (state instanceof FocusScope.State) {
                return;
            }
        }

        proxy.visitChildren(child -> {
            collectFocusDescendants(child, into);
        });
    }
}

record FocusEntry(Focusable.State<?> state, FocusLevel level) {}