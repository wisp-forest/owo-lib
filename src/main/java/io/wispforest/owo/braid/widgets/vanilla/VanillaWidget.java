package io.wispforest.owo.braid.widgets.vanilla;

import io.wispforest.owo.braid.core.Size;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.Key;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Align;
import io.wispforest.owo.braid.widgets.basic.Center;
import io.wispforest.owo.braid.widgets.basic.Sized;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.function.Supplier;

public class VanillaWidget<T extends Drawable & Element> extends StatefulWidget {
    //"Yeah, I think this is a good name" - glisco 2025

    public final Size size;
    public final Supplier<T> widgetSupplier;

    public VanillaWidget(Size size, Supplier<T> widgetSupplier) {
        this.widgetSupplier = widgetSupplier;
        this.size = size;
    }

    @Override
    public WidgetState<VanillaWidget<T>> createState() {
        return new State<>();
    }


    //TODO: tell people they need to use a key in the case where they modify the supplier without modifying the tree, i dont like 100% understand this but glisco will know what this means
    public static class State<T extends Drawable & Element> extends WidgetState<VanillaWidget<T>> {

        private T widget;

        @Override
        public void init() {
            widget = this.widget().widgetSupplier.get();
        }

        @Override
        public Widget build(BuildContext context) {
            return new Center(
                new Sized(
                    this.widget().size,
                    new VanillaWidgetWrapper<>(widget)
                )
            );
        }
    }
}
