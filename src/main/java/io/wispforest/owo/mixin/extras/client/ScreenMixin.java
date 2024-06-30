package io.wispforest.owo.mixin.extras.client;

import io.wispforest.owo.extras.ScreenEventFactory;
import io.wispforest.owo.extras.ScreenEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
abstract class ScreenMixin implements ScreenEvents.ScreenExtensions {

    @Unique private Event<ScreenEvents.Remove> removeEvent;
    @Unique private Event<ScreenEvents.BeforeTick> beforeTickEvent;
    @Unique private Event<ScreenEvents.AfterTick> afterTickEvent;
    @Unique private Event<ScreenEvents.BeforeRender> beforeRenderEvent;
    @Unique private Event<ScreenEvents.AfterRender> afterRenderEvent;

    // Keyboard
    @Unique private Event<ScreenEvents.AllowKeyPress> allowKeyPressEvent;
    @Unique private Event<ScreenEvents.BeforeKeyPress> beforeKeyPressEvent;
    @Unique private Event<ScreenEvents.AfterKeyPress> afterKeyPressEvent;
    @Unique private Event<ScreenEvents.AllowKeyRelease> allowKeyReleaseEvent;
    @Unique private Event<ScreenEvents.BeforeKeyRelease> beforeKeyReleaseEvent;
    @Unique private Event<ScreenEvents.AfterKeyRelease> afterKeyReleaseEvent;

    // Mouse
    @Unique private Event<ScreenEvents.AllowMouseClick> allowMouseClickEvent;
    @Unique private Event<ScreenEvents.BeforeMouseClick> beforeMouseClickEvent;
    @Unique private Event<ScreenEvents.AfterMouseClick> afterMouseClickEvent;
    @Unique private Event<ScreenEvents.AllowMouseRelease> allowMouseReleaseEvent;
    @Unique private Event<ScreenEvents.BeforeMouseRelease> beforeMouseReleaseEvent;
    @Unique private Event<ScreenEvents.AfterMouseRelease> afterMouseReleaseEvent;
    @Unique private Event<ScreenEvents.AllowMouseScroll> allowMouseScrollEvent;
    @Unique private Event<ScreenEvents.BeforeMouseScroll> beforeMouseScrollEvent;
    @Unique private Event<ScreenEvents.AfterMouseScroll> afterMouseScrollEvent;

    @Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At("HEAD"))
    private void beforeInitScreen(MinecraftClient client, int width, int height, CallbackInfo ci) {
        beforeInit(client, width, height);
    }

    @Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At("TAIL"))
    private void afterInitScreen(MinecraftClient client, int width, int height, CallbackInfo ci) {
        afterInit(client, width, height);
    }

    @Inject(method = "resize", at = @At("HEAD"))
    private void beforeResizeScreen(MinecraftClient client, int width, int height, CallbackInfo ci) {
        beforeInit(client, width, height);
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void afterResizeScreen(MinecraftClient client, int width, int height, CallbackInfo ci) {
        afterInit(client, width, height);
    }

    @Unique
    private void beforeInit(MinecraftClient client, int width, int height) {
        // All elements are repopulated on the screen, so we need to reinitialize all events
        this.removeEvent = ScreenEventFactory.createRemoveEvent();
        this.beforeRenderEvent = ScreenEventFactory.createBeforeRenderEvent();
        this.afterRenderEvent = ScreenEventFactory.createAfterRenderEvent();
        this.beforeTickEvent = ScreenEventFactory.createBeforeTickEvent();
        this.afterTickEvent = ScreenEventFactory.createAfterTickEvent();

        // Keyboard
        this.allowKeyPressEvent = ScreenEventFactory.createAllowKeyPressEvent();
        this.beforeKeyPressEvent = ScreenEventFactory.createBeforeKeyPressEvent();
        this.afterKeyPressEvent = ScreenEventFactory.createAfterKeyPressEvent();
        this.allowKeyReleaseEvent = ScreenEventFactory.createAllowKeyReleaseEvent();
        this.beforeKeyReleaseEvent = ScreenEventFactory.createBeforeKeyReleaseEvent();
        this.afterKeyReleaseEvent = ScreenEventFactory.createAfterKeyReleaseEvent();

        // Mouse
        this.allowMouseClickEvent = ScreenEventFactory.createAllowMouseClickEvent();
        this.beforeMouseClickEvent = ScreenEventFactory.createBeforeMouseClickEvent();
        this.afterMouseClickEvent = ScreenEventFactory.createAfterMouseClickEvent();
        this.allowMouseReleaseEvent = ScreenEventFactory.createAllowMouseReleaseEvent();
        this.beforeMouseReleaseEvent = ScreenEventFactory.createBeforeMouseReleaseEvent();
        this.afterMouseReleaseEvent = ScreenEventFactory.createAfterMouseReleaseEvent();
        this.allowMouseScrollEvent = ScreenEventFactory.createAllowMouseScrollEvent();
        this.beforeMouseScrollEvent = ScreenEventFactory.createBeforeMouseScrollEvent();
        this.afterMouseScrollEvent = ScreenEventFactory.createAfterMouseScrollEvent();

        ScreenEvents.BEFORE_INIT.invoker().beforeInit(client, (Screen) (Object) this, width, height);
    }

    @Unique
    private void afterInit(MinecraftClient client, int width, int height) {
        ScreenEvents.AFTER_INIT.invoker().afterInit(client, (Screen) (Object) this, width, height);
    }

    @Unique
    private <T> Event<T> ensureEventsAreInitialized(Event<T> event) {
        if (event == null) {
            throw new IllegalStateException(String.format("[fabric-screen-api-v1] The current screen (%s) has not been correctly initialised, please send this crash log to the mod author. This is usually caused by calling setScreen on the wrong thread.", this.getClass().getName()));
        }

        return event;
    }

    @Override
    public Event<ScreenEvents.Remove> owo_getRemoveEvent() {
        return ensureEventsAreInitialized(this.removeEvent);
    }

    @Override
    public Event<ScreenEvents.BeforeTick> owo_getBeforeTickEvent() {
        return ensureEventsAreInitialized(this.beforeTickEvent);
    }

    @Override
    public Event<ScreenEvents.AfterTick> owo_getAfterTickEvent() {
        return ensureEventsAreInitialized(this.afterTickEvent);
    }

    @Override
    public Event<ScreenEvents.BeforeRender> owo_getBeforeRenderEvent() {
        return ensureEventsAreInitialized(this.beforeRenderEvent);
    }

    @Override
    public Event<ScreenEvents.AfterRender> owo_getAfterRenderEvent() {
        return ensureEventsAreInitialized(this.afterRenderEvent);
    }

    // Keyboard

    @Override
    public Event<ScreenEvents.AllowKeyPress> owo_getAllowKeyPressEvent() {
        return ensureEventsAreInitialized(this.allowKeyPressEvent);
    }

    @Override
    public Event<ScreenEvents.BeforeKeyPress> owo_getBeforeKeyPressEvent() {
        return ensureEventsAreInitialized(this.beforeKeyPressEvent);
    }

    @Override
    public Event<ScreenEvents.AfterKeyPress> owo_getAfterKeyPressEvent() {
        return ensureEventsAreInitialized(this.afterKeyPressEvent);
    }

    @Override
    public Event<ScreenEvents.AllowKeyRelease> owo_getAllowKeyReleaseEvent() {
        return ensureEventsAreInitialized(this.allowKeyReleaseEvent);
    }

    @Override
    public Event<ScreenEvents.BeforeKeyRelease> owo_getBeforeKeyReleaseEvent() {
        return ensureEventsAreInitialized(this.beforeKeyReleaseEvent);
    }

    @Override
    public Event<ScreenEvents.AfterKeyRelease> owo_getAfterKeyReleaseEvent() {
        return ensureEventsAreInitialized(this.afterKeyReleaseEvent);
    }

    // Mouse

    @Override
    public Event<ScreenEvents.AllowMouseClick> owo_getAllowMouseClickEvent() {
        return ensureEventsAreInitialized(this.allowMouseClickEvent);
    }

    @Override
    public Event<ScreenEvents.BeforeMouseClick> owo_getBeforeMouseClickEvent() {
        return ensureEventsAreInitialized(this.beforeMouseClickEvent);
    }

    @Override
    public Event<ScreenEvents.AfterMouseClick> owo_getAfterMouseClickEvent() {
        return ensureEventsAreInitialized(this.afterMouseClickEvent);
    }

    @Override
    public Event<ScreenEvents.AllowMouseRelease> owo_getAllowMouseReleaseEvent() {
        return ensureEventsAreInitialized(this.allowMouseReleaseEvent);
    }

    @Override
    public Event<ScreenEvents.BeforeMouseRelease> owo_getBeforeMouseReleaseEvent() {
        return ensureEventsAreInitialized(this.beforeMouseReleaseEvent);
    }

    @Override
    public Event<ScreenEvents.AfterMouseRelease> owo_getAfterMouseReleaseEvent() {
        return ensureEventsAreInitialized(this.afterMouseReleaseEvent);
    }

    @Override
    public Event<ScreenEvents.AllowMouseScroll> owo_getAllowMouseScrollEvent() {
        return ensureEventsAreInitialized(this.allowMouseScrollEvent);
    }

    @Override
    public Event<ScreenEvents.BeforeMouseScroll> owo_getBeforeMouseScrollEvent() {
        return ensureEventsAreInitialized(this.beforeMouseScrollEvent);
    }

    @Override
    public Event<ScreenEvents.AfterMouseScroll> owo_getAfterMouseScrollEvent() {
        return ensureEventsAreInitialized(this.afterMouseScrollEvent);
    }
}
