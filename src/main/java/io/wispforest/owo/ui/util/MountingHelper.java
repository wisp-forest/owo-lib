package io.wispforest.owo.ui.util;

import io.wispforest.owo.ui.core.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MountingHelper {

    protected final ComponentSink sink;
    protected final List<Component> lateChildren;

    protected MountingHelper(ComponentSink sink, List<Component> children) {
        this.sink = sink;
        this.lateChildren = children;
    }

    public static MountingHelper mountEarly(ComponentSink sink, List<Component> children, Consumer<Component> layoutFunc) {
        var lateChildren = new ArrayList<Component>();

        for (var child : children) {
            if (!child.positioning().get().isRelative()) {
                sink.accept(child, layoutFunc);
            } else {
                lateChildren.add(child);
            }
        }

        return new MountingHelper(sink, lateChildren);
    }

    public void mountLate() {
        for (var child : this.lateChildren) {
            this.sink.accept(child, component -> {throw new IllegalStateException("A layout-positioned child was mounted late");});
        }
        this.lateChildren.clear();
    }

    public interface ComponentSink {
        void accept(@Nullable Component child, Consumer<Component> layoutFunc);
    }

}
