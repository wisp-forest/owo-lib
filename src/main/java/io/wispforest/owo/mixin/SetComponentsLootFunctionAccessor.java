package io.wispforest.owo.mixin;

import net.minecraft.component.ComponentChanges;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.function.SetComponentsLootFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(SetComponentsLootFunction.class)
public interface SetComponentsLootFunctionAccessor {
    @Invoker("<init>")
    static SetComponentsLootFunction createSetComponentsLootFunction(List<LootCondition> list, ComponentChanges componentChanges) {
        throw new UnsupportedOperationException();
    }
}
