package io.wispforest.owo.ui;

import io.wispforest.owo.ui.definitions.ParentComponent;
import io.wispforest.owo.ui.parsing.OwoUISpec;
import io.wispforest.owo.ui.parsing.OwoUISpecLoader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public abstract class BaseUISpecScreen<R extends ParentComponent> extends BaseOwoScreen<R> {

    protected final Class<R> rootComponentClass;
    protected final OwoUISpec spec;

    protected BaseUISpecScreen(Class<R> rootComponentClass, DataSource source) {
        var providedSpec = source.get();
        if (providedSpec == null) {
            source.reportError();
            this.invalid = true;
        }

        this.rootComponentClass = rootComponentClass;
        this.spec = providedSpec;
    }

    @Override
    protected @NotNull OwoUIAdapter<R> createAdapter() {
        return this.spec.createAdapter(rootComponentClass, this);
    }

    protected interface DataSource {

        @Nullable
        OwoUISpec get();

        void reportError();

        static DataSource debug(String filePath) {
            return new DataSource() {
                @Override
                @Nullable
                public OwoUISpec get() {
                    return OwoUISpec.load(Path.of(filePath));
                }

                @Override
                public void reportError() {
                    UIErrorToast.report("Could not load UI spec from file " + filePath);
                }
            };
        }

        static DataSource release(Identifier assetPath) {
            return new DataSource() {
                @Override
                public @Nullable OwoUISpec get() {
                    return OwoUISpecLoader.getPreloaded(assetPath);
                }

                @Override
                public void reportError() {
                    UIErrorToast.report("No UI spec with id " + assetPath + " was found");
                }
            };
        }

    }
}
