package io.wispforest.owo.util.pond;

import com.mojang.blaze3d.vertex.BufferBuilder;

public interface OwoTessellatorExtension {
    void owo$skipNextBegin();

    void owo$setStoredBuilder(BufferBuilder builder);

    BufferBuilder owo$getStoredBuilder();
}
