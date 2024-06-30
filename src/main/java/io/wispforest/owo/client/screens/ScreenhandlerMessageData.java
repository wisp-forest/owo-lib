package io.wispforest.owo.client.screens;

import io.wispforest.endec.Endec;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

@ApiStatus.Internal
public record ScreenhandlerMessageData<T>(int id, boolean clientbound, Endec<T> endec, Consumer<T> handler) {}
