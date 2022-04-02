package io.wispforest.owo.util;

import io.wispforest.owo.Owo;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple utility for freezing services after mod initialization.
 */
public final class OwoFreezer {
    private static final List<Runnable> FREEZE_CALLBACKS = new ArrayList<>();
    private static boolean IS_FROZEN = false;
    private static String FREEZER_CLASS = null;

    private OwoFreezer() {

    }

    /**
     * Registers an on freeze callback. The callback will be called when services are frozen
     * @param callback the callback to register
     */
    public static void registerFreezeCallback(Runnable callback) {
        FREEZE_CALLBACKS.add(callback);
    }

    /**
     * @return {@code true} if services are frozen
     */
    public static boolean isFrozen() {
        return IS_FROZEN;
    }

    /**
     * Shorthand for checking if services aren't frozen, and throwing if not.
     * @param pluralName the plural of the service being registered (e.g. "Network channels")
     * @throws ServicesFrozenException if services are frozen
     */
    public static void checkRegister(String pluralName) {
        if (OwoFreezer.isFrozen())
            throw new ServicesFrozenException(pluralName + " may only be registered during mod initialization");
    }

    @ApiStatus.Internal
    public static void freeze() {
        if (IS_FROZEN) {
            throw new ServicesFrozenException(ReflectionUtils.getCallingClassName(2) + " tried to freeze services after they were already frozen by " + FREEZER_CLASS);
        }

        IS_FROZEN = true;
        FREEZER_CLASS = ReflectionUtils.getCallingClassName(2);

        for (Runnable callback : FREEZE_CALLBACKS) {
            callback.run();
        }

        if (!Owo.DEBUG) return;
        Owo.LOGGER.info("Services frozen by '" + FREEZER_CLASS + "'");
    }
}
