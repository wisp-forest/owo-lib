package io.wispforest.owo.ui;

import io.wispforest.owo.ui.definitions.ParentComponent;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelLoader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public abstract class BaseUIModelScreen<R extends ParentComponent> extends BaseOwoScreen<R> {

    protected final Class<R> rootComponentClass;
    protected final UIModel model;

    protected BaseUIModelScreen(Class<R> rootComponentClass, DataSource source) {
        var providedModel = source.get();
        if (providedModel == null) {
            source.reportError();
            this.invalid = true;
        }

        this.rootComponentClass = rootComponentClass;
        this.model = providedModel;
    }

    @Override
    protected @NotNull OwoUIAdapter<R> createAdapter() {
        return this.model.createAdapter(rootComponentClass, this);
    }

    protected interface DataSource {

        @Nullable
        UIModel get();

        void reportError();

        static DataSource debug(String filePath) {
            return new DataSource() {
                @Override
                @Nullable
                public UIModel get() {
                    return UIModel.load(Path.of(filePath));
                }

                @Override
                public void reportError() {
                    UIErrorToast.report("Could not load UI model from file " + filePath);
                }
            };
        }

        static DataSource release(Identifier assetPath) {
            return new DataSource() {
                @Override
                public @Nullable UIModel get() {
                    return UIModelLoader.getPreloaded(assetPath);
                }

                @Override
                public void reportError() {
                    UIErrorToast.report("No UI model with id " + assetPath + " was found");
                }
            };
        }

    }
}
