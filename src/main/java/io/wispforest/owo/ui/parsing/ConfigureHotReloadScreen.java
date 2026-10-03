package io.wispforest.owo.ui.parsing;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.util.CommandOpenedScreen;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDL_DialogFileCallback;
import org.lwjgl.system.MemoryUtil;

import java.nio.file.Path;

public class ConfigureHotReloadScreen extends BaseUIModelScreen<FlowLayout> implements CommandOpenedScreen {

    private final @Nullable Screen parent;

    private final Identifier modelId;
    private @Nullable Path reloadLocation;

    private LabelComponent fileNameLabel;
    private @Nullable SDL_DialogFileCallback fileDialogCallback;

    public ConfigureHotReloadScreen(Identifier modelId, @Nullable Screen parent) {
        super(FlowLayout.class, DataSource.asset(Owo.id("configure_hot_reload")));
        this.parent = parent;

        this.modelId = modelId;
        this.reloadLocation = UIModelLoader.getHotReloadPath(this.modelId);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(LabelComponent.class, "ui-model-label").text(Component.translatable("text.owo.configure_hot_reload.model", this.modelId));
        this.fileNameLabel = rootComponent.childById(LabelComponent.class, "file-name-label");
        this.updateFileNameLabel();

        rootComponent.childById(ButtonComponent.class, "choose-button").onPress(button -> this.openFileDialog());

        rootComponent.childById(ButtonComponent.class, "save-button").onPress(button -> {
            UIModelLoader.setHotReloadPath(this.modelId, this.reloadLocation);
            this.onClose();
        });

        rootComponent.childById(LabelComponent.class, "close-label").mouseDown().subscribe((click, doubled) -> {
            UISounds.playInteractionSound();
            this.onClose();
            return true;
        });
    }

    private void openFileDialog() {
        if (this.fileDialogCallback != null) return;

        this.fileDialogCallback = SDL_DialogFileCallback.create((userdata, filelist, filter) -> {
            var chosenPath = firstPath(filelist);

            this.minecraft.execute(() -> {
                if (chosenPath != null) this.reloadLocation = Path.of(chosenPath);
                this.updateFileNameLabel();

                if (this.fileDialogCallback != null) {
                    this.fileDialogCallback.free();
                    this.fileDialogCallback = null;
                }
            });
        });

        SDLDialog.SDL_ShowOpenFileDialog(this.fileDialogCallback, 0, this.minecraft.getWindow().handle(), null, (CharSequence) null, false);
    }

    private static @Nullable String firstPath(long filelist) {
        if (filelist == 0) return null;

        long first = MemoryUtil.memGetAddress(filelist);
        if (first == 0) return null;

        return MemoryUtil.memUTF8(first);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    private void updateFileNameLabel() {
        this.fileNameLabel.text(Component.translatable(
                "text.owo.configure_hot_reload.reload_from",
                this.reloadLocation == null ? Component.translatable("text.owo.configure_hot_reload.reload_from.unset") : this.reloadLocation
        ));
    }
}