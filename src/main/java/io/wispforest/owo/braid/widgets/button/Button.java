package io.wispforest.owo.braid.widgets.button;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.MouseArea;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.util.UISounds;
import it.unimi.dsi.fastutil.booleans.BooleanPredicate;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

public class Button extends StatefulWidget {

    public final @Nullable Runnable onClick;
    public final Widget child;

    public Button(@Nullable Runnable onClick, Widget child) {
        this.onClick = onClick;
        this.child = child;
    }

    public Button(Runnable onClick, boolean active, Widget child) {
        this(active ? onClick : null, child);
    }

    @Override
    public WidgetState<Button> createState() {
        return new State();
    }

    public static class State extends WidgetState<Button> {
        private boolean hovered = false;

        @Override
        public Widget build(BuildContext context) {
            var active = this.widget().onClick != null;

            return new MouseArea(
                widget -> widget
                    .enterCallback(() -> this.setState(() -> this.hovered = true))
                    .exitCallback(() -> this.setState(() -> this.hovered = false)),
                new RawButton(
                    widget().onClick,
                    new Panel(
                        active
                            ? this.hovered ? ButtonComponent.HOVERED_TEXTURE : ButtonComponent.ACTIVE_TEXTURE
                            : ButtonComponent.DISABLED_TEXTURE,
                        new Padding(
                            Insets.all(5),
                            widget().child
                        )
                    )
                )
            );
        }
    }
}
