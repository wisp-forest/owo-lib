package io.wispforest.owosentinel;

import cpw.mods.modlauncher.api.NamedPath;
import cpw.mods.modlauncher.serviceapi.ITransformerDiscoveryService;

import java.nio.file.Path;
import java.util.List;

public class Maldenhagen implements ITransformerDiscoveryService {
    @Override
    public List<NamedPath> candidates(Path gameDirectory) {
        return List.of();
    }

    @Override
    public void earlyInitialization(String launchTarget, String[] arguments) {
        //OwoSentinel.launch();
    }
}
