package io.wispforest.owo.braid.widgets.intents;

import io.wispforest.owo.braid.framework.BuildContext;

public abstract class Action<I extends Intent> {

    public boolean isActive(BuildContext context, I intent) {
        return true;
    }

    public abstract void invoke(BuildContext context, I intent);

    public static <I extends Intent> Action<I> callback(Callback<I> callback) {
        return new CallbackAction<>(callback);
    }

    public static class CallbackAction<I extends Intent> extends Action<I> {

        public Callback<I> callback;

        public CallbackAction(Callback<I> callback) {
            this.callback = callback;
        }

        @Override
        public void invoke(BuildContext context, I intent) {
            this.callback.invoke(context, intent);
        }
    }

    @FunctionalInterface
    public interface Callback<I extends Intent> {
        void invoke(BuildContext actionCtx, I intent);
    }
}
