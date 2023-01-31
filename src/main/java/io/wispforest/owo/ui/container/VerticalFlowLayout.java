package io.wispforest.owo.ui.container;

import io.wispforest.owo.ui.core.Sizing;

/**
 * @deprecated Replaced by calling {@link FlowLayout#FlowLayout(Sizing, Sizing, Algorithm)}
 * with {@link FlowLayout.Algorithm#VERTICAL}
 */
@Deprecated(forRemoval = true)
public class VerticalFlowLayout extends FlowLayout {

    protected VerticalFlowLayout(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing, Algorithm.VERTICAL);
    }
}
