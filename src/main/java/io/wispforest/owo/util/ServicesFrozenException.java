package io.wispforest.owo.util;

public class ServicesFrozenException extends IllegalStateException {
    public ServicesFrozenException(String message) {
        super(message);
    }
}
