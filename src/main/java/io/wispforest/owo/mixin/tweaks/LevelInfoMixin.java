package io.wispforest.owo.mixin.tweaks;

import io.wispforest.owo.Owo;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelSettings.class)
public class LevelInfoMixin {

    @Shadow
    @Final
    private GameRules gameRules;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void simulationIsForNerds(String name, GameType gameMode, boolean hardcore, Difficulty difficulty, boolean allowCommands, GameRules gameRules, WorldDataConfiguration dataConfiguration, CallbackInfo ci) {
        if (!(Owo.DEBUG && FabricLoader.getInstance().isDevelopmentEnvironment())) return;

        this.gameRules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
        this.gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
    }

}
