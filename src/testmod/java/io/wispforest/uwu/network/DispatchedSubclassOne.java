package io.wispforest.uwu.network;

public record DispatchedSubclassOne(String a) implements DispatchedInterface {
    @Override
    public String getName() {
        return "one";
    }
}
