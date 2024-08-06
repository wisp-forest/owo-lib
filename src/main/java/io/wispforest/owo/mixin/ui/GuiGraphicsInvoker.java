package io.wispforest.owo.mixin.ui;

import com.mojang.blaze3d.vertex.MatrixStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsInvoker {

    @Invoker("drawTooltip")
    void owo$renderTooltipFromComponents(Font textRenderer, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner);

    @Accessor("matrixStack")
    MatrixStack owo$getMatrixStack();

    @Mutable
    @Accessor("matrixStack")
    void owo$setMatrixStack(MatrixStack matrices);

    @Accessor("scissorStack")
    GuiGraphics.ScissorStack owo$getScissorStack();

    @Mutable
    @Accessor("scissorStack")
    void owo$setScissorStack(GuiGraphics.ScissorStack scissorStack);
}
