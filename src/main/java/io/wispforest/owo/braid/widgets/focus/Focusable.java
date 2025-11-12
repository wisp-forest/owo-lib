package io.wispforest.owo.braid.widgets.focus;

import com.google.common.base.Preconditions;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

public class Focusable extends StatefulWidget {

    @Nullable private KeyDownCallback keyDownCallback;
    @Nullable private KeyUpCallback keyUpCallback;
    @Nullable private CharCallback charCallback;

    @Nullable private FocusGainedCallback focusGainedCallback;
    @Nullable private FocusLostCallback focusLostCallback;
    @Nullable private FocusLevelChangedCallback focusLevelChangedCallback;

    private boolean skipTraversal;
    private boolean autoFocus;
    @Nullable private Boolean clickFocus;

    public final Widget child;

    public Focusable(WidgetSetupCallback<Focusable> setupCallback, Widget child) {
        setupCallback.setup(this);
        this.child = child;
    }

    // ---

    public static @Nullable State<?> maybeOf(BuildContext context) {
        var provider = context.getAncestor(FocusStateProvider.class, FocusStateProvider.keyOf(State.class));
        if (provider == null) return null;

        return provider.state;
    }

    public static State<?> of(BuildContext context) {
        var state = maybeOf(context);
        Preconditions.checkNotNull(state, "attempted to look up the closest Focusable without one present");

        return state;
    }

    public static @Nullable FocusLevel levelOf(BuildContext context) {
        var provider = context.dependOnAncestor(FocusStateProvider.class, FocusStateProvider.keyOf(State.class));
        return provider != null ? provider.level : null;
    }

    public static boolean isFocused(BuildContext context) {
        return levelOf(context) != null;
    }

    public static boolean shouldShowHighlight(BuildContext context) {
        return levelOf(context) == FocusLevel.HIGHLIGHT;
    }

    // ---

    public Focusable keyDownCallback(@Nullable KeyDownCallback keyDownCallback) {
        this.assertMutable();
        this.keyDownCallback = keyDownCallback;
        return this;
    }

    public @Nullable KeyDownCallback keyDownCallback() {
        return this.keyDownCallback;
    }

    public Focusable keyUpCallback(@Nullable KeyUpCallback keyUpCallback) {
        this.assertMutable();
        this.keyUpCallback = keyUpCallback;
        return this;
    }

    public @Nullable KeyUpCallback keyUpCallback() {
        return this.keyUpCallback;
    }

    public Focusable charCallback(@Nullable CharCallback charCallback) {
        this.assertMutable();
        this.charCallback = charCallback;
        return this;
    }

    public @Nullable CharCallback charCallback() {
        return this.charCallback;
    }

    public Focusable focusGainedCallback(@Nullable FocusGainedCallback focusGainedCallback) {
        this.assertMutable();
        this.focusGainedCallback = focusGainedCallback;
        return this;
    }

    public @Nullable FocusGainedCallback focusGainedCallback() {
        return this.focusGainedCallback;
    }

    public Focusable focusLostCallback(@Nullable FocusLostCallback focusLostCallback) {
        this.assertMutable();
        this.focusLostCallback = focusLostCallback;
        return this;
    }

    public @Nullable FocusLostCallback focusLostCallback() {
        return this.focusLostCallback;
    }

    public Focusable focusLevelChangedCallback(@Nullable FocusLevelChangedCallback focusLevelChangedCallback) {
        this.assertMutable();
        this.focusLevelChangedCallback = focusLevelChangedCallback;
        return this;
    }

    public @Nullable FocusLevelChangedCallback focusLevelChangedCallback() {
        return this.focusLevelChangedCallback;
    }

    public Focusable skipTraversal(boolean skipTraversal) {
        this.assertMutable();
        this.skipTraversal = skipTraversal;
        return this;
    }

    public boolean skipTraversal() {
        return this.skipTraversal;
    }

    public Focusable autoFocus(boolean autoFocus) {
        this.assertMutable();
        this.autoFocus = autoFocus;
        return this;
    }

    public boolean autoFocus() {
        return this.autoFocus;
    }

    public Focusable clickFocus(@Nullable Boolean clickFocus) {
        this.assertMutable();
        this.clickFocus = clickFocus;
        return this;
    }

    public @Nullable Boolean clickFocus() {
        return this.clickFocus;
    }

    @Override
    public WidgetState<?> createState() {
        return new State<>();
    }

    @FunctionalInterface
    public interface KeyDownCallback {
        boolean onKeyDown(int keyCode, KeyModifiers modifiers);
    }

    @FunctionalInterface
    public interface KeyUpCallback {
        boolean onKeyUp(int keyCode, KeyModifiers modifiers);
    }

    @FunctionalInterface
    public interface CharCallback {
        boolean onChar(int charCode, KeyModifiers modifiers);
    }

    @FunctionalInterface
    public interface FocusGainedCallback {
        void onFocusGained();
    }

    @FunctionalInterface
    public interface FocusLostCallback {
        void onFocusLost();
    }

    @FunctionalInterface
    public interface FocusLevelChangedCallback {
        void onFocusLevelChanged(@Nullable FocusLevel level);
    }

    public static class State<F extends Focusable> extends WidgetState<F> {

        private @Nullable State<?> parent;
        private @Nullable FocusScope.State scope;
        @Nullable FocusLevel level;

        private int debugDepth;

        public int debugDepth() {
            return this.debugDepth;
        }
        
        public State<?> primaryFocus() {
            return this.scope != null ? this.scope.primaryFocus() : this;
        }

        public Stream<State<?>> ancestors() {
            return Stream.iterate(
                this.parent,
                Objects::nonNull,
                state -> state.parent
            );
        }

        public void requestFocus() {
            this.requestFocus(FocusLevel.HIGHLIGHT);
        }

        public void requestFocus(FocusLevel level) {
            if (this.scope != null) {
                this.scope.updateFocus(this, level);
            }
        }

        public void unfocus() {
            if (this.scope != null) {
                this.scope.updateFocus(null, null);
            }
        }

        public void traverseFocus(FocusTraversalDirection direction) {
            if (this.scope != null) {
                this.scope.traverseFocus(direction);
            }
        }

        void onFocusChange(@Nullable FocusLevel newLevel) {
            if (Owo.DEBUG) {
                Preconditions.checkState(
                    this.level != newLevel,
                    String.format("_onFocusChange(%s) invoked on a state which is already at %s", newLevel, newLevel)
                );
            }

            if (this.widget().focusLevelChangedCallback() instanceof FocusLevelChangedCallback callback) {
                callback.onFocusLevelChanged(newLevel);
            }

            if (this.level == null && newLevel != null) {
                if (this.widget().focusGainedCallback() instanceof FocusGainedCallback callback) {
                    callback.onFocusGained();
                }
            } else if (this.level != null && newLevel == null) {
                if (this.widget().focusLostCallback() instanceof FocusLostCallback callback) {
                    callback.onFocusLost();
                }
            }

            this.setState(() -> {
                this.level = newLevel;
            });
        }

        void onClick() {
            var shouldClickFocus = this.widget().clickFocus();
            if (shouldClickFocus == Boolean.TRUE || this.context().getAncestor(FocusPolicy.class).clickFocus) {
                this.requestFocus(FocusLevel.BASE);
            }
        }

        boolean onKeyDown(int keyCode, KeyModifiers modifiers) {
            if (Owo.DEBUG) {
                Preconditions.checkState(
                    this.level != null,
                    "onKeyDown invoked on a state which is not focused"
                );
            }

            return this.widget().keyDownCallback() instanceof KeyDownCallback callback && callback.onKeyDown(keyCode, modifiers);
        }

        boolean onKeyUp(int keyCode, KeyModifiers modifiers) {
            if (Owo.DEBUG) {
                Preconditions.checkState(
                    this.level != null,
                    "onKeyUp invoked on a state which is not focused"
                );
            }

            return this.widget().keyUpCallback() instanceof KeyUpCallback callback && callback.onKeyUp(keyCode, modifiers);
        }

        boolean onChar(int charCode, KeyModifiers modifiers) {
            if (Owo.DEBUG) {
                Preconditions.checkState(
                    this.level != null,
                    "onChar invoked on a state which is not focused"
                );
            }

            return this.widget().charCallback() instanceof CharCallback callback && callback.onChar(charCode, modifiers);
        }

        @Override
        public void init() {
            this.parent = Focusable.maybeOf(this.context());
            this.scope = FocusScope.State.maybeOf(this.context());

            this.debugDepth = this.parent != null ? this.parent.debugDepth + 1 : 0;

            if (this.widget().autoFocus()) {
                this.requestFocus();
            }
        }

        @Override
        public void dispose() {
            if (this.scope != null) {
                this.scope.onFocusableDisposed(this);
            }
        }

        @Override
        public Widget build(BuildContext context) {
            return new FocusClickArea(
                this::onClick,
                new FocusStateProvider<>(
                    this,
                    Focusable.State.class,
                    this.level,
                    this.widget().child
                )
            );
        }
    }
}
