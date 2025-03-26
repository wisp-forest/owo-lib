package io.wispforest.owo.config.base;

public enum SyncMode {
    /**
     * Do not ever send this option over the network
     */
    NONE,
    /**
     * Only send the client's value to the server,
     * but not vice-versa
     */
    INFORM_SERVER,
    /**
     * Send the client's value to the server
     * <i>and</i> send the server's value back,
     * overriding the client's value
     */
    OVERRIDE_CLIENT;

    public boolean isNone() {
        return this == NONE;
    }
}
