package io.wispforest.owo.ui.util;

import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Size;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MountingHelper {

    protected final ComponentSink sink;
    protected final List<Component> lateChildren;
    protected final Size childSpace;

    protected MountingHelper(ComponentSink sink, List<Component> children, Size childSpace) {
        this.sink = sink;
        this.lateChildren = children;
        this.childSpace = childSpace;
    }

    public static MountingHelper mountEarly(ComponentSink sink, List<Component> children, Size childSpace, Consumer<Component> layoutFunc) {
        var lateChildren = new ArrayList<Component>();

        for (var child : children) {
            if (child.positioning().get().type != Positioning.Type.RELATIVE) {
                sink.accept(child, childSpace, layoutFunc);
            } else {
                lateChildren.add(child);
            }
        }

        return new MountingHelper(sink, lateChildren, childSpace);
    }

    public void mountLate() {
        for (var child : lateChildren) {
            this.sink.accept(child, this.childSpace, component -> {throw new IllegalStateException("A layout-positioned child was mounted late");});
        }
        this.lateChildren.clear();
    }

    public interface ComponentSink {
        void accept(@Nullable Component child, Size space, Consumer<Component> layoutFunc);
    }

}
