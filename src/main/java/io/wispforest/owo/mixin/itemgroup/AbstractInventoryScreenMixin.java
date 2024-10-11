package io.wispforest.owo.mixin.itemgroup;

import io.wispforest.owo.itemgroup.OwoItemGroup;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StatusEffectsDisplay.class)
public class AbstractInventoryScreenMixin {

    @Shadow @Final private HandledScreen<?> parent;

    @ModifyVariable(method = "drawStatusEffects(Lnet/minecraft/client/gui/DrawContext;II)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;width:I", ordinal = 0), ordinal = 2)
    private int shiftStatusEffects(int x) {
        if (!(this.parent instanceof CreativeInventoryScreen)) return x;
        if (!(CreativeInventoryScreenAccessor.owo$getSelectedTab() instanceof OwoItemGroup group)) return x;
        if (group.getButtons().isEmpty()) return x;

        return x + 28;
    }

}
