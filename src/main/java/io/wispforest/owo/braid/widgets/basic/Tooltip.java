package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Constraints;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class Tooltip extends SingleChildInstanceWidget {

    public final @Nullable List<TooltipComponent> tooltip;
    public final Text tooltipText;

    public Tooltip(@NotNull List<TooltipComponent> tooltip, Widget child) {
        super(child);
        this.tooltip = tooltip;
        this.tooltipText = null;
    }

    public Tooltip(Collection<Text> tooltip, Widget child) {
        this(
            tooltip.stream().map(Text::asOrderedText).<TooltipComponent>map(OrderedTextTooltipComponent::new).toList(),
            child
        );
    }

    public Tooltip(Text tooltip, Widget child) {
        super(child);
        this.tooltip = null;
        this.tooltipText = tooltip;
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance<Tooltip> {

        public Instance(Tooltip widget) {
            super(widget);
        }

        @Override
        protected void doLayout(Constraints constraints) {
            this.sizeToChild(constraints, this.child);
        }
    }
}
