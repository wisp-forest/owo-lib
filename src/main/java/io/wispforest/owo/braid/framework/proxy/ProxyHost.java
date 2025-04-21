package io.wispforest.owo.braid.framework.proxy;

import net.minecraft.client.MinecraftClient;

import java.time.Duration;

public interface ProxyHost {

    MinecraftClient client();

    void scheduleAnimationCallback(AnimationCallback callback);

    void scheduleDelayedCallback(Duration delay, Runnable callback);

    interface AnimationCallback {
        void run(float delta);
    }
}
