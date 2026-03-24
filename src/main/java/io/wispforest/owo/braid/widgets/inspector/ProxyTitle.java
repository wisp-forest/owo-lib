package io.wispforest.owo.braid.widgets.inspector;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.*;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.VisitorWidget;
import io.wispforest.owo.braid.widgets.label.Label;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

public class ProxyTitle extends StatefulWidget {

    public final WidgetProxy proxy;

    public ProxyTitle(WidgetProxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public WidgetState<ProxyTitle> createState() {
        return new State();
    }

    public static class State extends WidgetState<ProxyTitle> {

        public boolean hovered = false;

        @Override
        public Widget build(BuildContext context) {
            var proxy = this.widget().proxy;
            var widgetName = proxy.widget().getClass().getSimpleName();

            record ProxyTag(String shortForm, String fullName, ChatFormatting color) {}
            var tag = switch (proxy) {
                case VisitorWidget.Proxy<?> ignored -> new ProxyTag("V", "Visitor", ChatFormatting.DARK_AQUA);
                case StatefulProxy ignored -> new ProxyTag("SF", "Stateful", ChatFormatting.AQUA);
                case StatelessProxy ignored -> new ProxyTag("SL", "Stateless", ChatFormatting.GREEN);
                case InheritedProxy ignored -> new ProxyTag("IH", "Inherited", ChatFormatting.GOLD);
                case InstanceWidgetProxy ignored -> new ProxyTag("IW", "Instance", ChatFormatting.LIGHT_PURPLE);
                default -> new ProxyTag("?", proxy.getClass().getSimpleName(), ChatFormatting.GRAY);
            };

            var text = Component.literal(widgetName)
                .withStyle(style -> style.withBold(this.hovered))
                .append(Component.literal(" " + tag.shortForm()).withStyle(style -> style
                    .withColor(tag.color())
                    .withHoverEvent(new HoverEvent.ShowText(
                        Component.literal(tag.fullName()).withStyle(tag.color())
                    ))
                ));

            if (proxy.needsRebuild()) {
                text = text.append(Component.literal(" !").withStyle(style -> style
                    .withColor(ChatFormatting.RED)
                    .withHoverEvent(new HoverEvent.ShowText(
                        Component.literal("Needs Rebuild")
                    ))
                ));
            }

            return new MouseArea(
                w -> w.enterCallback(() -> this.setState(() -> this.hovered = true))
                    .exitCallback(() -> this.setState(() -> this.hovered = false)),
                new Padding(Insets.vertical(1), new Label(text))
            );
        }
    }
}
