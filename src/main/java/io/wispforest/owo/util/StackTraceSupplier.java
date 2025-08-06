package io.wispforest.owo.util;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class StackTraceSupplier implements Supplier<String> {
    private final Throwable throwable;
    private final @Nullable Supplier<String> message;

    private StackTraceSupplier(Throwable throwable, @Nullable Supplier<String> message) {
        this.throwable = throwable;
        this.message = message;
    }

    public static StackTraceSupplier of(Throwable throwable) {
        return new StackTraceSupplier(throwable, null);
    }

    public static StackTraceSupplier of(Throwable throwable, Supplier<String> supplier) {
        return new StackTraceSupplier(throwable, supplier);
    }

    public static StackTraceSupplier of(String message) {
        var error = new IllegalStateException(message)
            .initCause(null);

        return new StackTraceSupplier(error, null);
    }

    @Override
    public String get() {
        return message != null ? message.get() : throwable.getMessage();
    }

    public StackTraceElement[] getFullStackTrace() {
        var innerThrowable = throwable();
        while (innerThrowable.getCause() != null) {
            innerThrowable = innerThrowable.getCause();

            // Prevent possible infinite loops where the cause is itself the cause as it is not setup or chain of exceptions
            if (innerThrowable == throwable()) break;
        }
        return innerThrowable.getStackTrace();
    }

    public Throwable throwable() {
        return throwable;
    }

    @Override
    public String toString() {
        return "StackTraceSupplier[" +
                "throwable=" + throwable + ", " +
                "message=" + message + ']';
    }
}
