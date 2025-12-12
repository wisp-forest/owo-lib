package io.wispforest.owo.compat.rei;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.fabric.FluidStackHooksFabric;
import io.wispforest.owo.util.ViewerStack;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.world.item.ItemStack;

public class ReiStackUtil {
    public static ViewerStack fromRei(EntryStack<?> stack) {
        if (stack.getValue() instanceof ItemStack item) {
            return ViewerStack.OfItem.of(item);
        } else if (stack.getValue() instanceof FluidStack fluid) {
            return new ViewerStack.OfFluid(FluidVariant.of(fluid.getFluid(), fluid.getPatch()), fluid.getAmount());
        } else {
            // TODO: custom REI stack.
            return ViewerStack.OfItem.EMPTY;
        }
    }

    public static EntryStack<?> toRei(ViewerStack stack) {
        if (stack instanceof ViewerStack.OfItem ofItem) {
            return EntryStacks.of(ofItem.asStack());
        } else if (stack instanceof ViewerStack.OfFluid ofFluid) {
            return EntryStacks.of(FluidStackHooksFabric.fromFabric(ofFluid.fluid(), ofFluid.count()));
        } else {
            throw new IllegalStateException("Invalid ViewerStack");
        }
    }
}
