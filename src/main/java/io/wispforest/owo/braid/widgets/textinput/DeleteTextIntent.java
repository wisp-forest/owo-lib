package io.wispforest.owo.braid.widgets.textinput;

import io.wispforest.owo.braid.widgets.intents.Intent;

public record DeleteTextIntent(boolean forwards, boolean entireWord) implements Intent {}
