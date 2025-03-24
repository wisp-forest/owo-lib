package io.wispforest.owo.braid.framework.proxy;

public interface ProxyHost {

    void scheduleAnimationCallback(AnimationCallback callback);

    interface AnimationCallback {
        void animate(float delta);
    }
}
