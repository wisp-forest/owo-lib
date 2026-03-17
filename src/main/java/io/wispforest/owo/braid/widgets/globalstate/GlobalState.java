package io.wispforest.owo.braid.widgets.globalstate;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonGrammar;
import io.wispforest.endec.Endec;
import io.wispforest.endec.format.jankson.JanksonDeserializer;
import io.wispforest.endec.format.jankson.JanksonSerializer;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.Listenable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public abstract class GlobalState extends Listenable {

    private static final Jankson JANKSON = Jankson.builder().build();
    private static final Set<GlobalState> LOADED_STATES = new HashSet<>();
    private static final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(1);

    static {
        ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> LOADED_STATES.forEach(GlobalState::save));
    }

    private ScheduledFuture<?> pendingSave;

    protected abstract Identifier id();

    protected abstract Endec<? extends GlobalState> endec();

    public static <T extends GlobalState> T load(T defaults) {
        LOADED_STATES.add(defaults);
        var path = defaults.configPath();
        if (!Files.exists(path)) return defaults;

        try {
            var json = JANKSON.load(Files.readString(path, StandardCharsets.UTF_8));
            return ((Endec<T>) defaults.endec()).decodeFully(JanksonDeserializer::of, json);
        } catch (Exception e) {
            Owo.LOGGER.warn("Failed to load GlobalState {}, using defaults", defaults.id(), e);
            return defaults;
        }
    }

    public final void setState(Runnable fn) {
        fn.run();
        this.notifyListeners();
        this.scheduleSave();
    }

    private void scheduleSave() {
        if (this.pendingSave != null) this.pendingSave.cancel(false);
        this.pendingSave = SCHEDULER.schedule(this::save, 10, TimeUnit.SECONDS);
    }

    private void save() {
        var path = configPath();
        try {
            Files.createDirectories(path.getParent());
            var json = ((Endec<GlobalState>) endec()).encodeFully(JanksonSerializer::of, this);
            Files.writeString(path, json.toJson(JsonGrammar.JANKSON), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Owo.LOGGER.warn("Failed to save GlobalState {}", id(), e);
        }
    }

    public Path configPath() {
        var id = id();
        return FabricLoader.getInstance().getConfigDir()
            .resolve("braid")
            .resolve(id.getNamespace())
            .resolve(id.getPath() + ".json5");
    }
}
