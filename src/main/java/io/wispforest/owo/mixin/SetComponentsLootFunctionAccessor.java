package io.wispforest.owo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

@Mixin(SetComponentsFunction.class)
public interface SetComponentsLootFunctionAccessor {
    @Invoker("<init>")
    static SetComponentsFunction createSetComponentsLootFunction(List<LootItemCondition> list, DataComponentPatch componentChanges) {
        throw new UnsupportedOperationException();
    }
}
