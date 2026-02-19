package io.wispforest.owo.braid.framework.widget;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

public class Key {

    final String value;

    private Key(String value) {
        this.value = value;
    }

    public static Key of(@NotNull String value) {
        Preconditions.checkNotNull(value ,"the value of a key must never be null");
        return new Key(value);
    }

    // ---

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Key key = (Key) o;
        return this.value.equals(key.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
}
