package io.wispforest.uwu.network;

import io.wispforest.endec.annotations.SealedPolymorphic;

@SealedPolymorphic
public sealed interface SealedTestClass permits SealedSubclassOne, SealedSubclassTwo {
}