package io.wispforest.owo.braid.core;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BraidHotReloadCallback {

    private static final Set<Listener> LISTENERS = new HashSet<>();
    public static final Logger LOGGER = LoggerFactory.getLogger("braid reload agent");

    public static Listener register() {
        var listener = new Listener();
        LISTENERS.add(listener);

        return listener;
    }

    @ApiStatus.Internal
    public static void setupComplete() {
        LOGGER.info("setup complete, debounce time is {}ms", Listener.DEBOUNCE_TIME);
    }

    @ApiStatus.Internal
    public static void invoke() {
        for (var listener : LISTENERS) {
            listener.triggered.set(true);
        }
    }

    public static class Listener {

        private static final int DEBOUNCE_TIME = Integer.getInteger("owo.braid.hotswapDebounceTime", 250);

        private final AtomicBoolean triggered = new AtomicBoolean();
        private @Nullable Instant lastTriggerTimestamp = null;

        public boolean poll() {
            if (this.triggered.getAndSet(false)) {
                this.lastTriggerTimestamp = Instant.now();
            }

            if (this.lastTriggerTimestamp != null && ChronoUnit.MILLIS.between(this.lastTriggerTimestamp, Instant.now()) > DEBOUNCE_TIME) {
                this.lastTriggerTimestamp = null;
                return true;
            }

            return false;
        }

        public void unregister() {
            LISTENERS.remove(this);
        }
    }
}
