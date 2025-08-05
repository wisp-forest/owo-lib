package io.wispforest.owo.braid.widgets.globalstate;

import io.wispforest.endec.Endec;
import net.minecraft.util.Identifier;

public abstract class GlobalState<T> {

    public abstract Identifier id();

    public abstract Endec<T> endec();
}
