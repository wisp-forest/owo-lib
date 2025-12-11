package io.wispforest.owo.mixin.ui.access;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(DrawContext.class)
public interface DrawContextAccessor {

    @Invoker("drawTooltipImmediately")
    void owo$drawTooltipImmediately(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner, @Nullable Identifier texture);

    @Accessor("matrices")
    Matrix3x2fStack owo$getMatrices();

    @Mutable
    @Accessor("matrices")
    void owo$setMatrices(Matrix3x2fStack matrices);

    @Accessor("scissorStack")
    DrawContext.ScissorStack owo$getScissorStack();

    @Mutable
    @Accessor("scissorStack")
    void owo$setScissorStack(DrawContext.ScissorStack scissorStack);

    @Accessor("tooltipDrawer")
    void owo$setTooltipDrawer(Runnable drawer);

    @Accessor("tooltipDrawer")
    Runnable owo$getTooltipDrawer();

    @Accessor("mouseX")
    int owo$getMouseX();

    @Accessor("mouseY")
    int owo$getMouseY();
}
