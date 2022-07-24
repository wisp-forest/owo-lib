package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
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

// TODO enforce sizing in all component constructors that need it

/**
 * Utility methods for creating UI components
 */
public class Components {

    public static <E extends Entity> EntityComponent<E> entity(Sizing sizing, EntityType<E> type, @Nullable NbtCompound nbt) {
        return createWithSizing(() -> new EntityComponent<>(type, nbt), sizing, sizing);
    }

    public static <E extends Entity> EntityComponent<E> entity(Sizing sizing, E entity) {
        return createWithSizing(() -> new EntityComponent<>(entity), sizing, sizing);
    }

    public static ItemComponent item(ItemStack item) {
        return new ItemComponent(item);
    }

    public static ButtonWidget button(Text message, ButtonWidget.PressAction onPress) {
        final var button = new ButtonWidget(0, 0, 0, 0, message, onPress);
        button.sizing(Sizing.content(1), Sizing.content());
        return button;
    }

    public static ButtonWidget button(Text message, int width, int height, ButtonWidget.PressAction onPress) {
        return new ButtonWidget(0, 0, width, height, message, onPress);
    }

    public static LabelComponent label(Text text) {
        return new LabelComponent(text);
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

    public static SliderComponent slider(Sizing horizontalSizing) {
        return createWithSizing(SliderComponent::new, horizontalSizing, Sizing.fixed(20));
    }

    public static DiscreteSliderComponent discreteSlider(Sizing horizontalSizing, double min, double max) {
        var slider = new DiscreteSliderComponent(min, max);
        slider.horizontalSizing(horizontalSizing);
        return slider;
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

    public static DropdownComponent dropdown(Sizing horizontalSizing) {
        return new DropdownComponent(horizontalSizing);
    }

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
