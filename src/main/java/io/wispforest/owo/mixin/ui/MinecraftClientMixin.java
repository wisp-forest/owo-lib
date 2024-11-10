package io.wispforest.owo.mixin.ui;

import io.wispforest.owo.ui.event.ClientRenderCallback;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import io.wispforest.owo.ui.util.DisposableScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Unique
    private final Set<DisposableScreen> screensToDispose = new HashSet<>();

    @Shadow
    @Final
    private Window window;

    @Shadow
    @Nullable
    public Screen currentScreen;

    @Inject(method = "onResolutionChanged", at = @At("TAIL"))
    private void captureResize(CallbackInfo ci) {
        WindowResizeCallback.EVENT.invoker().onResized((MinecraftClient) (Object) this, this.window);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setPhase(Ljava/lang/String;)V", ordinal = 1))
    private void beforeRender(boolean tick, CallbackInfo ci) {
        ClientRenderCallback.BEFORE.invoker().onRender((MinecraftClient) (Object) this);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;swapBuffers(Lnet/minecraft/client/util/tracy/TracyFrameCapturer;)V", shift = At.Shift.AFTER))
    private void afterRender(boolean tick, CallbackInfo ci) {
        ClientRenderCallback.AFTER.invoker().onRender((MinecraftClient) (Object) this);
    }

    @Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;removed()V"))
    private void captureSetScreen(Screen screen, CallbackInfo ci) {
        if (screen != null && this.currentScreen instanceof DisposableScreen disposable) {
            this.screensToDispose.add(disposable);
        } else if (screen == null) {
            if (this.currentScreen instanceof DisposableScreen disposable) {
                this.screensToDispose.add(disposable);
            }

            for (var disposable : this.screensToDispose) {
                try {
                    disposable.dispose();
                } catch (Throwable error) {
                    var report = new CrashReport("Failed to dispose screen", error);
                    report.addElement("Screen being disposed: ")
                            .add("Screen class", disposable.getClass())
                            .add("Screen being closed", this.currentScreen)
                            .add("Total screens to dispose", this.screensToDispose.size());

                    throw new CrashException(report);
                }
            }

            this.screensToDispose.clear();
        }
    }
}
