package io.wispforest.uwu.network;

import io.wispforest.owo.network.annotations.SealedPolymorphic;

@SealedPolymorphic
public sealed interface SealedTestClass permits SealedSubclassOne, SealedSubclassTwo {
}