package io.wispforest.owo.util.pond;

import io.wispforest.owo.ui.core.PositionedRectangle;
import org.jetbrains.annotations.Nullable;

public interface OwoSlotExtension {

    void owo$setDisabledOverride(boolean disabled);

    boolean owo$getDisabledOverride();

    void owo$setScissorArea(@Nullable PositionedRectangle scissor);

    @Nullable PositionedRectangle owo$getScissorArea();
}
