package io.wispforest.owo.mixin.braid;

import com.mojang.blaze3d.platform.Window;
import io.wispforest.owo.braid.core.BraidWindow;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Window.class)
public class WindowMixin {

    @ModifyArg(method = "createGlfwWindow", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"), index = 4)
    private static long injectContextShare(long original) {
        if (!BraidWindow.SHARE_NEXT_WINDOW_INSTANCE.get()) {
            return original;
        }

        BraidWindow.SHARE_NEXT_WINDOW_INSTANCE.set(false);
        return Minecraft.getInstance().getWindow().handle();
    }

}
