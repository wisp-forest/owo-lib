package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

// TODO paginated and tabbed containers

/**
 * Utility methods for creating UI components
 */
public class Components {

    // -----------------------
    // Wrapped Vanilla Widgets
    // -----------------------

    /**
     * @deprecated Replaced by {@link io.wispforest.owo.ui.component.ButtonComponent.Renderer#texture(Identifier, int, int, int, int)}
     */
    public static TexturedButtonComponent texturedButton(Identifier texture, Text message, int width, int height, int u, int v, int textureWidth, int textureHeight, ButtonWidget.PressAction onPress) {
        return new TexturedButtonComponent(texture, width, height, u, v, textureWidth, textureHeight, message, onPress);
    }

    /**
     * @deprecated Replaced by {@link io.wispforest.owo.ui.component.ButtonComponent.Renderer#texture(Identifier, int, int, int, int)}
     */
    public static TexturedButtonComponent texturedButton(Identifier texture, Text message, int width, int height, int u, int v, ButtonWidget.PressAction onPress) {
        return new TexturedButtonComponent(texture, width, height, u, v, 256, 256, message, onPress);
    }

    /**
     * @deprecated Use {@link #button(Text, Consumer)} instead
     */
    @Deprecated(forRemoval = true)
    public static ButtonWidget button(Text message, int width, int height, ButtonWidget.PressAction onPress) {
        return createWithSizing(() -> new ButtonComponent(message, onPress::onPress), Sizing.fixed(width), Sizing.fixed(height));
    }

    /**
     * @deprecated Use {@link #button(Text, Consumer)} instead
     */
    @Deprecated(forRemoval = true)
    public static ButtonWidget button(Text message, ButtonWidget.PressAction onPress) {
        final var button = new ButtonComponent(message, onPress::onPress);
        button.sizing(Sizing.content(1), Sizing.content());
        return button;
    }

    public static ButtonComponent button(Text message, Consumer<ButtonComponent> onPress) {
        final var button = new ButtonComponent(message, onPress);
        button.sizing(Sizing.content(1), Sizing.content());
        return button;
    }

    public static TextFieldWidget textBox(Sizing horizontalSizing) {
        return createWithSizing(
                () -> new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 0, 0, Text.empty()),
                horizontalSizing,
                Sizing.fixed(20)
        );
    }

    public static TextFieldWidget textBox(Sizing horizontalSizing, String text) {
        final var textBox = textBox(horizontalSizing);
        textBox.setText(text);
        textBox.setCursorToStart();
        return textBox;
    }

    // ------------------
    // Default Components
    // ------------------

    public static <E extends Entity> EntityComponent<E> entity(Sizing sizing, EntityType<E> type, @Nullable NbtCompound nbt) {
        return new EntityComponent<>(sizing, type, nbt);
    }

    public static <E extends Entity> EntityComponent<E> entity(Sizing sizing, E entity) {
        return new EntityComponent<>(sizing, entity);
    }

    public static ItemComponent item(ItemStack item) {
        return new ItemComponent(item);
    }

    public static BlockComponent block(BlockState state) {
        return new BlockComponent(state, null);
    }

    public static BlockComponent block(BlockState state, BlockEntity blockEntity) {
        return new BlockComponent(state, blockEntity);
    }

    public static BlockComponent block(BlockState state, @Nullable NbtCompound nbt) {
        final var client = MinecraftClient.getInstance();

        BlockEntity blockEntity = null;

        if (state.getBlock() instanceof BlockEntityProvider provider) {
            blockEntity = provider.createBlockEntity(client.player.getBlockPos(), state);
            BlockComponent.prepareBlockEntity(state, blockEntity, nbt);
        }

        return new BlockComponent(state, blockEntity);
    }

    public static LabelComponent label(Text text) {
        return new LabelComponent(text);
    }

    public static CheckboxComponent checkbox(Text message) {
        return new CheckboxComponent(message);
    }

    public static SliderComponent slider(Sizing horizontalSizing) {
        return new SliderComponent(horizontalSizing);
    }

    public static DiscreteSliderComponent discreteSlider(Sizing horizontalSizing, double min, double max) {
        return new DiscreteSliderComponent(horizontalSizing, min, max);
    }

    public static SpriteComponent sprite(SpriteIdentifier spriteId) {
        return new SpriteComponent(spriteId.getSprite());
    }

    public static SpriteComponent sprite(Sprite sprite) {
        return new SpriteComponent(sprite);
    }

    public static TextureComponent texture(Identifier texture, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        return new TextureComponent(texture, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }

    public static TextureComponent texture(Identifier texture, int u, int v, int regionWidth, int regionHeight) {
        return new TextureComponent(texture, u, v, regionWidth, regionHeight, 256, 256);
    }

    public static BoxComponent box(Sizing horizontalSizing, Sizing verticalSizing) {
        return new BoxComponent(horizontalSizing, verticalSizing);
    }

    public static DropdownComponent dropdown(Sizing horizontalSizing) {
        return new DropdownComponent(horizontalSizing);
    }

    // -------
    // Utility
    // -------

    public static <T, C extends Component> FlowLayout list(List<T> data, Consumer<FlowLayout> layoutConfigurator, Function<T, C> componentMaker, boolean vertical) {
        var layout = vertical ? Containers.verticalFlow(Sizing.content(), Sizing.content()) : Containers.horizontalFlow(Sizing.content(), Sizing.content());
        layoutConfigurator.accept(layout);

        for (var value : data) {
            layout.child(componentMaker.apply(value));
        }

        return layout;
    }

    public static VanillaWidgetComponent wrapVanillaWidget(ClickableWidget widget) {
        return new VanillaWidgetComponent(widget);
    }

    public static <T extends Component> T createWithSizing(Supplier<T> componentMaker, Sizing horizontalSizing, Sizing verticalSizing) {
        var component = componentMaker.get();
        component.sizing(horizontalSizing, verticalSizing);
        return component;
    }

}
