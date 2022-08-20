package io.wispforest.uwu.network;

import io.wispforest.owo.network.serialization.SealedPolymorphic;

@SealedPolymorphic
public sealed interface SealedTestClass permits SealedSubclassOne, SealedSubclassTwo {
}