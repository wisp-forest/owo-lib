package io.wispforest.uwu.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Map;

public class LoggingResourceLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    public LoggingResourceLoader() {
        super(GSON, "resource_tests");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        for (var entry : prepared.entrySet()) {
            System.out.println("Loaded " + entry.getKey() + ": " + entry.getValue());
        }
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier("uwu", "logging_resource_loader");
    }
}
