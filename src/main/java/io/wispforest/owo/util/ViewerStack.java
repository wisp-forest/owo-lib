package io.wispforest.owo.util;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

// TODO: pick better name
public interface ViewerStack {
    long count();

    DataComponentPatch componentChanges();

    // TODO: WILL NEED TO HANDLE BINARY COMPAT SOMEHOW???
    record OfItem(ItemResource item, long count) implements ViewerStack {
        public static final OfItem EMPTY = new OfItem(ItemResource.of(ItemStack.EMPTY), 0);

        public static OfItem of(Item item) {
            return new OfItem(ItemResource.of(item), 1);
        }

        public static OfItem of(ItemStack stack) {
            return new OfItem(ItemResource.of(stack), stack.getCount());
        }

        public ItemStack asStack() {
            return item.toStack((int) count);
        }

        @Override
        public DataComponentPatch componentChanges() {
            return item.getComponentsPatch();
        }
    }

    record OfFluid(FluidResource fluid, long count) implements ViewerStack {
        @Override
        public DataComponentPatch componentChanges() {
            return fluid.getComponentsPatch();
        }
    }
}
