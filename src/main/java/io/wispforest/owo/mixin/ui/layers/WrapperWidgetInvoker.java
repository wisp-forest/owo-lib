package io.wispforest.owo.mixin.ui.layers;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.WrapperWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(WrapperWidget.class)
public interface WrapperWidgetInvoker {

    @Invoker("wrappedWidgets")
    List<? extends ClickableWidget> owo$wrappedWidgets();
}
