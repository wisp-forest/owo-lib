package io.wispforest.owo.ui.parsing;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.util.CommandOpenedScreen;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ConfigureHotReloadScreen extends BaseUIModelScreen<FlowLayout> implements CommandOpenedScreen {

    private final @Nullable Screen parent;

    private final Identifier modelId;
    private @Nullable Path reloadLocation;

    private LabelComponent fileNameLabel;

    public ConfigureHotReloadScreen(Identifier modelId, @Nullable Screen parent) {
        super(FlowLayout.class, DataSource.asset(Identifier.of("owo", "configure_hot_reload")));
        this.parent = parent;

        this.modelId = modelId;
        this.reloadLocation = UIModelLoader.getHotReloadPath(this.modelId);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(LabelComponent.class, "ui-model-label").text(Text.translatable("text.owo.configure_hot_reload.model", this.modelId));
        this.fileNameLabel = rootComponent.childById(LabelComponent.class, "file-name-label");
        this.updateFileNameLabel();

        rootComponent.childById(ButtonComponent.class, "choose-button").onPress(button -> {
            CompletableFuture.runAsync(() -> {
                var newPath = TinyFileDialogs.tinyfd_openFileDialog("Choose UI Model source", null, null, null, false);
                if (newPath != null) this.reloadLocation = Path.of(newPath);
            }, Util.getMainWorkerExecutor()).whenComplete((unused, throwable) -> {
                this.updateFileNameLabel();
            });
        });

        rootComponent.childById(ButtonComponent.class, "save-button").onPress(button -> {
            UIModelLoader.setHotReloadPath(this.modelId, this.reloadLocation);
            this.close();
        });

        rootComponent.childById(LabelComponent.class, "close-label").mouseDown().subscribe((mouseX, mouseY, button) -> {
            UISounds.playInteractionSound();
            this.close();
            return true;
        });
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private void updateFileNameLabel() {
        this.fileNameLabel.text(Text.translatable(
                "text.owo.configure_hot_reload.reload_from",
                this.reloadLocation == null ? Text.translatable("text.owo.configure_hot_reload.reload_from.unset") : this.reloadLocation
        ));
    }
}
