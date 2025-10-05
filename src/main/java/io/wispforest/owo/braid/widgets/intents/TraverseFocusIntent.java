package io.wispforest.owo.braid.widgets.intents;

import io.wispforest.owo.braid.widgets.focus.FocusTraversalDirection;

public record TraverseFocusIntent(FocusTraversalDirection direction) implements Intent {}
