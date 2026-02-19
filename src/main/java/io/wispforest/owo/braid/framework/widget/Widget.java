package io.wispforest.owo.braid.framework.widget;

import io.wispforest.owo.braid.framework.proxy.WidgetProxy;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class Widget {
    private boolean mutable = true;

    @ApiStatus.Internal
    public final void freeze() {
        mutable = false;
    }

    protected final void assertMutable() {
        if (!this.mutable) throw new ImmutableWidgetError();
    }

    // ---

    private @Nullable Key key;

    public Widget key(Key key) {
        this.assertMutable();
        this.key = key;

        return this;
    }

    public @Nullable Key key() {
        return this.key;
    }

    // ---

    public abstract WidgetProxy proxy();

    public static boolean canUpdate(Widget oldWidget, Widget newWidget) {
        return oldWidget.getClass() == newWidget.getClass() && Objects.equals(oldWidget.key, newWidget.key);
    }

    public static <T extends Widget> WidgetSetupCallback<T> noSetup() {
        //noinspection unchecked
        return NO_SETUP;
    }

    @SuppressWarnings("rawtypes")
    private static final WidgetSetupCallback NO_SETUP = widget -> {};
}

class ImmutableWidgetError extends Error {
    public ImmutableWidgetError() {
        // TODO: more detailed explanation of why this is bad
        super("A mutation on a widget was attempted after the widget was frozen");
    }
}
