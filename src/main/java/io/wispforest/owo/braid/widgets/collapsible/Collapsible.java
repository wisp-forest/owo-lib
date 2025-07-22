package io.wispforest.owo.braid.widgets.collapsible;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Collapsible extends StatefulWidget {

    public final boolean showVerticalRule;

    public final boolean collapsed;
    public final Consumer<Boolean> onToggled;

    public final Widget title;
    public final Widget content;

    public Collapsible(boolean showVerticalRule, boolean collapsed, Consumer<Boolean> onToggled, Widget title, Widget content) {
        this.showVerticalRule = showVerticalRule;
        this.collapsed = collapsed;
        this.onToggled = onToggled;
        this.title = title;
        this.content = content;
    }

    @Override
    public WidgetState<Collapsible> createState() {
        return new State();
    }

    public static class State extends WidgetState<Collapsible> {

        public boolean hovered = false;

        @Override
        public Widget build(BuildContext context) {
            var body = new ArrayList<Widget>();

            if (this.widget().showVerticalRule) {
                body.add(new Align(
                    Alignment.LEFT,
                    new Padding(
                        Insets.left(5),
                        new Sized(
                            1,
                            Double.POSITIVE_INFINITY,
                            new Box(
                                this.hovered ? Color.WHITE : Color.WHITE.interpolate(Color.BLACK, .9f)
                            )
                        )
                    )
                ));
            }

            body.add(new StackBase(
                new Padding(Insets.left(10), this.widget().content)
            ));

            return new Column(
                new MouseArea(
                    widget -> widget
                        .enterCallback(this.widget().showVerticalRule ? () -> this.setState(() -> this.hovered = true) : null)
                        .exitCallback(this.widget().showVerticalRule ? () -> this.setState(() -> this.hovered = false) : null),
                    new Row(
                        MainAxisAlignment.START,
                        CrossAxisAlignment.CENTER,
                        new Sized(
                            10,
                            12,
                            Actions.click(
                                widget -> widget.cursorStyle(CursorStyle.HAND),
                                () -> this.widget().onToggled.accept(!this.widget().collapsed),
                                new Align(
                                    Alignment.LEFT,
                                    new Transform(
                                        new Matrix4f().rotateZ(this.widget().collapsed ? 0 : (float) Math.toRadians(90)),
                                        new Label(Text.literal(">"))
                                    )
                                )
                            )
                        ),
                        this.widget().title
                    )
                ),
                new Visibility(
                    !this.widget().collapsed,
                    new Stack(
                        body
                    )
                )
            );
        }
    }
}
