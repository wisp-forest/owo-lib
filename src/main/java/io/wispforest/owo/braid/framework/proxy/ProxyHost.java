package io.wispforest.owo.braid.framework.proxy;

import net.minecraft.client.MinecraftClient;

public interface ProxyHost {

    MinecraftClient client();

    void scheduleAnimationCallback(AnimationCallback callback);

    interface AnimationCallback {
        void run(float delta);
    }
}
