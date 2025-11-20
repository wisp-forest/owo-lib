package io.wispforest.owo.util.pond;

import io.wispforest.owo.itemgroup.base.OwoItemGroup;
import io.wispforest.owo.itemgroup.OwoItemGroupBuilder;
import org.jetbrains.annotations.Nullable;

public interface OwoItemGroupExtension {
    @Nullable
    OwoItemGroup owo$getExtension();

    void attemptToBuildExtension();

    void owo$setBuilder(OwoItemGroupBuilder builder);

    OwoItemGroupBuilder owo$getBuilder();
}
