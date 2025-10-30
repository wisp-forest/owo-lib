package io.wispforest.owo.config.base;

import io.wispforest.owo.config.options.FieldOption;

///
/// Enum used to indicate if a given instance of [FieldOption] will have some form of syncing performed on it
/// based on the selected mode
///
public enum SyncMode {
    ///
    /// Do not ever send this option over the network
    ///
    NONE,
    ///
    /// Only send the client's value to the server,
    /// but not vice-versa
    ///
    INFORM_SERVER,
    ///
    /// Send the client's value to the server
    /// _and_ send the server's value back,
    /// overriding the client's value
    ///
    OVERRIDE_CLIENT;

    public boolean isNone() {
        return this == NONE;
    }
}
