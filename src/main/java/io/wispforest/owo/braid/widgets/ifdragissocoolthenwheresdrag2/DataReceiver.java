package io.wispforest.owo.braid.widgets.ifdragissocoolthenwheresdrag2;

public interface DataReceiver {
    default boolean canReceive(Object data) {
        return false;
    }

    default boolean onReceive(Object data) {
        return false;
    }
}
