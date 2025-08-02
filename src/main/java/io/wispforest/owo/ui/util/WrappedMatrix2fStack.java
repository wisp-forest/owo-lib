package io.wispforest.owo.ui.util;

import io.wispforest.owo.ui.core.PositionedRectangle;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class WrappedMatrix2fStack implements MatrixStack2fTransformer<WrappedMatrix2fStack> {

    private final Matrix3x2fStack matrixStack;

    @Nullable
    private PositionedRectangle scissor = null;

    public WrappedMatrix2fStack(Matrix3x2fStack matrixStack) {
        this.matrixStack = matrixStack;
    }

    public WrappedMatrix2fStack() {
        this(new Matrix3x2fStack());
    }

    public <T extends MatrixStack2fTransformer<T>> void drawWithTransforms(T t, Runnable drawCall) {
        t.push().applyStackTransformer(this);

        if(scissor != null) t.pushScissor(scissor);

        drawCall.run();

        if(scissor != null) t.pop();

        t.pop();
    }

    @Override
    public WrappedMatrix2fStack pushScissor(int x, int y, int width, int height) {
        this.scissor = PositionedRectangle.of(x, y, width, height);

        return this;
    }

    @Override
    public WrappedMatrix2fStack popScissor() {
        this.scissor = null;

        return this;
    }

    @Override
    public WrappedMatrix2fStack drawWithScissor(int x, int y, int width, int height, Consumer<WrappedMatrix2fStack> consumer) {
        throw new IllegalStateException("Unable to draw with scissor as such dose not work for WrappedMatrixStack!");
    }

    @Override
    public Matrix3x2fStack getMatrixStack() {
        return matrixStack;
    }

    public static WrappedMatrix2fStack parseStack(Element element, Map<String, Element> children) {
        UIParsing.expectChildren(element, children, "transforms");

        var transforms = UIParsing.<Element>allChildrenOfType(children.get("transforms"), Node.ELEMENT_NODE);

        return parseStack(transforms);
    }

    public static WrappedMatrix2fStack parseStack(List<Element> elements) {
        var stack = new WrappedMatrix2fStack();

        for (var transform : elements) {
            switch (transform.getNodeName()) {
                case "translate" -> stack.translate(UIParsing.parseVector2f(transform));
                case "scale" -> stack.scale(UIParsing.parseVector2f(transform));
                case "matrix" -> {
                    var list = Arrays.stream(transform.getTextContent().split(" "))
                            .map(Float::parseFloat)
                            .toList();

                    var array = new float[list.size()];

                    for (int i = 0; i < list.size(); i++) array[i] = list.get(i);

                    var matrix = new Matrix3x2f().set(array);

                    stack.mul(matrix);
                }
                case "scissor" -> {
                    var scissorChildren = UIParsing.childElements(transform);

                    UIParsing.expectChildren(transform, scissorChildren, "x", "y", "width", "height");

                    var x = UIParsing.parseSignedInt(scissorChildren.get("x"));
                    var y = UIParsing.parseSignedInt(scissorChildren.get("y"));
                    var width = UIParsing.parseSignedInt(scissorChildren.get("width"));
                    var height = UIParsing.parseSignedInt(scissorChildren.get("height"));

                    stack.pushScissor(x, y, width, height);
                }
                default -> throw new UIModelParsingException("Unknown transform type '" + transform.getNodeName() + "'");
            }
        }

        return stack;
    }
}
