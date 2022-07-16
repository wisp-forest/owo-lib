package io.wispforest.owo.persistence;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class OwoPersistentStateManager {

    static HashMap<String, Pair<Function<NbtCompound, OwoPersistentState<?>>, Supplier<OwoPersistentState<?>>>> REGISTERED_STATES = new HashMap<>();

    @ApiStatus.Internal
    public static void init() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            PersistentStateManager manager = world.getPersistentStateManager();
            REGISTERED_STATES.forEach((identifier, functionSupplierPair) -> {
                manager.getOrCreate(functionSupplierPair.getLeft(), functionSupplierPair.getRight(), identifier);
            });
        });
    }

    /**
     * Get an instance of a persistent state from a world.
     * @param world The world
     * @param id The ID used to register the persistent state.
     * @return The instance of the persistent state on the world.
     * @param <T> The class of the persistent state.
     */
    public static <T extends OwoPersistentState<?>> T get(ServerWorld world, String id) {
        Pair<Function<NbtCompound, OwoPersistentState<?>>, Supplier<OwoPersistentState<?>>> val = REGISTERED_STATES.get(id);
        return (T) world.getPersistentStateManager().get(val.getLeft(), id);
    }

    /**
     * Register your OwoPersistentState ready for world load.
     * @param id The ID of your state - this will also be used as the .dat file name.
     * @param owoPersistentState Default instance of your OwoPersistentState class
     * @param <T> The type that extends OwoPersistentState
     * @return False if failed to register (unlikely). True if successfully registered.
     */
    public static <T extends OwoPersistentState<?>> boolean register(String id, T owoPersistentState) {
        try {
            T defaultInstance = (T) owoPersistentState.getClass().newInstance();
            REGISTERED_STATES.put(id, new Pair<>(defaultInstance::gather, () -> defaultInstance));
            return true;
        } catch (InstantiationException | IllegalAccessException e) {
            return false;
        }
    }
}
