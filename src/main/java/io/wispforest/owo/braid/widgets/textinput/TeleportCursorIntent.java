package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.widgets.intents.Intent;

public record TeleportCursorIntent(boolean toStart, boolean selecting) implements Intent {}
