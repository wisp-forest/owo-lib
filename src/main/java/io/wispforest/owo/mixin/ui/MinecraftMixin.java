package io.wispforest.owo.mixin.ui;

import com.mojang.blaze3d.platform.Window;
import io.wispforest.owo.ui.event.ClientRenderCallback;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import io.wispforest.owo.ui.util.DisposableScreen;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
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
public class MinecraftMixin {

    @Unique
    private final Set<DisposableScreen> screensToDispose = new HashSet<>();

    @Shadow
    @Final
    private Window window;

    @Shadow
    @Final
    public Gui gui;

    @Inject(method = "resizeGui", at = @At("TAIL"))
    private void captureResize(CallbackInfo ci) {
        WindowResizeCallback.EVENT.invoker().onResized((Minecraft) (Object) this, this.window);
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;setErrorSection(Ljava/lang/String;)V", ordinal = 1))
    private void beforeRender(boolean tick, CallbackInfo ci) {
        ClientRenderCallback.BEFORE.invoker().onRender((Minecraft) (Object) this);
    }

    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuSurface;present()V", shift = At.Shift.AFTER))
    private void afterRender(boolean tick, CallbackInfo ci) {
        ClientRenderCallback.AFTER.invoker().onRender((Minecraft) (Object) this);
    }

    @Inject(method = "renderFrame", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;frameTimeNs:J", opcode = Opcodes.PUTFIELD))
    private void beforeSwap(boolean tick, CallbackInfo ci) {
        ClientRenderCallback.BEFORE_SWAP.invoker().onRender((Minecraft) (Object) this);
    }

    @Inject(method = "setScreenAndShow", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void captureSetScreen(Screen screen, CallbackInfo ci) {
        var currentScreen = this.gui.screen();
        if (screen != null && currentScreen instanceof DisposableScreen disposable) {
            this.screensToDispose.add(disposable);
        } else if (screen == null) {
            if (currentScreen instanceof DisposableScreen disposable) {
                this.screensToDispose.add(disposable);
            }

            for (var disposable : this.screensToDispose) {
                try {
                    disposable.dispose();
                } catch (Throwable error) {
                    var report = new CrashReport("Failed to dispose screen", error);
                    report.addCategory("Screen being disposed: ")
                            .setDetail("Screen class", disposable.getClass())
                            .setDetail("Screen being closed", currentScreen)
                            .setDetail("Total screens to dispose", this.screensToDispose.size());

                    throw new ReportedException(report);
                }
            }

            this.screensToDispose.clear();
        }
    }
}
