package io.wispforest.owo.mixin.ui;

import com.mojang.blaze3d.platform.Window;
import io.wispforest.owo.ui.event.ClientRenderCallback;
import io.wispforest.owo.ui.event.WindowResizeCallback;
import io.wispforest.owo.ui.renderstate.BlurQuadElementRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    @Final
    private Window window;

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

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;window:Lcom/mojang/blaze3d/platform/Window;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void initBlurRenderer(GameConfig args, CallbackInfo ci) {
        BlurQuadElementRenderState.initialize((Minecraft) (Object) this);
    }
}
