package io.wispforest.owo.data;

import net.minecraft.resource.ResourcePack;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EarlyResourcePackHelper {
    private EarlyResourcePackHelper() {

    }

    private static final List<EarlyResourcePack> EARLY_RESOURCE_PACKS = new ArrayList<>();

    public static void register(Path basePath, String name) {
        EARLY_RESOURCE_PACKS.add(new EarlyResourcePack(basePath, name));
    }

    @ApiStatus.Internal
    public static void registerAll(Consumer<ResourcePack> addPack) {
        EARLY_RESOURCE_PACKS.forEach(addPack);
    }
}
