package io.wispforest.owo.braid.widgets.ifdragissocoolthenwheresdrag2;

import org.jetbrains.annotations.Nullable;

public interface DataSupplier {
    @Nullable
    default Object getData() {
        return null;
    }
}
