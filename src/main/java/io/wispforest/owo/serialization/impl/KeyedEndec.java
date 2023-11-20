package io.wispforest.owo.serialization.impl;

import io.wispforest.owo.serialization.Endec;

public record KeyedEndec<F>(String key, Endec<F> endec, F defaultValue) {}
