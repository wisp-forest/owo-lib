package io.wispforest.owo.neoforge.api;

import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.lang.invoke.MethodHandles;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record Matrix3x2fStackAccessor(Matrix3x2fStack stack) {

    private static final Function<Matrix3x2fStack, Matrix3x2f[]> matsGetter;
    private static final BiConsumer<Matrix3x2fStack, Matrix3x2f[]> matsSetter;

    private static final Function<Matrix3x2fStack, Integer> currGetter;
    private static final BiConsumer<Matrix3x2fStack, Integer> currSetter;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Matrix3x2fStack.class, MethodHandles.lookup());

            var matsGetterHandle = lookup.findGetter(Matrix3x2fStack.class, "mats", Matrix3x2f[].class);
            var matsSetterHandle = lookup.findSetter(Matrix3x2fStack.class, "mats", Matrix3x2f[].class);

            matsGetter = (stack) -> {
                try {
                    return (Matrix3x2f[]) matsGetterHandle.invoke(stack);
                } catch (Throwable e) {
                    throw new RuntimeException("Unable to invoke mats getter for reflection access for Matrix3x2fStackAccessor used within Braid", e);
                }
            };
            matsSetter = (stack, array) -> {
                try {
                    matsSetterHandle.invoke(stack, array);
                } catch (Throwable e) {
                    throw new RuntimeException("Unable to invoke mats setter for reflection access for Matrix3x2fStackAccessor used within Braid", e);
                }
            };

            var currGetterHandle = lookup.findGetter(Matrix3x2fStack.class, "curr", int.class);
            var currSetterHandle = lookup.findSetter(Matrix3x2fStack.class, "curr", int.class);

            currGetter = (stack) -> {
                try {
                    return (int) currGetterHandle.invoke(stack);
                } catch (Throwable e) {
                    throw new RuntimeException("Unable to invoke curr getter for reflection access for Matrix3x2fStackAccessor used within Braid", e);
                }
            };
            currSetter = (stack, array) -> {
                try {
                    currSetterHandle.invoke(stack, array);
                } catch (Throwable e) {
                    throw new RuntimeException("Unable to invoke curr setter for reflection access for Matrix3x2fStackAccessor used within Braid", e);
                }
            };
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Unable to create needed reflection access for Matrix3x2fStackAccessor used within Braid", e);
        }
    }

    public Matrix3x2f[] owo$getMats() {
        return matsGetter.apply(this.stack);
    }

    public void owo$setMats(Matrix3x2f[] mats) {
        matsSetter.accept(this.stack, mats);
    }

    public int owo$getCurr() {
        return currGetter.apply(this.stack);
    }

    public void owo$setCurr(int curr) {
        currSetter.accept(this.stack, curr);
    }
}
