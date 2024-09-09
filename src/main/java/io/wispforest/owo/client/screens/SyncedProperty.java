package io.wispforest.owo.client.screens;

import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.owo.serialization.RegistriesAttribute;
import io.wispforest.owo.util.Observable;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.ApiStatus;

public class SyncedProperty<T> extends Observable<T> {
    private final int index;
    private final Endec<T> endec;
    private final ScreenHandler owner;
    private boolean needsSync;

    @ApiStatus.Internal
    public SyncedProperty(int index, Endec<T> endec, T initial, ScreenHandler owner) {
        super(initial);

        this.index = index;
        this.endec = endec;
        this.owner = owner;
    }

    public int index() {
        return index;
    }

    @ApiStatus.Internal
    public boolean needsSync() {
        return needsSync;
    }

    @ApiStatus.Internal
    public void write(PacketByteBuf buf) {
        needsSync = false;
        buf.write(serializationContext(), this.endec, value);
    }

    @ApiStatus.Internal
    public void read(PacketByteBuf buf) {
        this.set(buf.read(serializationContext(), this.endec));
    }

    @Override
    protected void notifyObservers(T value) {
        super.notifyObservers(value);

        this.needsSync = true;
    }

    public void markDirty() {
        notifyObservers(value);
    }

    private SerializationContext serializationContext() {
        var player = this.owner.player();
        if (player == null) return SerializationContext.empty();

        return SerializationContext.attributes(RegistriesAttribute.of(player.getRegistryManager()));
    }
}
