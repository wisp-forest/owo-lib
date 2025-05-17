package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.Button;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;

public class RawButton extends StatefulWidget {

    public final Text text;
    public final @Nullable IntConsumer onClick;

    public RawButton(Text text, @Nullable IntConsumer onClick) {
        this.text = text;
        this.onClick = onClick;
    }

    public RawButton(Text text, IntConsumer onClick, boolean enabled) {
        this(text, enabled ? onClick : null);
    }

    @Override
    public WidgetState<RawButton> createState() {
        return new RawButton.State();
    }

    public static class State extends WidgetState<RawButton> {

        private boolean hovered = false;

        @Override
        public Widget build(BuildContext context) {
            var active = this.widget().onClick != null;

            return new MouseArea(
                widget -> widget
                    .clickCallback((x, y, button) -> {
                        if (!active) return;

                        this.widget().onClick.accept(button);
                        UISounds.playButtonSound();
                    })
                    .enterCallback(() -> this.setState(() -> this.hovered = true))
                    .exitCallback(() -> this.setState(() -> this.hovered = false))
                    .cursorStyle(active ? CursorStyle.HAND : null),
                new Panel(
                    active
                        ? this.hovered ? ButtonComponent.HOVERED_TEXTURE : ButtonComponent.ACTIVE_TEXTURE
                        : ButtonComponent.DISABLED_TEXTURE,
                    new Padding(
                        Insets.all(5),
                        new Label(
                            active
                                ? LabelStyle.SHADOW
                                : new LabelStyle(null, Color.ofFormatting(Formatting.GRAY), null, false),
                            true,
                            this.widget().text
                        )
                    )
                )
            );
        }
    }
}
