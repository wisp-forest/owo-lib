package io.wispforest.owo.neoforge.api;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public class FriendlyByteBufs {
    private static final FriendlyByteBuf EMPTY_FRIENDLY_BYTE_BUF = new FriendlyByteBuf(Unpooled.EMPTY_BUFFER);

    /**
     * Returns an empty instance of friendly byte buf.
     *
     * @return an empty buf
     */
    public static FriendlyByteBuf empty() {
        return EMPTY_FRIENDLY_BYTE_BUF;
    }

    /**
     * Returns a new heap memory-backed instance of friendly byte buf.
     *
     * @return a new buf
     */
    public static FriendlyByteBuf create() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }
}
