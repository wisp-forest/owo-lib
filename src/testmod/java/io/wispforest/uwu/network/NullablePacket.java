package io.wispforest.uwu.network;

import io.wispforest.owo.serialization.annotations.NullableComponent;

import java.util.List;

public record NullablePacket(@NullableComponent String name, @NullableComponent List<String> names) {}
