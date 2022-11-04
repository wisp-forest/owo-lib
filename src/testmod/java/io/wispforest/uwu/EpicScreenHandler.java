package io.wispforest.uwu;

import io.wispforest.owo.client.screens.ScreenUtils;
import io.wispforest.owo.client.screens.SlotGenerator;
import io.wispforest.owo.client.screens.SyncedProperty;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.SlotActionType;

import java.util.concurrent.ThreadLocalRandom;

public class EpicScreenHandler extends ScreenHandler {
    private static final char[] VOWELS = {'a', 'e', 'i', 'o', 'u'};
    private static final char[] CONSONANTS = {'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z'};

    private final ScreenHandlerContext context;

    public final SyncedProperty<String> epicNumber;

    public EpicScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, ScreenHandlerContext.EMPTY);
    }

    public EpicScreenHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(Uwu.EPIC_SCREEN_HANDLER_TYPE, syncId);
        this.context = context;
        SlotGenerator.begin(this::addSlot, 8, 84)
            .grid(new SimpleInventory(4), 0, 4, 1)
            .playerInventory(inventory);

        this.epicNumber = addProperty(String.class, "");
        this.epicNumber.set(generateEpicName());

        addClientboundPacket(MaldPacket.class, this::handleMald);
        addServerboundPacket(EpicPacket.class, this::handleEpic);
    }

    private void handleMald(MaldPacket r) {
        sendPacket(new EpicPacket(r.number));
    }

    private void handleEpic(EpicPacket r) {
        this.epicNumber.set(generateEpicName() + " " + r.number);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (!player.world.isClient)
            sendPacket(new MaldPacket(slotIndex));

        super.onSlotClick(slotIndex, button, actionType, player);
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
    public ItemStack transferSlot(PlayerEntity player, int index) {
        return ScreenUtils.handleSlotTransfer(this, index, 4);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public record EpicPacket(int number) { }

    public record MaldPacket(int number) { }
}
