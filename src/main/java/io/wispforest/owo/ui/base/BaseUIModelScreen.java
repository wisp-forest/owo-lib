package io.wispforest.owo.ui.base;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.parsing.ConfigureHotReloadScreen;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelLoader;
import io.wispforest.owo.ui.util.UIErrorToast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import net.minecraft.resources.Identifier;

/**
 * A simple base implementation of a screen that builds its UI
 * upon the base of a UI model parsed from an XML file. To work with this system,
 * declare your UI structure in an XML file and pass it into the super constructor
 * call using the relevant {@link DataSource}.
 * <p>
 * You can then query and set up different components of your UI hierarchy using
 * {@link ParentComponent#childById(Class, String)} in the {@link #build(ParentComponent)} method
 *
 * @param <R> The type of root component this screen expects from the UI model
 */
public abstract class BaseUIModelScreen<R extends ParentComponent> extends BaseOwoScreen<R> {

    /**
     * The UI model this screen is built upon, parsed from XML.
     * This is usually not relevant to subclasses, the UI adapter
     * inherited from {@link BaseOwoScreen} is more interesting
     */
    protected final UIModel model;
    protected final Class<R> rootComponentClass;

    protected final @Nullable Identifier modelId;

    protected BaseUIModelScreen(Class<R> rootComponentClass, DataSource source) {
        var providedModel = source.get();
        if (providedModel == null) {
            source.reportError();
            this.invalid = true;
        }

        this.rootComponentClass = rootComponentClass;
        this.model = providedModel;

        this.modelId = source instanceof DataSource.AssetDataSource assetSource
                ? assetSource.assetPath()
                : null;
    }

    protected BaseUIModelScreen(Class<R> rootComponentClass, Identifier modelId) {
        this(rootComponentClass, DataSource.asset(modelId));
    }

    @Override
    protected @NotNull OwoUIAdapter<R> createAdapter() {
        return this.model.createAdapter(this.rootComponentClass, this);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Owo.DEBUG && this.modelId != null && keyCode == GLFW.GLFW_KEY_F5 && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            this.client.setScreen(new ConfigureHotReloadScreen(this.modelId, this));
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * A source of UI model data, by default can be loaded
     * from a file or resourcepack. If you need a different way of
     * fetching the model - implement this interface and pass it
     * to the {@code super(...)} call in your constructor
     */
    public interface DataSource {

        @Nullable
        UIModel get();

        void reportError();

        /**
         * Dynamically load the UI model by parsing the XML file
         * at the given file path relative to the game's run directory.
         * <p>
         * This source is useful for development, as changes to the file
         * instantly show up in-game without needing to reload resource packs
         * <p>
         * This source throws when running in release mode,
         * because only files inside the jar can be shipped
         *
         * @param filePath The path of the XML file to load
         * @deprecated Using the file data source directly is strongly discouraged
         * as it primarily causes issues when accidentally used in production. Instead, use
         * the asset source and configure the hot reload location for your model
         */
        @Deprecated
        static DataSource file(String filePath) {
            return new DataSource() {
                @Override
                @Nullable
                public UIModel get() {
                    if (!Owo.DEBUG) {
                        throw new IllegalStateException("Debug UI data source must not be used in production");
                    }
                    return UIModel.load(Path.of(filePath));
                }

                @Override
                public void reportError() {
                    UIErrorToast.report("Could not load UI model from file " + filePath);
                }
            };
        }

        /**
         * Get a statically loaded and parsed UI model from the currently
         * loaded resource packs. This source is preferred as it has significantly
         * higher performance due to completely avoiding I/O operations and the
         * model XML can be overridden by different resource packs
         *
         * @param assetPath The path of the asset that was parsed into
         *                  a UI model, relative to {@code assets/<namespace>/owo_ui}
         */
        static DataSource asset(Identifier assetPath) {
            return new AssetDataSource(assetPath);
        }

        record AssetDataSource(Identifier assetPath) implements DataSource {
            @Override
            public @Nullable UIModel get() {
                return UIModelLoader.get(assetPath);
            }

            @Override
            public void reportError() {
                UIErrorToast.report("No UI model with id " + assetPath + " was found");
            }
        }
    }
}
