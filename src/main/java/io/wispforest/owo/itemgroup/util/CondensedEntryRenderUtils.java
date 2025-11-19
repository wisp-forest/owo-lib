package io.wispforest.owo.itemgroup.util;


import io.wispforest.owo.Owo;
import io.wispforest.owo.itemgroup.impl.CondensedEntryStates;
import io.wispforest.owo.itemgroup.core.CondensedEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

///
/// Handles the various rendering parts that the [CondensedEntry]
/// may need like the parent icon, entry background, and the entry outline
///
@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class CondensedEntryRenderUtils {

    private static final Identifier CONDENSED_ENTRY_BACKGROUND = Identifier.of("owo", "textures/gui/condensed_entry/background.png");

    private static final Identifier PLUS_ICON = Identifier.of("owo", "textures/gui/condensed_entry/plus_logo.png");
    private static final Identifier MINUS_ICON = Identifier.of("owo", "textures/gui/condensed_entry/minus_logo.png");

    public static void renderBackground(DrawContext context, Slot slot, CondensedEntry.State state) {
        if(!(slot instanceof CreativeInventoryScreen.LockableSlot && state.showChildren() && Owo.CONFIG.showBackgroundColor())) return;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, CONDENSED_ENTRY_BACKGROUND, slot.x - 1, slot.y - 1, 0, 0, 18, 18, 18, 18);
    }

    public static void renderOnTop(DrawContext context, Slot slot, boolean isParent, CondensedEntry.State state){
        if(!(slot instanceof CreativeInventoryScreen.LockableSlot)) return;

        int minX = slot.x;
        int minY = slot.y;

        int maxX = minX + 16;
        int maxY = minY + 16;

        if(state.showChildren() && Owo.CONFIG.showBorderColor()) {
            var outlineColor = Owo.CONFIG.borderColor()/*.interpolate(Color.ofArgb(0xFF000000), 0.25f)*/;//borderColor;

            if (!isSlotAboveFromEntry(slot, state)) {
                var sideOffset = 1;

                // Handles inner corner edge cases making the entire outline a seamless connection
                if (isSlotAboveFromEntry(new Slot(slot.inventory, slot.getIndex() + 1, 0, 0), state)) {
                    sideOffset = 2;
                }

                context.fill(minX - ((slot.getIndex() % 9 != 0) ? sideOffset : 1), minY - 1, maxX + sideOffset, maxY - 16, outlineColor.argb());
            }

            if (!isSlotBelowFromEntry(slot, state)) {
                var sideOffset = 1;

                // Handles inner corner edge cases making the entire outline a seamless connection
                if (slot.getIndex() % 9 != 0 && isSlotBelowFromEntry(new Slot(slot.inventory, slot.getIndex() - 1, 0, 0), state)) {
                    sideOffset = 2;
                }

                context.fill(minX - sideOffset, minY + 16, maxX + sideOffset, maxY + 1, outlineColor.argb());
            }

            if (!isSlotRightFromEntry(slot, state)) {
                context.fill(minX + 16, minY - 1, maxX + 1, maxY + 1, outlineColor.argb());
            }

            if (!isSlotLeftFromEntry(slot, state)) {
                context.fill(minX - 1, minY - 1, maxX - 16, maxY + 1, outlineColor.argb());
            }
        }

        if(isParent) {
            var id = state.showChildren() ? MINUS_ICON : PLUS_ICON;

            context.drawTexture(RenderPipelines.GUI_TEXTURED, id, minX, minY, 0, 0, 16, 16, 16, 16);
        }
    }

    private static boolean isSlotAboveFromEntry(Slot slot, CondensedEntry.State state){
        int topIndex = slot.getIndex() - 9;

        return topIndex >= 0 && isFromEntry(slot, topIndex, state);
    }

    private static boolean isSlotBelowFromEntry(Slot slot, CondensedEntry.State state){
        int bottomIndex = slot.getIndex() + 9;

        return bottomIndex < slot.inventory.size() && isFromEntry(slot, bottomIndex, state);
    }

    private static boolean isSlotLeftFromEntry(Slot slot, CondensedEntry.State state){
        if (slot.id % 9 == 0) return false;

        int leftIndex = slot.getIndex() - 1;

        return leftIndex < slot.inventory.size() && isFromEntry(slot, leftIndex, state);
    }

    private static boolean isSlotRightFromEntry(Slot slot, CondensedEntry.State state){
        if (slot.id % 9 == 8) return false;

        int rightIndex = slot.getIndex() + 1;

        return rightIndex < slot.inventory.size() && isFromEntry(slot, rightIndex, state);
    }

    private static boolean isFromEntry(Slot slot, int index, CondensedEntry.State state) {
        var results = CondensedEntryStates.getState(slot.inventory.getStack(index));

        if (results == null) return false;

        return results.state().entry().id() == state.entry().id();
    }
}
