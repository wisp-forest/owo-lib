package io.wispforest.owo.braid.framework.widget;

public interface WidgetSetupCallback<T extends Widget> {
    void setup(T widget);

    default WidgetSetupCallback<T> compose(WidgetSetupCallback<? super T> before) {
        return widget -> {
            before.setup(widget);
            this.setup(widget);
        };
    }

    default WidgetSetupCallback<T> andThen(WidgetSetupCallback<? super T> after) {
        return widget -> {
            this.setup(widget);
            after.setup(widget);
        };
    }
}
