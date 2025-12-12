package io.wispforest.owo.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class QueuedChannelSet {
    public static Set<Identifier> channels;
}
