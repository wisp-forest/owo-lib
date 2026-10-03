package io.wispforest.owo.mixin.braid;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.SDLEventHandler;
import io.wispforest.owo.braid.core.BraidWindow;
import org.lwjgl.sdl.SDL_Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SDLEventHandler.class)
public class SDLEventHandlerMixin {

    @WrapOperation(method = "pollEvents", at = @At(value = "INVOKE", target = "Lorg/lwjgl/sdl/SDLEvents;SDL_PollEvent(Lorg/lwjgl/sdl/SDL_Event;)Z"))
    private boolean owo$routeBraidWindowEvents(SDL_Event event, Operation<Boolean> original) {
        while (original.call(event)) {
            if (!BraidWindow.dispatchEvent(event)) return true;
        }

        return false;
    }
}