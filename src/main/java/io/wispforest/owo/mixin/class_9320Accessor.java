package io.wispforest.owo.mixin;

import net.minecraft.class_9320;
import net.minecraft.component.ComponentChanges;
import net.minecraft.loot.condition.LootCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(class_9320.class)
public interface class_9320Accessor {
    @Invoker("<init>")
    static class_9320 createClass_9320(List<LootCondition> list, ComponentChanges componentChanges) {
        throw new UnsupportedOperationException();
    }
}
