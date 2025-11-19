package io.wispforest.owo.mixin.itemgroup;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(net.minecraft.client.gui.screen.Screen.class)
public interface ScreenAccessor {
    @Invoker("clearAndInit")
    void owo$clearAndInit();

    @Invoker("remove")
    void owo$remove(Element child);

    @Accessor("drawables")
    List<Drawable> owo$drawables();
}
