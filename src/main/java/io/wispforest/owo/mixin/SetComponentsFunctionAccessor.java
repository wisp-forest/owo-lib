package io.wispforest.owo.mixin;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;
import net.minecraft.core.Holder;

@Mixin(SetComponentsFunction.class)
public interface SetComponentsFunctionAccessor {
    @Invoker("<init>")
    static SetComponentsFunction createSetComponentsLootFunction(Optional<Holder<LootItemCondition>> condition, DataComponentPatch componentChanges) {
        throw new UnsupportedOperationException();
    }
}
