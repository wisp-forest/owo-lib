package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.OwoUIRenderLayers;
import io.wispforest.owo.ui.core.PositionedRectangle;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.WrappedMatrixStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Map;
import java.util.function.Consumer;

public abstract class IncrementButton extends ButtonComponent {

    protected ValueRenderer<IncrementButton> renderer = ValueRenderer.EMPTY;

    protected Text baseMessage;
    protected int minValue;
    protected int maxValue;

    private int currentValue;
    @Nullable
    private Integer previousValue = null;

    protected boolean wasRightClicked = false;

    protected IncrementButton(Text baseMessage, Consumer<ButtonComponent> onPress, int startValue, int minValue, int maxValue) {
        super(Text.empty(), onPress);

        this.baseMessage = baseMessage;

        this.setMessage(buildMessage());

        if (minValue   >= maxValue) throw new IllegalArgumentException("Unable to setup Increment button as such minValue [" + minValue + "] is bigger than maxValue [" + maxValue + "]!");
        if (startValue >= maxValue) throw new IllegalArgumentException("Unable to setup Increment button as such startValue [" + startValue + "] is bigger than maxValue [" + maxValue + "]!");
        if (startValue <  minValue) throw new IllegalArgumentException("Unable to setup Increment button as such startValue [" + startValue + "] is smaller than minValue [" + minValue + "]!");

        this.minValue = minValue;
        this.maxValue = maxValue;

        this.currentValue = startValue;
    }

    protected IncrementButton setValue(int value) {
        if (value >= maxValue) {
            throw new IllegalArgumentException("Unable to set Increment button as such the given value [" + value + "] is bigger than maxValue [" + maxValue + "]!");
        }

        this.currentValue = value;

        this.setMessage(buildMessage());

        return this;
    }

    protected int getValue() {
        return this.currentValue;
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        this.wasRightClicked = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
        return super.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_RIGHT || super.isValidClickButton(button);
    }

    @Override
    public void renderText(DrawContext context, int mouseX, int mouseY, float delta) {
        if (renderer == ValueRenderer.EMPTY) {
            super.renderText(context, mouseX, mouseY, delta);
        } else {
            this.renderer.draw((OwoUIDrawContext) context, this, delta);
        }
    }

    @Override
    protected ButtonComponent setText(Text message) {
        this.baseMessage = message;

        return this;
    }

    protected Text getValueText() {
        return Text.of(String.valueOf(currentValue));
    }

    protected Text buildMessage() {
        var valueText = getValueText();

        if (baseMessage.getContent().equals(PlainTextContent.EMPTY)) return valueText;

        return Text.literal("")
                .append(baseMessage)
                .append(": ")
                .append(valueText);
    }

    @Override
    public void onPress() {
        if (this.wasRightClicked || Screen.hasShiftDown()) {
            this.currentValue--;
            if (this.currentValue < 0) this.currentValue += this.maxValue;
        } else {
            this.currentValue++;
            if (this.currentValue >= this.maxValue) this.currentValue = this.minValue;
        }

        this.setMessage(buildMessage());

        super.onPress();
    }

    public void rollbackPress() {
        if (this.previousValue != null) {
            var currentValue = this.currentValue;

            this.currentValue = previousValue;

            this.previousValue = currentValue;

            this.setMessage(buildMessage());
        }
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "value-renderer", ValueRenderer::parse, renderer -> this.renderer = renderer);
    }

    public interface ValueRenderer<B extends IncrementButton> {
        ValueRenderer<IncrementButton> EMPTY = (context, button, delta) -> {};

        void draw(OwoUIDrawContext context, B button, float delta);

        static ValueRenderer<IncrementButton> parse(Element element) {
            var children = UIParsing.<Element>allChildrenOfType(element, Node.ELEMENT_NODE);

            Map<Integer, ValueRenderer<IncrementButton>> valueRenderers = new Int2ObjectOpenHashMap<>();

            for (Element rendererElement : children) {
                switch (rendererElement.getNodeName()) {
                    case "texture" -> {
                        UIParsing.expectAttributes(rendererElement, "value");

                        var textureDraw = TextureValueRenderer.parse(rendererElement);

                        var value = UIParsing.parseSignedInt(rendererElement.getAttributeNode("value"));

                        valueRenderers.put(value, textureDraw);
                    }
                    default -> throw new UIModelParsingException("Unknown button renderer '" + rendererElement.getNodeName() + "'");
                };
            }

            return (context, button, delta) -> {
                var renderer = valueRenderers.get(button.getValue());

                if (renderer != null) {
                    renderer.draw(context, button, delta);
                }
            };
        }

        record TextureValueRenderer(Identifier texture, int width, int height, int u, int v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, boolean blend, @Nullable WrappedMatrixStack stack) implements ValueRenderer<IncrementButton> {

            @Override
            public void draw(OwoUIDrawContext context, IncrementButton button, float delta) {
                Runnable drawCall = () -> {
                    context.drawTexture(identifier -> OwoUIRenderLayers.getGuiTextured(identifier, this.blend),
                            this.texture,
                            button.x(),
                            button.y(),
                            this.u,
                            this.v,
                            this.width,
                            this.height,
                            this.regionWidth,
                            this.regionHeight,
                            this.textureWidth,
                            this.textureHeight
                    );
                };

                if (stack != null) {
                    stack.drawWithTransforms(context, drawCall);
                } else {
                    drawCall.run();
                }
            }

            public static TextureValueRenderer parse(Element element) {
                UIParsing.expectAttributes(element, "texture", "width", "height");
                var textureId = UIParsing.parseIdentifier(element.getAttributeNode("texture"));

                int width = UIParsing.parseSignedInt(element.getAttributeNode("width"));
                int height = UIParsing.parseSignedInt(element.getAttributeNode("height"));

                int u = 0, v = 0, regionWidth = 0, regionHeight = 0, textureWidth = 256, textureHeight = 256;

                if (element.hasAttribute("u")) {
                    u = UIParsing.parseSignedInt(element.getAttributeNode("u"));
                }

                if (element.hasAttribute("v")) {
                    v = UIParsing.parseSignedInt(element.getAttributeNode("v"));
                }

                if (element.hasAttribute("region-width")) {
                    regionWidth = UIParsing.parseSignedInt(element.getAttributeNode("region-width"));
                } else {
                    regionWidth = width;
                }

                if (element.hasAttribute("region-height")) {
                    regionHeight = UIParsing.parseSignedInt(element.getAttributeNode("region-height"));
                } else {
                    regionHeight = height;
                }

                if (element.hasAttribute("texture-width")) {
                    textureWidth = UIParsing.parseSignedInt(element.getAttributeNode("texture-width"));
                }

                if (element.hasAttribute("texture-height")) {
                    textureHeight = UIParsing.parseSignedInt(element.getAttributeNode("texture-height"));
                }

                var blend = false;

                if (element.hasAttribute("blend")) {
                    blend = UIParsing.parseBool(element.getAttributeNode("blend"));
                }

                var children = UIParsing.childElements(element);

                var matrixStack = children.containsKey("transforms") ? WrappedMatrixStack.parseStack(element, children) : null;

                return new TextureValueRenderer(textureId, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, blend, matrixStack);
            }
        }
    }


}
