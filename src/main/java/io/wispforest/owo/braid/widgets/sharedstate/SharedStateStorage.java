package io.wispforest.owo.braid.widgets.sharedstate;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import io.wispforest.endec.Endec;
import io.wispforest.endec.format.jankson.JanksonDeserializer;
import io.wispforest.endec.format.jankson.JanksonSerializer;
import io.wispforest.owo.Owo;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
//import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
//import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

//global state isn't real it can't hurt you
@SuppressWarnings("unchecked")
public class SharedStateStorage {

    private static final Jankson JANKSON = Jankson.builder().build();
    private static final HashedWheelTimer TIMER = new HashedWheelTimer();
    private static final Map<ShareableState, Entry<?>> ENTRIES = new HashMap<>();

    static {
        NeoForge.EVENT_BUS.<ClientStoppingEvent>addListener((_) -> ENTRIES.values().forEach(Entry::save));
    }

    public static <T extends ShareableState> T persist(Identifier id, Endec<T> endec, Supplier<T> defaults) {
        var path = configPath(id);

        T result;
        if (!Files.exists(path)) {
            result = defaults.get();
        } else {
            try {
                var json = JANKSON.load(Files.readString(path, StandardCharsets.UTF_8));
                result = endec.decodeFully(JanksonDeserializer::of, json);
            } catch (Exception e) {
                Owo.LOGGER.warn("Failed to load SharedState {}, using defaults", id, e);
                result = defaults.get();
            }
        }

        var entry = new Entry<>(id, (Endec<ShareableState>) endec, result);
        ENTRIES.put(result, entry);
        result.addListener(entry::scheduleSave);
        return result;
    }

    public static <T extends ShareableState> T persist(String namespace, String path, Endec<T> endec, Supplier<T> defaults) {
        return persist(Identifier.fromNamespaceAndPath(namespace, path), endec, defaults);
    }

    public static Path configPath(Identifier id) {
        return FMLPaths.CONFIGDIR.get()
            .resolve("braid")
            .resolve("shared_state")
            .resolve(id.getNamespace())
            .resolve(id.getPath() + ".json5");
    }

    private static class Entry<T extends ShareableState> {

        private final Identifier id;
        private final Endec<T> endec;
        private final T instance;
        private Timeout pendingSave;

        private Entry(Identifier id, Endec<T> endec, T instance) {
            this.id = id;
            this.endec = endec;
            this.instance = instance;
        }

        private void scheduleSave() {
            if (this.pendingSave != null) this.pendingSave.cancel();
            this.pendingSave = TIMER.newTimeout(_ -> save(), 10, TimeUnit.SECONDS);
        }

        private void save() {
            var path = configPath(this.id);
            try {
                Files.createDirectories(path.getParent());
                var json = this.endec.encodeFully(JanksonSerializer::of, this.instance);
                Files.writeString(path, json.toJson(JsonGrammar.JANKSON), StandardCharsets.UTF_8);
            } catch (IOException e) {
                Owo.LOGGER.warn("Failed to save SharedState {}", this.id, e);
            }
        }
    }
}
