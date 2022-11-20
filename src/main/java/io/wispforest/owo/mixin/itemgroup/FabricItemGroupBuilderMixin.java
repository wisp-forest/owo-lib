package io.wispforest.owo.mixin.itemgroup;

import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.json.OwoItemGroupLoader;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(value = FabricItemGroupBuilder.class)
public class FabricItemGroupBuilderMixin {

    @Shadow
    private Identifier identifier;

    @Shadow(remap = false)
    private Supplier<ItemStack> stackSupplier;

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "build()Lnet/minecraft/item/ItemGroup;", at = @At("RETURN"), cancellable = true)
    private void afterConstructor(CallbackInfoReturnable<ItemGroup> cir) {
        if (OwoItemGroup.class.isAssignableFrom(this.getClass())) return;

        var createdGroup = OwoItemGroupLoader.onGroupCreated(cir.getReturnValue());
        if (createdGroup != null) cir.setReturnValue(createdGroup);
    }

}