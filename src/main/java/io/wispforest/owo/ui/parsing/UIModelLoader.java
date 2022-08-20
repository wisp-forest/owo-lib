package io.wispforest.owo.ui.parsing;

import io.wispforest.owo.Owo;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;

public class UIModelLoader implements SynchronousResourceReloader, IdentifiableResourceReloadListener {

    private static final HashMap<Identifier, UIModel> LOADED_MODELS = new HashMap<>();

    public static @Nullable UIModel getPreloaded(Identifier id) {
        return LOADED_MODELS.getOrDefault(id, null);
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier("owo", "ui-model-loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        LOADED_MODELS.clear();

        manager.findResources("owo_ui", identifier -> identifier.getPath().endsWith(".xml")).forEach((resourceId, resource) -> {
            try {
                var modelId = new Identifier(
                        resourceId.getNamespace(),
                        resourceId.getPath().substring(7, resourceId.getPath().length() - 4)
                );

                LOADED_MODELS.put(modelId, UIModel.load(resource.getInputStream()));
            } catch (ParserConfigurationException | IOException | SAXException e) {
                Owo.LOGGER.error("Could not parse UI model {}", resourceId, e);
            }
        });
    }
}
