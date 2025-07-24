package io.wispforest.owo.braid.framework.proxy;

import net.minecraft.client.MinecraftClient;

import java.time.Duration;

public interface ProxyHost {

    MinecraftClient client();

    void scheduleAnimationCallback(AnimationCallback callback);

    long scheduleDelayedCallback(Duration delay, Runnable callback);

    void cancelDelayedCallback(long id);

    void schedulePostLayoutCallback(Runnable callback);

    interface AnimationCallback {
        void run(Duration delta);
    }
}
