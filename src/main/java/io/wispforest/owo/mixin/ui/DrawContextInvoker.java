package io.wispforest.owo.mixin.ui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import com.mojang.blaze3d.vertex.MatrixStack;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;

@Mixin(GuiGraphics.class)
public interface DrawContextInvoker {

    @Invoker("drawTooltip")
    void owo$renderTooltipFromComponents(Font textRenderer, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner);

    @Accessor("matrices")
    MatrixStack owo$getMatrices();

    @Mutable
    @Accessor("matrices")
    void owo$setMatrices(MatrixStack matrices);

    @Accessor("scissorStack")
    GuiGraphics.ScissorStack owo$getScissorStack();

    @Mutable
    @Accessor("scissorStack")
    void owo$setScissorStack(GuiGraphics.ScissorStack scissorStack);
}
