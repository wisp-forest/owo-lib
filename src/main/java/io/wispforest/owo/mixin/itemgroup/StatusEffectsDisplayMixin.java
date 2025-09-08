package io.wispforest.owo.mixin.itemgroup;

import io.wispforest.owo.itemgroup.base.OwoItemGroup;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StatusEffectsDisplay.class)
public abstract class StatusEffectsDisplayMixin {

    @ModifyVariable(method = "drawStatusEffects(Lnet/minecraft/client/gui/DrawContext;II)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;width:I", ordinal = 0),
            ordinal = 2)
    private int shiftStatusEffects(int x) {
        if ((Object) this instanceof CreativeInventoryScreen) {
            var extension = OwoItemGroup.getExtension(CreativeInventoryScreenAccessor.owo$getSelectedTab());

            if (extension != null && !extension.getButtons().isEmpty()) {
                x += 28;
            }
        }

        return x;
    }

}
