package com.glisco.uwu.mixin;

import com.glisco.uwu.Uwu;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    public void init(CallbackInfo ci) {
        this.addDrawableChild(new ButtonWidget(5, 5, 98, 20, new LiteralText("Play Unlinked SoundEvent"), (button -> {
            assert this.client != null;
            Uwu.OWO_SOUND_EVENT.playUnlinked(this.client, 1F, 1F);
        })));
    }
}
