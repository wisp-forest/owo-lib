package io.wispforest.owo.neoforge.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.callback.AddCallback;
import net.neoforged.neoforge.registries.callback.RegistryCallback;

public interface RegistryEntryAddedCallback<T> {
    /**
     * Called when a new entry is added to the registry.
     *
     * @param rawId the raw id of the entry
     * @param id the identifier of the entry
     * @param object the object that was added
     */
    void onEntryAdded(int rawId, Identifier id, T object);

    /**
     * Get the {@link Event} for the {@link RegistryEntryAddedCallback} for the given registry.
     *
     * @param registry the registry to get the event for
     * @return the event
     */
    static <T> Event<RegistryEntryAddedCallback<T>> event(Registry<T> registry) {
        return event(registry.key());
    }

    static <T> Event<RegistryEntryAddedCallback<T>> event(ResourceKey<? extends Registry<T>> key) {
        return RegistryEntryAddedCallbackImpl.event(key);
    }

    /**
     * Register a callback for all present and future entries in the registry.
     *
     * <p>Note: The callback is recursive and will be invoked for anything registered within the callback itself.
     *
     * @param registry the registry to listen to
     * @param consumer the callback that accepts a {@link Holder.Reference}
     */
    static <T> void allEntries(Registry<T> registry, Consumer<Holder.Reference<T>> consumer) {
        event(registry).register((_, id, _) -> consumer.accept(registry.get(id).orElseThrow()));
        // Call the consumer for all existing entries, after registering the callback.
        // This way if the callback registers a new entry, it will also be called for that entry.
        // It is also important to take a copy of the registry with .toList() to avoid concurrent modification exceptions if the callback modifies the registry.
        registry.listElements().toList().forEach(consumer);
    }
}

class RegistryEntryAddedCallbackImpl {
    private static Map<ResourceKey<? extends Registry<?>>, Event<RegistryEntryAddedCallback<?>>> addObjectEvents = new HashMap<>();
    private static Set<ResourceKey<? extends Registry<?>>> unhookedRegistries = new HashSet<>();

    static {
        BuiltInRegistries.REGISTRY.addCallback((AddCallback) (_, var2, var3, var4) -> {
            var itr = unhookedRegistries.iterator();

            var registryObj = (Registry<?>) var4;

            while (itr.hasNext()) {
                var unhookedRegistry = itr.next();
                if (registryObj.key().identifier().equals(unhookedRegistry.identifier())) {
                    setupRegEvent(registryObj);
                    itr.remove();
                }
            }
        });
    }

    private static <T> void setupRegEvent(Registry<T> registry) {
        setupRegEvent(registry, (Event<RegistryEntryAddedCallback<T>>) (Event) addObjectEvents.get(registry.key()));
    }

    private static <T> void setupRegEvent(Registry<T> registry, Event<RegistryEntryAddedCallback<T>> event) {
        registry.addCallback((AddCallback<T>) (_, id, key, t) -> event.invoker().onEntryAdded(id, key.identifier(), t));
    }

    static <T> Event<RegistryEntryAddedCallback<T>> event(ResourceKey<? extends Registry<T>> key) {
        return (Event<RegistryEntryAddedCallback<T>>) (Object) addObjectEvents.computeIfAbsent(key, key1 -> {
            var event = EventFactory.<RegistryEntryAddedCallback<T>>createArrayBacked(RegistryEntryAddedCallback.class,
            (callbacks) -> (rawId, id, object) -> {
                for (RegistryEntryAddedCallback<T> callback : callbacks) {
                    callback.onEntryAdded(rawId, id, object);
                }
            });

            BuiltInRegistries.REGISTRY.get(key1.identifier())
                .ifPresentOrElse(
                    (registry) -> setupRegEvent((Registry<T>) registry.value(), event),
                    () -> unhookedRegistries.add(key1)
                );

            return (Event) event;
        });
    }
}