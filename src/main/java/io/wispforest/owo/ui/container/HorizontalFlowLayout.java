package io.wispforest.owo.ui.container;

import io.wispforest.owo.ui.core.Sizing;

/**
 * @deprecated Replaced by calling {@link FlowLayout#FlowLayout(Sizing, Sizing, Algorithm)}
 * with {@link FlowLayout.Algorithm#HORIZONTAL}
 */
@Deprecated(forRemoval = true)
public class HorizontalFlowLayout extends FlowLayout {

    protected HorizontalFlowLayout(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing, Algorithm.HORIZONTAL);
    }
}
