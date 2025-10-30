package io.wispforest.owo.ui.util;

import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class WrappedMatrixStack implements MatrixStackTransformer<WrappedMatrixStack> {

    private final MatrixStack matrixStack;

    public WrappedMatrixStack(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;
    }

    public WrappedMatrixStack() {
        this(new MatrixStack());
    }

    public <T extends MatrixStackTransformer<T>> void drawWithTransforms(T t, Runnable drawCall) {
        t.push().applyStackTransformer(this);

        drawCall.run();

        t.pop();
    }

    @Override
    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public static WrappedMatrixStack parseStack(Element element, Map<String, Element> children) {
        UIParsing.expectChildren(element, children, "transforms");

        var transforms = UIParsing.<Element>allChildrenOfType(children.get("transforms"), Node.ELEMENT_NODE);

        return parseStack(transforms);
    }

    public static WrappedMatrixStack parseStack(List<Element> elements) {
        var stack = new WrappedMatrixStack();

        for (var transform : elements) {
            switch (transform.getNodeName()) {
                case "translate" -> stack.translate(UIParsing.parseVector3f(transform));
                case "scale" -> stack.scale(UIParsing.parseVector3f(transform));
                case "multiply" -> {
                    var quaternion = UIParsing.parseQuaternionf(transform);
                    var origin = UIParsing.get(UIParsing.childElements(transform), "origin", UIParsing::parseVector3f);

                    if (origin.isEmpty()) {
                        stack.multiply(quaternion);
                    } else {
                        stack.multiply(quaternion, origin.get());
                    }
                }
                case "matrix" -> {
                    var list = Arrays.stream(transform.getTextContent().split(" "))
                            .map(Float::parseFloat)
                            .toList();

                    var array = new float[list.size()];

                    for (int i = 0; i < list.size(); i++) array[i] = list.get(i);

                    var matrix = new Matrix4f().set(array);

                    stack.multiplyPositionMatrix(matrix);
                }
                default -> throw new UIModelParsingException("Unknown transform type '" + transform.getNodeName() + "'");
            }
        }

        return stack;
    }
}
