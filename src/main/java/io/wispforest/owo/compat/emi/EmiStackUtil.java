package io.wispforest.owo.compat.emi;

import dev.emi.emi.api.FabricEmiStack;
import dev.emi.emi.api.stack.EmiStack;
import io.wispforest.owo.util.ViewerStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public class EmiStackUtil {
    public static ViewerStack fromEmi(EmiStack stack) {
        if (stack.getKey() instanceof Item item) {
            return ViewerStack.OfItem.of(stack.getItemStack());
        } else if (stack.getKey() instanceof Fluid fluid) {
            return new ViewerStack.OfFluid(FluidVariant.of(fluid, stack.getComponentChanges()), stack.getAmount());
        } else {
            // TODO: custom EMI stack.
            return ViewerStack.OfItem.EMPTY;
        }
    }

    public static EmiStack toEmi(ViewerStack stack) {
        if (stack instanceof ViewerStack.OfItem ofItem) {
            return EmiStack.of(ofItem.asStack());
        } else if (stack instanceof ViewerStack.OfFluid ofFluid) {
            return FabricEmiStack.of(ofFluid.fluid(), ofFluid.count());
        } else {
            throw new IllegalStateException("Invalid ViewerStack");
        }
    }
}
