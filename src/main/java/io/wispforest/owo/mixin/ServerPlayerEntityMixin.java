package io.wispforest.owo.mixin;

import io.wispforest.owo.config.ConfigSynchronizer;
import io.wispforest.owo.config.Option;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ConfigSynchronizer.ServerPlayerEntityExtension {

    @Unique
    private final Map<String, Map<Option.Key, Object>> owo$optionStorage = new HashMap<>();

    @Override
    public Map<String, Map<Option.Key, Object>> owo$optionStorage() {
        return this.owo$optionStorage;
    }
}
