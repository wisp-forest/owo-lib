package io.wispforest.uwu.config;

public record RecordTestObj(float amount, String id, int tag, float other, String anotherTest, boolean toggle) {
    public RecordTestObj(){
        this(0.0f, "none", 1, 0.0f, "weeeeeee", false);
    }
}
