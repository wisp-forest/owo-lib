package io.wispforest.uwu.network;

public record DispatchedSubclassTwo(int a) implements DispatchedInterface {
    @Override
    public String getName() {
        return "two";
    }
}
