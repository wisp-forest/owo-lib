package io.wispforest.owo.neoforge.api;

import com.google.common.base.Suppliers;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RegistryUtils {

    private static final Map<ResourceKey<? extends Registry<?>>, Map<Identifier, Supplier<Object>>> registryEntries = new HashMap<>();

    public static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void registerArgumentType(Identifier id, Class<? extends A> clazz, ArgumentTypeInfo<A, T> serializer) {
        RegistryUtils.registerDeferred(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, id, () -> ArgumentTypeInfos.registerByClass(clazz, (ArgumentTypeInfo) serializer));
    }

    public static <V, T extends V> T register(Registry<V> registry, Identifier location, T value) {
        registerDeferred(registry, location, () -> value);

        return value;
    }

    public static <V, T extends V> Supplier<T> registerDeferred(Registry<V> registry, Identifier location, Supplier<T> value) {
        var sup = Suppliers.memoize(value::get);

        registryEntries
            .computeIfAbsent(registry.key(), _ -> new LinkedHashMap<>())
            .put(location, sup::get);

        return sup;
    }

    /*
        - Alex
        - Lea
        - Jakob
        - Zeal
        - Sugar
        - Me
        -
     */

    @ApiStatus.Internal
    public static void init(IEventBus bus) {
        bus.<RegisterEvent>addListener(event -> {
            var entries = registryEntries.remove(event.getRegistryKey());
            if (entries != null) {
                registerEntries(event.getRegistryKey(), event, entries);
            }
        });
    }

    private static void registerEntries(ResourceKey key, RegisterEvent event, Map<Identifier, Supplier<Object>> entries) {
        event.register(key, helper -> entries.forEach((id, entry) -> helper.register(id, entry.get())));
    }
}
