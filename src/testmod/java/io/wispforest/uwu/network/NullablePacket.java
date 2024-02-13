package io.wispforest.uwu.network;

import io.wispforest.owo.serialization.annotations.NullableField;

import java.util.List;

public record NullablePacket(@NullableField String name, @NullableField List<String> names) {
}
