package io.wispforest.owo.util.pond;

import net.minecraft.client.render.BufferBuilder;

public interface OwoTessellatorExtension {
    void owo$skipNextBegin();

    void owo$setStoredBuilder(BufferBuilder builder);

    BufferBuilder owo$getStoredBuilder();
}
