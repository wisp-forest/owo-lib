package io.wispforest.owo.util;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// TODO: pick better name
public interface ViewerStack {
    long count();

    DataComponentPatch componentChanges();

    record OfItem(ItemVariant item, long count) implements ViewerStack {
        public static final OfItem EMPTY = new OfItem(ItemVariant.of(ItemStack.EMPTY), 0);

        public static OfItem of(Item item) {
            return new OfItem(ItemVariant.of(item), 1);
        }

        public static OfItem of(ItemStack stack) {
            return new OfItem(ItemVariant.of(stack), stack.getCount());
        }

        public ItemStack asStack() {
            return item.toStack((int) count);
        }

        @Override
        public DataComponentPatch componentChanges() {
            return item.getComponents();
        }
    }

    record OfFluid(FluidVariant fluid, long count) implements ViewerStack {
        @Override
        public DataComponentPatch componentChanges() {
            return fluid.getComponents();
        }
    }
}
