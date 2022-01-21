package io.wispforest.owo.data;

import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EarlyResourcePackHelper {
    private EarlyResourcePackHelper() {

    }

    private static final List<EarlyResourcePackDesc> EARLY_RESOURCE_PACKS = new ArrayList<>();

    public static void register(Path basePath, String name) {
        EARLY_RESOURCE_PACKS.add(new EarlyResourcePackDesc(basePath, name));
    }

    @ApiStatus.Internal
    public static void registerAll(Consumer<ResourcePack> addPack, ResourceType type) {
        EARLY_RESOURCE_PACKS.forEach(desc -> addPack.accept(new EarlyResourcePack(desc.basePath(), desc.name(), type)));
    }
}
