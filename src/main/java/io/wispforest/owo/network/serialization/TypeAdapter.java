package io.wispforest.owo.network.serialization;

import net.minecraft.network.PacketByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record TypeAdapter<T>(BiConsumer<PacketByteBuf, T> serializer, Function<PacketByteBuf, T> deserializer) {}
