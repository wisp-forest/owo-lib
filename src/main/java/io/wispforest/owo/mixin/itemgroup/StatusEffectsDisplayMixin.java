package io.wispforest.owo.mixin.itemgroup;

import io.wispforest.owo.itemgroup.OwoItemGroup;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StatusEffectsDisplay.class)
public class StatusEffectsDisplayMixin {

    @ModifyVariable(method = "drawStatusEffects(Lnet/minecraft/client/gui/DrawContext;II)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;width:I", ordinal = 0),
            ordinal = 2)
    private int shiftStatusEffects(int x) {
        if (!((Object) this instanceof CreativeInventoryScreen)) return x;
        if (!(CreativeInventoryScreenAccessor.owo$getSelectedTab() instanceof OwoItemGroup group)) return x;
        if (group.getButtons().isEmpty()) return x;

        return x + 28;
    }

}
