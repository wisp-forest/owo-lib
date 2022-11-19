package io.wispforest.uwu.config;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.*;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

@Modmenu(modId = "fabric")
@Config(name = "uowou", wrapperName = "BruhConfig")
@Sync(Option.SyncMode.OVERRIDE_CLIENT)
public class UowouConfigModel {

    @RestartRequired
    public boolean thisIsNotSyncable = false;

    @Hook
    public Identifier idPlease = new Identifier("uowou", "bruh");

    @Sync(Option.SyncMode.NONE)
    public Set<String> setPlease = Set.of("that's a value");

}


