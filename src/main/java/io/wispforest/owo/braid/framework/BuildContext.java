package io.wispforest.owo.braid.framework;

public interface BuildContext {
    <T> T dependOnAncestor(Class<T> ancestorClass);
}
