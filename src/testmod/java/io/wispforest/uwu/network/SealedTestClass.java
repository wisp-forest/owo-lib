package io.wispforest.uwu.network;

import io.wispforest.owo.serialization.annotations.SealedPolymorphic;

@SealedPolymorphic
public sealed interface SealedTestClass permits SealedSubclassOne, SealedSubclassTwo {
}