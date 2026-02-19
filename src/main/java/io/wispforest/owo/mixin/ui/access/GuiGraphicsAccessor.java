package io.wispforest.owo.mixin.ui.access;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsAccessor {

    @Invoker("renderTooltip")
    void owo$drawTooltipImmediately(Font textRenderer, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, @Nullable Identifier texture);

    @Accessor("pose")
    Matrix3x2fStack owo$getPose();

    @Mutable
    @Accessor("pose")
    void owo$setPose(Matrix3x2fStack matrices);

    @Accessor("scissorStack")
    GuiGraphics.ScissorStack owo$getScissorStack();

    @Mutable
    @Accessor("scissorStack")
    void owo$setScissorStack(GuiGraphics.ScissorStack scissorStack);

    @Accessor("deferredTooltip")
    void owo$setDeferredTooltip(Runnable drawer);

    @Accessor("deferredTooltip")
    Runnable owo$getDeferredTooltip();

    @Accessor("mouseX")
    int owo$getMouseX();

    @Accessor("mouseY")
    int owo$getMouseY();
}
