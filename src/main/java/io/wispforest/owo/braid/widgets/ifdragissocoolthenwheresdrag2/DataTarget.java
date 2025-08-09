package io.wispforest.owo.braid.widgets.ifdragissocoolthenwheresdrag2;

import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class DataTarget extends SingleChildInstanceWidget {
    private static final Predicate<Object> TRUE = data -> true;

    private @Nullable DataAcceptor dataAcceptor;
    private Predicate<Object> predicate = TRUE;

    public DataTarget(
        WidgetSetupCallback<DataTarget> setupCallback,
        Widget child
    ) {
        super(child);
        setupCallback.setup(this);
    }

    public DataTarget dataAcceptor(@Nullable DataAcceptor dataAcceptor) {
        this.assertMutable();
        this.dataAcceptor = dataAcceptor;
        return this;
    }

    public @Nullable DataAcceptor dataAcceptor() {
        return this.dataAcceptor;
    }

    public DataTarget predicate(Predicate<Object> predicate) {
        this.assertMutable();
        this.predicate = predicate;
        return this;
    }

    public Predicate<Object> predicate() {
        return this.predicate;
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    @FunctionalInterface
    public interface DataAcceptor {
        boolean onDrop(Object data);
    }

    public static class Instance extends SingleChildWidgetInstance.ShrinkWrap<DataTarget> {

        public Instance(DataTarget widget) {
            super(widget);
        }
    }
}
