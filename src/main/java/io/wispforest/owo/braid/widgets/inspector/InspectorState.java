package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.braid.widgets.sharedstate.ShareableState;
import org.jetbrains.annotations.Nullable;

public class InspectorState extends ShareableState {
    public @Nullable Object selectedElement;
    public @Nullable RevealInstanceEvent lastRevealEvent;
}
