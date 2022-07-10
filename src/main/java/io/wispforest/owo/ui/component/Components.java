package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.Layouts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Components {

    private static final Supplier<TextFieldWidget> EMPTY_TEXT_FIELD = () -> {
        return new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 0, 0, Text.empty());
    };

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
        return createSized(
                EMPTY_TEXT_FIELD,
                horizontalSizing,
                Sizing.fixed(20)
        );
    }

    public static TextFieldWidget textBox(Sizing horizontalSizing, String text) {
        final var textBox = createSized(
                EMPTY_TEXT_FIELD,
                horizontalSizing,
                Sizing.fixed(20)
        );
        textBox.setText(text);
        textBox.setCursorToStart();
        return textBox;
    }

    public static SliderComponent slider(Sizing horizontalSizing, Text message) {
        final var slider = createSized(SliderComponent::new, horizontalSizing, Sizing.fixed(20));
        slider.setMessage(message);
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

    public static <T extends Component> T createSized(Supplier<T> componentMaker, Sizing horizontalSizing, Sizing verticalSizing) {
        var component = componentMaker.get();
        component.sizing(horizontalSizing, verticalSizing);
        return component;
    }

    public static <T, C extends Component> FlowLayout list(List<T> data, Consumer<FlowLayout> layoutConfigurator, Function<T, C> componentMaker, boolean vertical) {
        var layout = vertical ? Layouts.verticalFlow(Sizing.content(), Sizing.content()) : Layouts.horizontalFlow(Sizing.content(), Sizing.content());
        layoutConfigurator.accept(layout);

        for (var value : data) {
            layout.child(componentMaker.apply(value));
        }

        return layout;
    }

}
