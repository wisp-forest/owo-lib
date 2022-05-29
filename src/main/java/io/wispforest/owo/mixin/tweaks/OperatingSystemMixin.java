package io.wispforest.owo.mixin.tweaks;

import com.mojang.logging.LogUtils;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

@Mixin(value = Util.OperatingSystem.class)
public abstract class OperatingSystemMixin {

    @Shadow
    protected abstract String[] getURLOpenCommand(URL url);

    /**
     * @author glisco
     * @reason By not properly consuming the stdout stream of the started process,
     * Minecraft's implementation of this method causes xdg-open on linux to fail at actually
     * opening the target program about 80% of the time. This overwrite uses a more modern approach
     * to starting processes and properly voids both stdout and stderr, making xdg-open succeed
     * at opening the user's desired application 100% of the time
     */
    @Overwrite()
    public void open(URL url) {
        CompletableFuture.runAsync(() -> {
            try {
                final var command = getURLOpenCommand(url);
                new ProcessBuilder(command)
                        .redirectError(ProcessBuilder.Redirect.DISCARD)
                        .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                        .start();
            } catch (IOException e) {
                LogUtils.getLogger().error("Couldn't open url '{}'", url, e);
            }
        }, Util.getMainWorkerExecutor());
    }
}
