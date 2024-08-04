package io.wispforest.owo.client.screens;

import java.util.function.Consumer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Stateful slot generation utility for easily
 * arranging the slot grid used in a {@link net.minecraft.world.inventory.AbstractContainerMenu}
 */
public final class SlotGenerator {

    private int anchorX, anchorY;
    private int horizontalSpacing = 0;
    private int verticalSpacing = 0;

    private SlotFactory slotFactory = Slot::new;
    private Consumer<Slot> slotConsumer;

    private SlotGenerator(Consumer<Slot> slotConsumer, int anchorX, int anchorY) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.slotConsumer = slotConsumer;
    }

    /**
     * Begin generating slots into {@code slotConsumer}, starting at
     * ({@code anchorX}, {@code anchorY}). Usually, the {@code slotConsumer}
     * will be the {@code addSlot} method of the screen handler for which
     * slots are being generated
     * <p>
     * <pre>
     * {@code
     * SlotGenerator.begin(this::addSlot, 50, 10)
     *     .grid(someInventory, 0, 3, 3) // add a 3x3 grid of slots 0-8 of 'someInventory'
     *     .moveTo(10, 100)
     *     .playerInventory(playerInventory); // add the player inventory and hotbar slots
     * }
     * </pre>
     */
    public static SlotGenerator begin(Consumer<Slot> slotConsumer, int anchorX, int anchorY) {
        return new SlotGenerator(slotConsumer, anchorX, anchorY);
    }

    /**
     * Move the top-left anchor point of generated grids to ({@code anchorX}, {@code anchorY})
     */
    public SlotGenerator moveTo(int anchorX, int anchorY) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        return this;
    }

    /**
     * Shorthand for calling both {@link #horizontalSpacing} and
     * {@link #verticalSpacing} with {@code spacing}
     */
    public SlotGenerator spacing(int spacing) {
        this.horizontalSpacing = spacing;
        this.verticalSpacing = spacing;
        return this;
    }

    public SlotGenerator horizontalSpacing(int horizontalSpacing) {
        this.horizontalSpacing = horizontalSpacing;
        return this;
    }

    public SlotGenerator verticalSpacing(int verticalSpacing) {
        this.verticalSpacing = verticalSpacing;
        return this;
    }

    public SlotGenerator slotConsumer(Consumer<Slot> slotConsumer) {
        this.slotConsumer = slotConsumer;
        return this;
    }

    /**
     * Reset the slot factory of this generator
     * to the default {@link Slot#Slot(Container, int, int, int)} constructor
     */
    public SlotGenerator defaultSlotFactory() {
        this.slotFactory = Slot::new;
        return this;
    }

    /**
     * Set the slot factory of this generator, used for instantiating
     * each generated slot, to {@code slotFactory}
     */
    public SlotGenerator slotFactory(SlotFactory slotFactory) {
        this.slotFactory = slotFactory;
        return this;
    }

    public SlotGenerator grid(Container inventory, int startIndex, int width, int height) {
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                slotConsumer.accept(this.slotFactory.create(
                        inventory,
                        startIndex + row * width + column,
                        anchorX + column * (18 + this.horizontalSpacing),
                        anchorY + row * (18 + this.verticalSpacing)
                ));
            }
        }

        return this;
    }

    public SlotGenerator playerInventory(Inventory playerInventory) {
        this.grid(playerInventory, 9, 9, 3);
        this.anchorY += 58;
        this.grid(playerInventory, 0, 9, 1);
        this.anchorY -= 58;

        return this;
    }

    @FunctionalInterface
    public interface SlotFactory {
        Slot create(Container inventory, int index, int x, int y);
    }
}
