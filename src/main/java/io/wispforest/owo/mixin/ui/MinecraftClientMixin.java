package io.wispforest.owo.mixin.ui;

import com.mojang.blaze3d.platform.Window;
import io.wispforest.owo.ui.event.ClientRenderCallback;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import io.wispforest.owo.ui.util.DisposableScreen;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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

@Mixin(Minecraft.class)
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
        WindowResizeCallback.EVENT.invoker().onResized((Minecraft) (Object) this, this.window);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setPhase(Ljava/lang/String;)V", ordinal = 1))
    private void beforeRender(boolean tick, CallbackInfo ci) {
        ClientRenderCallback.BEFORE.invoker().onRender((Minecraft) (Object) this);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;swapBuffers()V", shift = At.Shift.AFTER))
    private void afterRender(boolean tick, CallbackInfo ci) {
        ClientRenderCallback.AFTER.invoker().onRender((Minecraft) (Object) this);
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
                    report.addCategory("Screen being disposed: ")
                            .setDetail("Screen class", disposable.getClass())
                            .setDetail("Screen being closed", this.currentScreen)
                            .setDetail("Total screens to dispose", this.screensToDispose.size());

                    throw new ReportedException(report);
                }
            }

            this.screensToDispose.clear();
        }
    }
}
