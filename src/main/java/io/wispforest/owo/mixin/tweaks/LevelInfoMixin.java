package io.wispforest.owo.mixin.tweaks;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.serialization.Dynamic;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ext.GameruleDisabler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.level.LevelInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Mixin(LevelInfo.class)
public class LevelInfoMixin {

    @Shadow
    @Final
    private GameRules gameRules;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void simulationIsForNerds(String name, GameMode gameMode, boolean hardcore, Difficulty difficulty, boolean allowCommands, GameRules gameRules, DataConfiguration dataConfiguration, CallbackInfo ci) {
        if (!(Owo.DEBUG && FabricLoader.getInstance().isDevelopmentEnvironment() && GameruleDisabler.shouldDisableGamerules() && !name.equals("Demo World"))) return;

        this.gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, null);
        this.gameRules.get(GameRules.DO_WEATHER_CYCLE).set(false, null);

        GameruleDisabler.hasGamerulesBeenAdjusted = true;
    }

    // Aim of this adjustment to the mixin is to toggle such off by default when ever constructed outside of decoding
    @WrapMethod(method = "fromDynamic")
    private static LevelInfo wrapIfGameruleToggled1(Dynamic<?> dynamic, DataConfiguration dataConfiguration, Operation<LevelInfo> original) {
        return GameruleDisabler.disableGamemodeAdjustment(() -> original.call(dynamic, dataConfiguration));
    }

    @WrapMethod(method = "withGameMode")
    private LevelInfo wrapIfGameruleToggled3(GameMode mode, Operation<LevelInfo> original) {
        return GameruleDisabler.disableGamemodeAdjustment(() -> original.call(mode));
    }

    @WrapMethod(method = "withDifficulty")
    private LevelInfo wrapIfGameruleToggled3(Difficulty difficulty, Operation<LevelInfo> original) {
        return GameruleDisabler.disableGamemodeAdjustment(() -> original.call(difficulty));
    }

    @WrapMethod(method = "withDataConfiguration")
    private LevelInfo wrapIfGameruleToggled3(DataConfiguration dataConfiguration, Operation<LevelInfo> original) {
        return GameruleDisabler.disableGamemodeAdjustment(() -> original.call(dataConfiguration));
    }

    @WrapMethod(method = "withCopiedGameRules")
    private LevelInfo wrapIfGameruleToggled2(Operation<LevelInfo> original) {
        return GameruleDisabler.disableGamemodeAdjustment(original::call);
    }
}
