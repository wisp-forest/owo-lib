package io.wispforest.owo.braid.widgets.focus;

import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.StatefulProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetProxy;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.scroll.Scrollable;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

import java.util.*;
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

        private final Deque<Focusable.State<?>> traversalHistory = new LinkedList<>();
        private FocusTraversalDirection historyDirection = null;

        public void updateFocus(@Nullable Focusable.State<?> primary, @Nullable FocusLevel level) {
            this.updateFocus(primary, level, false);
        }

        public void updateFocus(@Nullable Focusable.State<?> primary, @Nullable FocusLevel level, boolean keepTraversalHistory) {
            var currentPrimaryFocus = !this.focusedDescendants.isEmpty() ? this.focusedDescendants.getFirst() : null;
            if (primary == currentPrimaryFocus && (primary != null ? primary.level : null) == level) {
                return;
            }

            if (!keepTraversalHistory) {
                this.traversalHistory.clear();
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

            if (primary != null) {
                var scrollable = Scrollable.maybeOf(primary.context());
                if (scrollable != null) {
                    Scrollable.reveal(primary.context());
                }
            }

            this.focusedDescendants = nowFocused;
        }

        void onFocusableDisposed(Focusable.State<?> descendant) {
            if (!this.focusedDescendants.isEmpty() && descendant == this.focusedDescendants.getFirst() && !this.previouslyFocusedScopes.isEmpty()) {
                var entry = this.previouslyFocusedScopes.removeLast();
                updateFocus(entry.state(), entry.level());
            }

            this.focusedDescendants.remove(descendant);
            this.traversalHistory.remove(descendant);
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
            switch (direction) {
                case PREVIOUS, NEXT -> this.traverseFocusLogical(direction == FocusTraversalDirection.NEXT);
                case LEFT, RIGHT, UP, DOWN -> this.traverseFocusDirectional(direction);
            }
        }

        private void traverseFocusLogical(boolean forwards) {
            var descendants = this.descendants.get();

            var searchStartIdx = !this.focusedDescendants.isEmpty()
                ? descendants.indexOf(this.focusedDescendants.getFirst())
                : (forwards ? -1 : 0);
            var offset = forwards ? 1 : -1;

            var nextFocusIdx = searchStartIdx;
            do {
                nextFocusIdx = Mth.positiveModulo(nextFocusIdx + offset, descendants.size());
            } while (descendants.get(nextFocusIdx).widget().skipTraversal());

            this.updateFocus(descendants.get(nextFocusIdx), FocusLevel.HIGHLIGHT);
        }

        private boolean tryTraverseFocusHistory(FocusTraversalDirection direction) {
            var poppedHistory = false;

            if (!this.traversalHistory.isEmpty()) {
                if (this.historyDirection == direction.opposite()) {
                    poppedHistory = true;
                    this.updateFocus(this.traversalHistory.pop(), FocusLevel.HIGHLIGHT, true);
                } else if (this.historyDirection != direction) {
                    this.traversalHistory.clear();
                }
            }

            if (!poppedHistory && !this.focusedDescendants.isEmpty()) {
                this.historyDirection = direction;
            }

            return poppedHistory;
        }

        private void traverseFocusDirectional(FocusTraversalDirection direction) {
            if (this.focusedDescendants.isEmpty() || this.tryTraverseFocusHistory(direction)) return;

            var descendants = this.descendants.get();

            var focusedBounds = this.focusedDescendants.getFirst().context().instance().computeGlobalBounds();
            var focusedCenter = FocusTraversalCandidate.of(this.focusedDescendants.getFirst()).center();

            var candidates = descendants.stream()
                .filter(state -> !state.widget().skipTraversal())
                .map(FocusTraversalCandidate::of)
                .filter(candidate -> {
                    return this.filterCandidate(candidate, focusedBounds, direction);
                })
                .collect(Collectors.toList());

            var candidatesInBand = candidates.stream()
                .filter(candidate -> {
                    return this.filterInBand(candidate, focusedBounds, direction);
                })
                .collect(Collectors.toList());

            if (!candidatesInBand.isEmpty()) {
                candidatesInBand.sort(this.sortInBand(focusedCenter, direction));

                this.traversalHistory.push(this.focusedDescendants.getFirst());
                this.updateFocus(candidatesInBand.getFirst().state(), FocusLevel.HIGHLIGHT, true);
                return;
            }

            if (!candidates.isEmpty()) {
                candidates.sort(this.sortOutOfBand(focusedCenter, direction));

                this.traversalHistory.push(this.focusedDescendants.getFirst());
                this.updateFocus(candidates.getFirst().state(), FocusLevel.HIGHLIGHT, true);
            }
        }

        private boolean filterCandidate(FocusTraversalCandidate candidate, AABB focusedBounds, FocusTraversalDirection direction) {
            return switch (direction) {
                case LEFT -> candidate.center().x <= focusedBounds.minX;
                case RIGHT -> candidate.center().x >= focusedBounds.maxX;
                case UP -> candidate.center().y <= focusedBounds.minY;
                case DOWN -> candidate.center().y >= focusedBounds.maxY;
                default -> throw new IllegalStateException();
            };
        }

        private boolean filterInBand(FocusTraversalCandidate candidate, AABB focusedBounds, FocusTraversalDirection direction) {
            return switch (direction) {
                case LEFT, RIGHT -> candidate.aabb().minY < focusedBounds.maxY && candidate.aabb().maxY > focusedBounds.minY;
                case UP, DOWN -> candidate.aabb().minX < focusedBounds.maxX && candidate.aabb().maxX > focusedBounds.minX;
                default -> throw new IllegalStateException();
            };
        }

        private Comparator<FocusTraversalCandidate> sortInBand(Vector2d focusedCenter, FocusTraversalDirection direction) {
            return switch (direction) {
                case LEFT -> Comparator.<FocusTraversalCandidate>comparingDouble(candidate -> -candidate.center().x)
                    .thenComparingDouble(candidate -> Math.abs(candidate.center().y - focusedCenter.y));
                case RIGHT -> Comparator.<FocusTraversalCandidate>comparingDouble(candidate -> candidate.center().x)
                    .thenComparingDouble(candidate -> Math.abs(candidate.center().y - focusedCenter.y));
                case UP -> Comparator.<FocusTraversalCandidate>comparingDouble(candidate -> -candidate.center().y)
                    .thenComparingDouble(candidate -> Math.abs(candidate.center().x - focusedCenter.x));
                case DOWN -> Comparator.<FocusTraversalCandidate>comparingDouble(candidate -> candidate.center().y)
                    .thenComparingDouble(candidate -> Math.abs(candidate.center().x - focusedCenter.x));
                default -> throw new IllegalStateException();
            };
        }

        private Comparator<FocusTraversalCandidate> sortOutOfBand(Vector2d focusedCenter, FocusTraversalDirection direction) {
            return switch (direction) {
                case LEFT, RIGHT -> Comparator.<FocusTraversalCandidate>comparingDouble(candidate -> Math.abs(candidate.center().y - focusedCenter.y))
                    .thenComparingDouble(candidate -> Math.abs(candidate.center().x - focusedCenter.x));
                case UP, DOWN -> Comparator.<FocusTraversalCandidate>comparingDouble(candidate -> Math.abs(candidate.center().x - focusedCenter.x))
                    .thenComparingDouble(candidate -> Math.abs(candidate.center().y - focusedCenter.y));
                default -> throw new IllegalStateException();
            };
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
                )
//                new CustomDraw((ctx, transform) -> {
//                    if (this.focusedDescendants.isEmpty()) return;
//
//                    var instance = this.focusedDescendants.getFirst().context().instance();
//                    var drawTransform = instance.parent().computeTransformFrom(this.context().instance()).invert();
//
//                    var boxMin = drawTransform.transformPosition(instance.transform.aabb().getMinPos().toVector3f());
//                    var boxMax = drawTransform.transformPosition(instance.transform.aabb().getMaxPos().toVector3f());
//
//                    var box = new Box(new Vec3d(boxMin), new Vec3d(boxMax));
//
//                    ctx.push();
//                    ctx.translate(box.minX, box.minY, box.minZ);
//
//                    NinePatchTexture.draw(
//                        Identifier.of("owo", "braid_debug_focused"),
//                        ctx,
//                        0, 0, (int) (box.maxX - box.minX), (int) (box.maxY - box.minY),
//                        Color.ofHsv(this.focusedDescendants.getFirst().debugDepth() / 8f % 1f, .75f, 1)
//                    );
//
//                    ctx.pop();
//                })
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

record FocusTraversalCandidate(Focusable.State<?> state, AABB aabb, Vector2d center) {
    public static FocusTraversalCandidate of(Focusable.State<?> state) {
        var aabb = state.context().instance().computeGlobalBounds();
        var center = new Vector2d(
            aabb.minX + (aabb.maxX - aabb.minX) / 2,
            aabb.minY + (aabb.maxY - aabb.minY) / 2
        );

        return new FocusTraversalCandidate(state, aabb, center);
    }
}