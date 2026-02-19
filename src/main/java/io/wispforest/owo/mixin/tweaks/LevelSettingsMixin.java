package io.wispforest.owo.mixin.tweaks;

import io.wispforest.owo.Owo;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.fml.loading.FMLLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelSettings.class)
public class LevelSettingsMixin {

    @Shadow
    @Final
    private GameRules gameRules;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void simulationIsForNerds(String name, GameType gameMode, boolean hardcore, Difficulty difficulty, boolean allowCommands, GameRules gameRules, WorldDataConfiguration dataConfiguration, CallbackInfo ci) {
        if (!(Owo.DEBUG && !FMLLoader.getCurrent().isProduction())) return;

        this.gameRules.set(GameRules.ADVANCE_TIME, false, null);
        this.gameRules.set(GameRules.ADVANCE_WEATHER, false, null);
    }

}
