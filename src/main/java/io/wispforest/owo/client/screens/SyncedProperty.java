package io.wispforest.owo.client.screens;

import io.wispforest.endec.Endec;
import io.wispforest.owo.util.Observable;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;

public class SyncedProperty<T> extends Observable<T> {
    private final int index;
    private final Endec<T> endec;
    private boolean needsSync;

    @ApiStatus.Internal
    public SyncedProperty(int index, Endec<T> endec, T initial) {
        super(initial);

        this.index = index;
        this.endec = endec;
    }

    public int index() {
        return index;
    }

    @ApiStatus.Internal
    public boolean needsSync() {
        return needsSync;
    }

    @ApiStatus.Internal
    public void write(FriendlyByteBuf buf) {
        needsSync = false;
        buf.write(this.endec, value);
    }

    @ApiStatus.Internal
    public void read(FriendlyByteBuf buf) {
        this.set(buf.read(this.endec));
    }

    @Override
    protected void notifyObservers(T value) {
        super.notifyObservers(value);

        this.needsSync = true;
    }

    public void markDirty() {
        notifyObservers(value);
    }
}
