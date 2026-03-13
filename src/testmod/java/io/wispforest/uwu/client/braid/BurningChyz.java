package io.wispforest.uwu.client.braid;

import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.widgets.sharedstate.ShareableState;
import io.wispforest.owo.braid.widgets.sharedstate.SharedState;
import net.minecraft.world.entity.player.Player;

public class BurningChyz extends ShareableState {
    public final Player chyz;

    public BurningChyz(Player chyz) {this.chyz = chyz;}

    public static Player of(BuildContext context) {
        return SharedState.getWithoutDependency(context, BurningChyz.class).chyz;
    }
}
