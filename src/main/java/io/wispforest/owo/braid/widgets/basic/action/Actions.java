package io.wispforest.owo.braid.widgets.basic.action;

import com.google.common.collect.Iterables;
import io.wispforest.owo.braid.core.BraidUtils;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import io.wispforest.owo.braid.widgets.basic.KeyboardInput;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Actions extends StatefulWidget {
    private @Nullable MouseArea.EnterCallback enterCallback;
    private @Nullable MouseArea.ExitCallback exitCallback;
    private @Nullable MouseArea.CursorStyleSupplier cursorStyleSupplier;

    private @Nullable KeyboardInput.FocusGainedCallback focusGainedCallback;
    private @Nullable KeyboardInput.FocusLostCallback focusLostCallback;

    private final Map<List<ActionTrigger>, Runnable> actions = new HashMap<>();

    private final Widget child;

    public Actions(
        WidgetSetupCallback<Actions> setupCallback,
        Widget child
    ) {
        this.child = child;
        setupCallback.setup(this);
    }

    public static Actions click(
        WidgetSetupCallback<Actions> setupCallback,
        @Nullable Runnable onClick,
        Widget child
    ) {
        return new Actions(
            widget -> {
                if (onClick != null) widget.addAction(List.of(ActionTrigger.CLICK), onClick);
                setupCallback.setup(widget);
            },
            child
        );
    }

    public Actions enterCallback(@Nullable MouseArea.EnterCallback enterCallback) {
        this.assertMutable();
        this.enterCallback = enterCallback;
        return this;
    }

    public @Nullable MouseArea.EnterCallback enterCallback() {
        return this.enterCallback;
    }

    public Actions exitCallback(@Nullable MouseArea.ExitCallback exitCallback) {
        this.assertMutable();
        this.exitCallback = exitCallback;
        return this;
    }

    public @Nullable MouseArea.ExitCallback exitCallback() {
        return this.exitCallback;
    }

    public Actions cursorStyleSupplier(@Nullable MouseArea.CursorStyleSupplier cursorStyleSupplier) {
        this.assertMutable();
        this.cursorStyleSupplier = cursorStyleSupplier;
        return this;
    }

    public Actions cursorStyle(@Nullable CursorStyle style) {
        return this.cursorStyleSupplier((x, y) -> style);
    }

    public @Nullable MouseArea.CursorStyleSupplier cursorStyleSupplier() {
        return this.cursorStyleSupplier;
    }

    public Actions focusGainedCallback(@Nullable KeyboardInput.FocusGainedCallback focusGainedCallback) {
        this.assertMutable();
        this.focusGainedCallback = focusGainedCallback;
        return this;
    }

    public @Nullable KeyboardInput.FocusGainedCallback focusGainedCallback() {
        return this.focusGainedCallback;
    }

    public Actions focusLostCallback(@Nullable KeyboardInput.FocusLostCallback focusLostCallback) {
        this.assertMutable();
        this.focusLostCallback = focusLostCallback;
        return this;
    }

    public @Nullable KeyboardInput.FocusLostCallback focusLostCallback() {
        return this.focusLostCallback;
    }

    public Actions addAction(List<ActionTrigger> triggers, Runnable action) {
        this.assertMutable();
        this.actions.put(triggers, action);
        return this;
    }

    public Map<List<ActionTrigger>, Runnable> actions() {
        return this.actions;
    }

    @Override
    public WidgetState<Actions> createState() {
        return new State();
    }

    public static class State extends WidgetState<Actions> {
        private List<ActionSequence> sequences = new ArrayList<>();

        private final List<ActionSequence> queuedSequences = new ArrayList<>();
        @Nullable private Long callbackId;

        @Override
        public void init() {
            sequences = widget().actions.entrySet().stream().map(emongus -> new ActionSequence(emongus.getKey(), emongus.getValue())).toList();
        }

        // TODO: dart ver. says "probably not ideal to rebuild this list every time"
        @Override
        public void didUpdateWidget(Actions oldWidget) {
            sequences = widget().actions.entrySet().stream().map(emongus -> new ActionSequence(emongus.getKey(), emongus.getValue())).toList();
        }

        @Override
        public Widget build(BuildContext context) {
            return new MouseArea(
                widget -> widget
                    .enterCallback(this.widget().enterCallback())
                    .exitCallback(this.widget().exitCallback())
                    .cursorStyleSupplier(this.widget().cursorStyleSupplier())
                    .clickCallback((x, y, button) -> stepActions(trigger -> trigger.isTriggeredByMouseButton(button)
                        ? ActionTriggerResult.ACTIVATED
                        : ActionTriggerResult.NOT_ACTIVATED)),
                new KeyboardInput(
                    widget -> widget
                        .focusGainedCallback(this.widget().focusGainedCallback())
                        .focusLostCallback(this.widget().focusLostCallback())
                        .keyDownCallback((keyCode, modifiers) -> stepActions(trigger -> {
                            if (trigger.isTriggeredByKeyCode(keyCode, modifiers)) return ActionTriggerResult.ACTIVATED;
                            return KeyModifiers.isModifier(keyCode) ? ActionTriggerResult.IGNORED : ActionTriggerResult.NOT_ACTIVATED;
                        })),
                    this.widget().child
                )
            );
        }

        private boolean stepActions(Function<ActionTrigger, ActionTriggerResult> test) {
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
                .map(sequence -> new Pair<>(sequence, sequence.step(test)))
                .filter(pair -> pair.getRight() != ActionSequenceStep.IGNORE)
                .toList();

            // next, get the sequence to treat as completed on this iteration - if any
            // - if multiple sequences completed, pick the first one
            // - always prioritize non-singular sequences over singular sequences.
            //   this is important, since the current trigger could both finish
            //   a non-singular sequence (user intent) and immediately complete a
            //   singular one (this would be an artifact)
            var completed = BraidUtils.fold(
                Iterables.filter(steppedSequences, pair -> pair.getRight() == ActionSequenceStep.COMPLETE),
                (Pair<ActionSequence, ActionSequenceStep>) null,
                (acc, element) -> {
                    if (acc == null) return element;
                    if (element.getLeft().isSingular && !acc.getLeft().isSingular) return acc;
                    return element;
                }
            );
            //(I personally think this should've used stream.reduce but glisco said it was "not ideal" so here we are) -chyz

            // if we have successfully resolved all ambiguity, that is,
            // every remaining (non-poisoned) sequence stepped to completion,
            // dispatch immediately
            if (steppedSequences.stream().allMatch(pair -> pair.getRight() == ActionSequenceStep.COMPLETE) && completed != null) {
                this.dispatch(completed.getLeft(), completed.getLeft().isSingular);
                return true;
            } else {
                // otherwise, queue up the completed sequence (if any)
                // and queue dispatch after the maximum possible input delay

                if (completed != null) {
                    // if the sequence we just complete is non-singular, clear
                    // the queue - this is important, since otherwise we could duplicate
                    // the respective events
                    if (!completed.getLeft().isSingular) {
                        this.queuedSequences.clear();
                    }

                    this.queuedSequences.add(completed.getLeft());
                    completed.getLeft().nextTriggerIndex = 0;
                }

                callbackId = this.scheduleDelayedCallback(MAX_INPUT_DELAY, () -> this.dispatch(null, true));

                return !steppedSequences.isEmpty();
            }

        }

        private void dispatch(@Nullable ActionSequence completedSequence, boolean runQueued) {
            if (runQueued) {
                for (ActionSequence sequence : queuedSequences) sequence.callback.run();
            }

            if (completedSequence != null) completedSequence.callback.run();

            queuedSequences.clear();
            for (ActionSequence sequence : sequences) sequence.nextTriggerIndex = 0;

        }

        public static Duration MAX_INPUT_DELAY = Duration.ofMillis(250);

        private enum ActionTriggerResult {
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

        private enum ActionSequenceStep {
            IGNORE,
            ADVANCE,
            COMPLETE
        }

        private static class ActionSequence {
            public final List<ActionTrigger> triggers;
            public final Runnable callback;

            /// whether this sequence is singular, i.e. it only has
            /// a single trigger and can be completed at any time
            public final boolean isSingular;

            public int nextTriggerIndex = 0;

            public ActionSequence(List<ActionTrigger> triggers, Runnable callback) {
                this.triggers = triggers;
                this.callback = callback;
                this.isSingular = triggers.size() == 1;
            }

            /// step this sequence
            /// - if the sequence ignored the input, is poisoned or is completed, return [ActionSequenceStep#IGNORE
            /// - if the sequence activated its final trigger, return [ActionSequenceStep#COMPLETE]
            /// - if the sequence activated an intermediate trigger, return [ActionSequenceStep#ADVANCE]
            public ActionSequenceStep step(Function<ActionTrigger, ActionTriggerResult> test) {
                if (this.nextTriggerIndex < 0 || nextTriggerIndex >= triggers.size()) return ActionSequenceStep.IGNORE;

                var result = test.apply(triggers.get(nextTriggerIndex));
                if (result == ActionTriggerResult.ACTIVATED) {
                    nextTriggerIndex++;
                    return nextTriggerIndex == triggers.size() ? ActionSequenceStep.COMPLETE : ActionSequenceStep.ADVANCE;
                } else if (!isSingular && result == ActionTriggerResult.NOT_ACTIVATED) {
                    // only poison non-singular sequences. this is important, because
                    // otherwise we could incorrectly swallow a singular sequence completed
                    // just after the first trigger of a non-singular sequence
                    nextTriggerIndex = -1;
                }
                return ActionSequenceStep.IGNORE;
            }
        }

    }
}
