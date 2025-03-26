package io.wispforest.owo.ext;

import io.wispforest.owo.Owo;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.text.Text;
import net.minecraft.world.level.LevelInfo;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

@ApiStatus.Internal
public class GameruleDisabler {

    private static final ThreadLocal<Boolean> SHOULD_DISABLE_GAME_RULES = ThreadLocal.withInitial(() -> true);

    @ApiStatus.Internal
    public static boolean hasGamerulesBeenAdjusted = false;

    public static LevelInfo disableGamemodeAdjustment(Supplier<LevelInfo> infoSupplier) {
        SHOULD_DISABLE_GAME_RULES.set(false);

        var info = infoSupplier.get();

        SHOULD_DISABLE_GAME_RULES.set(true);

        return info;
    }

    public static boolean shouldDisableGamerules() {
        return SHOULD_DISABLE_GAME_RULES.get();
    }

    static {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!hasGamerulesBeenAdjusted) return;

            handler.player.sendMessage(
                    Text.literal("")
                            .append(Owo.PREFIX)
                            .append(Text.of("The given Gamerules [Daylight and Weather Cycles] have been set to false for the NEW world.")));

            hasGamerulesBeenAdjusted = false;
        });
    }
}
