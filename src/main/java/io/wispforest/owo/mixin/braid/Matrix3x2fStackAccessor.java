package io.wispforest.owo.mixin.braid;

import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.gen.Accessor;

@org.spongepowered.asm.mixin.Mixin(org.joml.Matrix3x2fStack.class)
public interface Matrix3x2fStackAccessor {
    @Accessor("mats")
    Matrix3x2f[] owo$getMats();

    @Accessor("mats")
    void owo$setMats(Matrix3x2f[] mats);

    @Accessor("curr")
    int owo$getCurr();

    @Accessor("curr")
    void owo$setCurr(int curr);
}
