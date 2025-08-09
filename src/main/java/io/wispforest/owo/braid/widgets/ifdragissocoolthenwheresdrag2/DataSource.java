package io.wispforest.owo.braid.widgets.ifdragissocoolthenwheresdrag2;

import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DataSource extends SingleChildInstanceWidget {
    @Nullable
    public final Supplier<?> dataSupplier;

    public DataSource(
        @Nullable Supplier<?> supplier,
        Widget child
    ) {
        super(child);
        this.dataSupplier = supplier;
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance.ShrinkWrap<DataSource> implements DataSupplier {
        public Instance(DataSource widget) {
            super(widget);
        }

        @Override
        @Nullable
        public Object getData() {
            if (this.widget.dataSupplier == null) return null;
            return this.widget.dataSupplier.get();
        }
    }
}
