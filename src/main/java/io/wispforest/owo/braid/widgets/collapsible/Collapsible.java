package io.wispforest.owo.braid.widgets.collapsible;

import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.intents.Interactable;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;

import java.util.ArrayList;

public class Collapsible extends StatefulWidget {

    public final boolean showVerticalRule;

    public final boolean collapsed;
    public final CollapsibleCallback onToggled;

    public final Widget title;
    public final Widget content;

    public Collapsible(boolean showVerticalRule, boolean collapsed, CollapsibleCallback onToggled, Widget title, Widget content) {
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
                        Insets.left(6),
                        new Sized(
                            1,
                            Double.POSITIVE_INFINITY,
                            new Box(
                                this.hovered ? Color.WHITE : Color.mix(.5f, Color.WHITE, Color.BLACK)
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
                            12,
                            12,
                            Interactable.primary(
                                () -> this.widget().onToggled.onToggled(!this.widget().collapsed),
                                new Center(
                                    new SpriteWidget(Owo.id(this.widget().collapsed ? "braid_collapsible_closed" : "braid_collapsible_open"))
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
