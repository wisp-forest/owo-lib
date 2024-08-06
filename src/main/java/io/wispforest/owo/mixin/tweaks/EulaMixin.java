package io.wispforest.owo.mixin.tweaks;

import net.minecraft.server.Eula;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Scanner;

@Mixin(Eula.class)
public abstract class EulaMixin {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private Path file;

    @Shadow public abstract boolean hasAgreedToEULA();

    @Inject(method = "readFile", at = @At(value = "TAIL"), cancellable = true)
    private void overrideEulaAgreement(CallbackInfoReturnable<Boolean> cir) {
        if (this.hasAgreedToEULA()) return;

        var scanner = new Scanner(System.in);
        LOGGER.info("By answering 'true' to this prompt you are indicating your agreement to Minecraft's EULA (https://account.mojang.com/documents/minecraft_eula)\nEULA:");

        var input = scanner.next();
        if (!input.equalsIgnoreCase("true")) return;

        try (var inStream = Files.newInputStream(this.file); var outStream = Files.newOutputStream(this.file)) {
            var properties = new Properties();
            properties.load(inStream);
            properties.setProperty("eula", "true");
            properties.store(outStream, "By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).");
        } catch (IOException e) {
            LOGGER.info("Could not accept eula", e);
        }

        LOGGER.info("EULA accepted");
        cir.setReturnValue(true);
    }
}
