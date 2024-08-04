package io.wispforest.uwu;

import C;
import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import io.wispforest.owo.client.screens.SyncedProperty;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class EpicScreenHandler extends AbstractContainerMenu {
    private static final char[] VOWELS = {'a', 'e', 'i', 'o', 'u'};
    private static final char[] CONSONANTS = {'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z'};

    private final ContainerLevelAccess context;

    public final SyncedProperty<String> epicNumber;

    public EpicScreenHandler(int syncId, Inventory inventory) {
        this(syncId, inventory, ContainerLevelAccess.NULL);
    }

    public EpicScreenHandler(int syncId, Inventory inventory, ContainerLevelAccess context) {
        super(Uwu.EPIC_SCREEN_HANDLER_TYPE, syncId);
        this.context = context;
        SlotGenerator.begin(this::addSlot, 8, 84)
                .grid(new SimpleContainer(4), 0, 4, 1)
                .playerInventory(inventory);

        this.epicNumber = this.createProperty(String.class, "");
        this.epicNumber.set(generateEpicName());

        this.addClientboundMessage(MaldMessage.class, this::handleMald);
        this.addServerboundMessage(EpicMessage.class, this::handleEpic);
    }

    private void handleMald(MaldMessage r) {
        this.sendMessage(new EpicMessage(r.number));
    }

    private void handleEpic(EpicMessage r) {
        this.epicNumber.set(generateEpicName() + " " + r.number);
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType actionType, Player player) {
        if (!player.level().isClientSide)
            this.sendMessage(new MaldMessage(slotIndex));

        super.clicked(slotIndex, button, actionType, player);
    }

    // made originally by det hoonter tm
    private static String generateEpicName() {
        var sb = new StringBuilder();

        for (int i = 0; i < 4; ++i) {
            var consonant = CONSONANTS[ThreadLocalRandom.current().nextInt(CONSONANTS.length)];

            if (i == 0) consonant = Character.toUpperCase(consonant);

            sb.append(consonant);
            sb.append(VOWELS[ThreadLocalRandom.current().nextInt(VOWELS.length)]);
        }

        return sb.toString();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ScreenUtils.handleSlotTransfer(this, index, 4);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public record EpicMessage(int number) {}

    public record MaldMessage(int number) {}
}
