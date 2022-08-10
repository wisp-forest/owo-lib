package io.wispforest.uwu.config;

import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RestartRequired;
import io.wispforest.owo.config.annotation.Sync;
import net.minecraft.util.Identifier;

@Modmenu(modId = "fabric")
@Config(name = "uowou", wrapperName = "BruhConfig")
@Sync(Option.SyncMode.OVERRIDE_CLIENT)
public class UowouConfigModel {

    @RestartRequired
    public boolean thisIsNotSyncable = false;

    public Identifier idPlease = new Identifier("uowou", "bruh");

}


