package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.framework.instance.InstanceHost;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.instance.TooltipProvider;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class Tooltip extends SingleChildInstanceWidget {

    public final @Nullable List<ClientTooltipComponent> tooltip;
    public final Component tooltipText;

    public Tooltip(@NotNull List<ClientTooltipComponent> tooltip, Widget child) {
        super(child);
        this.tooltip = tooltip;
        this.tooltipText = null;
    }

    public Tooltip(Collection<Component> tooltip, Widget child) {
        this(
            tooltip.stream().map(Component::getVisualOrderText).<ClientTooltipComponent>map(ClientTextTooltip::new).toList(),
            child
        );
    }

    public Tooltip(Component tooltip, Widget child) {
        super(child);
        this.tooltip = null;
        this.tooltipText = tooltip;
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance.ShrinkWrap<Tooltip> implements TooltipProvider {
        private @Nullable List<ClientTooltipComponent> tooltip;

        public Instance(Tooltip widget) {
            super(widget);
        }

        @Override
        public void attachHost(InstanceHost host) {
            super.attachHost(host);
            this.setup();
        }

        @Override
        public void setWidget(Tooltip widget) {
            super.setWidget(widget);
            this.setup();
        }

        private void setup() {
            this.tooltip = widget.tooltipText != null
                ? this.host().client().font
                .split(widget.tooltipText, Integer.MAX_VALUE)
                .stream()
                .<ClientTooltipComponent>map(ClientTextTooltip::new)
                .toList()
                : widget.tooltip;
        }

        @Override
        public @Nullable List<ClientTooltipComponent> getTooltipComponentsAt(double x, double y) {
            return tooltip;
        }
    }
}
