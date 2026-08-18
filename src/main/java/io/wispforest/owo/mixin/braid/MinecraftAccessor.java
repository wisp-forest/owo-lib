package io.wispforest.owo.mixin.braid;

import com.mojang.blaze3d.platform.MonitorManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor("monitorManager")
    MonitorManager owo$getMonitorManager();
}
