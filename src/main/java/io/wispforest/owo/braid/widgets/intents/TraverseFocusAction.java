package io.wispforest.owo.braid.widgets.intents;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.widgets.focus.Focusable;

public class TraverseFocusAction extends Action<TraverseFocusIntent> {
    @Override
    public void invoke(BuildContext context, TraverseFocusIntent intent) {
        Focusable.of(context).traverseFocus(intent.direction());
    }
}
