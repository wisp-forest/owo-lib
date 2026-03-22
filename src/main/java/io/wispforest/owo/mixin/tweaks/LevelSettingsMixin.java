package io.wispforest.owo.mixin.tweaks;

import net.minecraft.world.level.LevelSettings;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelSettings.class)
public class LevelSettingsMixin {

//    @Shadow
//    @Final
//    private GameRules gameRules;
//
//    @Inject(method = "<init>", at = @At("TAIL"))
//    private void simulationIsForNerds(String name, GameType gameMode, boolean hardcore, Difficulty difficulty, boolean allowCommands, GameRules gameRules, WorldDataConfiguration dataConfiguration, CallbackInfo ci) {
//        if (!(Owo.DEBUG && FabricLoader.getInstance().isDevelopmentEnvironment())) return;
//
//        this.gameRules.set(GameRules.ADVANCE_TIME, false, null);
//        this.gameRules.set(GameRules.ADVANCE_WEATHER, false, null);
//    }

}
