package io.wispforest.owo.client.screens;

import io.wispforest.owo.network.serialization.PacketBufSerializer;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

@ApiStatus.Internal
public record ScreenhandlerMessageData<T>(int id, boolean clientbound, PacketBufSerializer<T> serializer, Consumer<T> handler) {}
