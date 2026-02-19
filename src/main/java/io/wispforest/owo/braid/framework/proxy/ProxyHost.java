package io.wispforest.owo.braid.framework.proxy;

import net.minecraft.client.Minecraft;

import java.time.Duration;

public interface ProxyHost {

    Minecraft client();

    void scheduleAnimationCallback(AnimationCallback callback);

    long scheduleDelayedCallback(Duration delay, Runnable callback);

    void cancelDelayedCallback(long id);

    void schedulePostLayoutCallback(Runnable callback);

    interface AnimationCallback {
        void run(Duration delta);
    }
}
