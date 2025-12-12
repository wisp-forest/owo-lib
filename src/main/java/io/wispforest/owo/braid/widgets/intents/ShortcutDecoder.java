package io.wispforest.owo.braid.widgets.intents;

import com.google.common.collect.Iterables;
import io.wispforest.owo.braid.core.BraidUtils;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.focus.Focusable;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ShortcutDecoder extends StatefulWidget {
    private @Nullable MouseArea.EnterCallback enterCallback;
    private @Nullable MouseArea.ExitCallback exitCallback;
    private @Nullable MouseArea.CursorStyleSupplier cursorStyleSupplier;

    private @Nullable Focusable.FocusGainedCallback focusGainedCallback;
    private @Nullable Focusable.FocusLostCallback focusLostCallback;
    private boolean skipTraversal = false;
    private boolean autoFocus = false;

    private final Map<List<ShortcutTrigger>, Listener> shortcuts = new LinkedHashMap<>();

    private final Widget child;

    public ShortcutDecoder(
        WidgetSetupCallback<ShortcutDecoder> setupCallback,
        Widget child
    ) {
        this.child = child;
        setupCallback.setup(this);
    }

    public ShortcutDecoder enterCallback(@Nullable MouseArea.EnterCallback enterCallback) {
        this.assertMutable();
        this.enterCallback = enterCallback;
        return this;
    }

    public @Nullable MouseArea.EnterCallback enterCallback() {
        return this.enterCallback;
    }

    public ShortcutDecoder exitCallback(@Nullable MouseArea.ExitCallback exitCallback) {
        this.assertMutable();
        this.exitCallback = exitCallback;
        return this;
    }

    public @Nullable MouseArea.ExitCallback exitCallback() {
        return this.exitCallback;
    }

    public ShortcutDecoder cursorStyleSupplier(@Nullable MouseArea.CursorStyleSupplier cursorStyleSupplier) {
        this.assertMutable();
        this.cursorStyleSupplier = cursorStyleSupplier;
        return this;
    }

    public ShortcutDecoder cursorStyle(@Nullable CursorStyle style) {
        return this.cursorStyleSupplier((x, y) -> style);
    }

    public @Nullable MouseArea.CursorStyleSupplier cursorStyleSupplier() {
        return this.cursorStyleSupplier;
    }

    public ShortcutDecoder focusGainedCallback(@Nullable Focusable.FocusGainedCallback focusGainedCallback) {
        this.assertMutable();
        this.focusGainedCallback = focusGainedCallback;
        return this;
    }

    public @Nullable Focusable.FocusGainedCallback focusGainedCallback() {
        return this.focusGainedCallback;
    }

    public ShortcutDecoder focusLostCallback(@Nullable Focusable.FocusLostCallback focusLostCallback) {
        this.assertMutable();
        this.focusLostCallback = focusLostCallback;
        return this;
    }

    public @Nullable Focusable.FocusLostCallback focusLostCallback() {
        return this.focusLostCallback;
    }

    public ShortcutDecoder skipTraversal(boolean skipTraversal) {
        this.assertMutable();
        this.skipTraversal = skipTraversal;
        return this;
    }

    public boolean skipTraversal() {
        return this.skipTraversal;
    }

    public ShortcutDecoder autoFocus(boolean autoFocus) {
        this.assertMutable();
        this.autoFocus = autoFocus;
        return this;
    }

    public boolean autoFocus() {
        return this.autoFocus;
    }

    public ShortcutDecoder shortcuts(Map<List<ShortcutTrigger>, Listener> shortcuts) {
        this.assertMutable();
        this.shortcuts.putAll(shortcuts);
        return this;
    }

    public ShortcutDecoder addShortcut(List<ShortcutTrigger> triggers, Listener action) {
        this.assertMutable();
        this.shortcuts.put(triggers, action);
        return this;
    }

    public ShortcutDecoder addShortcut(ShortcutTrigger trigger, Listener action) {
        return this.addShortcut(List.of(trigger), action);
    }

    public Map<List<ShortcutTrigger>, Listener> shortcuts() {
        return this.shortcuts;
    }

    @Override
    public WidgetState<ShortcutDecoder> createState() {
        return new State();
    }

    @FunctionalInterface
    public interface Listener {
        boolean trigger(TriggerType type);
    }

    public static class State extends WidgetState<ShortcutDecoder> {
        private List<ShortcutSequence> sequences = new ArrayList<>();

        private final List<ShortcutSequence> queuedSequences = new ArrayList<>();
        @Nullable private Long callbackId;

        @Override
        public void init() {
            this.buildSequences();
        }

        @Override
        public void didUpdateWidget(ShortcutDecoder oldWidget) {
            this.buildSequences();
        }

        private void buildSequences() {
            this.sequences = widget().shortcuts.entrySet().stream().map(emongus -> new ShortcutSequence(emongus.getKey(), emongus.getValue())).toList();
        }

        @Override
        public Widget build(BuildContext context) {
            return new MouseArea(
                widget -> widget
                    .enterCallback(this.widget().enterCallback())
                    .exitCallback(this.widget().exitCallback())
                    .cursorStyleSupplier(this.widget().cursorStyleSupplier())
                    .clickCallback((x, y, button, modifiers) -> stepShortcuts(trigger -> trigger.isTriggeredByMouseButton(button, modifiers)
                        ? ShortcutTriggerResult.ACTIVATED
                        : ShortcutTriggerResult.NOT_ACTIVATED, TriggerType.MOUSE)),
                new Focusable(
                    widget -> widget
                        .focusGainedCallback(this.widget().focusGainedCallback())
                        .focusLostCallback(this.widget().focusLostCallback())
                        .skipTraversal(this.widget().skipTraversal())
                        .autoFocus(this.widget().autoFocus())
                        .keyDownCallback((keyCode, modifiers) -> stepShortcuts(trigger -> {
                            if (trigger.isTriggeredByKeyCode(keyCode, modifiers)) return ShortcutTriggerResult.ACTIVATED;
                            return KeyModifiers.isModifier(keyCode) ? ShortcutTriggerResult.IGNORED : ShortcutTriggerResult.NOT_ACTIVATED;
                        }, TriggerType.KEY)),
                    this.widget().child
                )
            );
        }

        private boolean stepShortcuts(Function<ShortcutTrigger, ShortcutTriggerResult> test, TriggerType trigger) {
            // in case we currently have a dispatch queued, we
            // must cancel it *now* to avoid prematurely triggering
            // a dispatch before the user is done entering triggers
            if (this.callbackId != null) {
                this.cancelDelayedCallback(this.callbackId);
                this.callbackId = null;
            }

            // now, begin by stepping all sequences with current input and keeping
            // only the ones which didn't ignore it. this can lead to a few outcomes
            // for each sequence. to break it down:
            // - singular sequences:
            //   these can always step and, if so, will immediately complete
            // - non-singular sequences:
            //   whether these can step depends on their current state:
            //   - non-negative trigger index:
            //     if triggered, will step and potentially complete
            //     if not triggered, will not step and poison the trigger index
            //   - negative (poisoned) trigger index:
            //     will not step
            var steppedSequences = sequences.stream()
                .map(sequence -> new Tuple<>(sequence, sequence.step(test)))
                .filter(pair -> pair.getB() != ShortcutSequenceStep.IGNORE)
                .toList();

            // next, get the sequence to treat as completed on this iteration - if any
            // - if multiple sequences completed, pick the first one
            // - always prioritize non-singular sequences over singular sequences.
            //   this is important, since the current trigger could both finish
            //   a non-singular sequence (user intent) and immediately complete a
            //   singular one (this would be an artifact)
            var completed = BraidUtils.fold(
                Iterables.filter(steppedSequences, pair -> pair.getB() == ShortcutSequenceStep.COMPLETE),
                (Tuple<ShortcutSequence, ShortcutSequenceStep>) null,
                (acc, element) -> {
                    if (acc == null) return element;
                    if (!element.getA().isSingular && acc.getA().isSingular) return element;
                    return acc;
                }
            );
            //(I personally think this should've used stream.reduce but glisco said it was "not ideal" so here we are) -chyz

            // if we have successfully resolved all ambiguity, that is,
            // every remaining (non-poisoned) sequence stepped to completion,
            // dispatch immediately
            if (steppedSequences.stream().allMatch(pair -> pair.getB() == ShortcutSequenceStep.COMPLETE) && completed != null) {
                return this.dispatch(completed.getA(), completed.getA().isSingular, trigger);
            } else {
                // otherwise, queue up the completed sequence (if any)
                // and queue dispatch after the maximum possible input delay

                if (completed != null) {
                    // if the sequence we just complete is non-singular, clear
                    // the queue - this is important, since otherwise we could duplicate
                    // the respective events
                    if (!completed.getA().isSingular) {
                        this.queuedSequences.clear();
                    }

                    this.queuedSequences.add(completed.getA());
                    completed.getA().nextTriggerIndex = 0;
                }

                this.callbackId = this.scheduleDelayedCallback(MAX_INPUT_DELAY, () -> this.dispatch(null, true, trigger));

                return !steppedSequences.isEmpty();
            }

        }

        private boolean dispatch(@Nullable ShortcutDecoder.State.ShortcutSequence completedSequence, boolean runQueued, TriggerType trigger) {
            if (runQueued) {
                for (var sequence : this.queuedSequences) {
                    sequence.callback.trigger(trigger);
                }
            }

            var success = false;
            if (completedSequence != null) {
                success = completedSequence.callback.trigger(trigger);
            }

            this.queuedSequences.clear();
            for (var sequence : sequences) {
                sequence.nextTriggerIndex = 0;
            }

            return success;
        }

        public static final Duration MAX_INPUT_DELAY = Duration.ofMillis(250);

        private enum ShortcutTriggerResult {
            /// the trigger was not activated by this input.
            /// non-singular sequences should poison
            NOT_ACTIVATED,
            /// the trigger was activated by this input.
            /// sequences should step
            ACTIVATED,
            /// the trigger entirely ignored this input.
            /// non-singular sequences should not poison
            /// and sequences should not step
            IGNORED
        }

        private enum ShortcutSequenceStep {
            IGNORE,
            ADVANCE,
            COMPLETE
        }

        private static class ShortcutSequence {
            public final List<ShortcutTrigger> triggers;
            public final Listener callback;

            /// whether this sequence is singular, i.e. it only has
            /// a single trigger and can be completed at any time
            public final boolean isSingular;

            public int nextTriggerIndex = 0;

            public ShortcutSequence(List<ShortcutTrigger> triggers, Listener callback) {
                this.triggers = triggers;
                this.callback = callback;
                this.isSingular = triggers.size() == 1;
            }

            /// step this sequence
            /// - if the sequence ignored the input, is poisoned or is completed, return [ShortcutSequenceStep#IGNORE]
            /// - if the sequence activated its final trigger, return [ShortcutSequenceStep#COMPLETE]
            /// - if the sequence activated an intermediate trigger, return [ShortcutSequenceStep#ADVANCE]
            public ShortcutSequenceStep step(Function<ShortcutTrigger, ShortcutTriggerResult> test) {
                if (this.nextTriggerIndex < 0 || this.nextTriggerIndex >= this.triggers.size()) return ShortcutSequenceStep.IGNORE;

                var result = test.apply(this.triggers.get(this.nextTriggerIndex));
                if (result == ShortcutTriggerResult.ACTIVATED) {
                    this.nextTriggerIndex++;
                    return this.nextTriggerIndex == this.triggers.size() ? ShortcutSequenceStep.COMPLETE : ShortcutSequenceStep.ADVANCE;
                } else if (!this.isSingular && result == ShortcutTriggerResult.NOT_ACTIVATED) {
                    // only poison non-singular sequences. this is important, because
                    // otherwise we could incorrectly swallow a singular sequence completed
                    // just after the first trigger of a non-singular sequence
                    this.nextTriggerIndex = -1;
                }
                return ShortcutSequenceStep.IGNORE;
            }
        }

    }
}
