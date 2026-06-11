package io.wispforest.owo.network;

import io.wispforest.owo.neoforge.env.EnvType;
import io.wispforest.owo.neoforge.env.Environment;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class QueuedChannelSet {
    public static Set<Identifier> channels;
}
