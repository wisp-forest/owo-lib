package io.wispforest.owo.braid.widgets.basic;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;

public class EmptyWidget extends StatelessWidget {

    public static final EmptyWidget INSTANCE = new EmptyWidget();

    private EmptyWidget() {}

    @Override
    public Widget build(BuildContext context) {
        return new Padding(Insets.none());
    }
}
