package io.wispforest.owo.network;

import net.minecraft.util.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

@ApiStatus.Internal
@OnlyIn(Dist.CLIENT)
public class QueuedChannelSet {
    public static Set<Identifier> channels;
}
