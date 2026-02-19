package io.wispforest.owo.braid.util.kdl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationAttribute;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class BraidKdlEndecs {
    private BraidKdlEndecs() {}

    public static final SerializationAttribute.WithValue<Map<String, Consumer<@Nullable Object>>> HANDLERS = SerializationAttribute.withValue("braid_handlers");

    public static final Endec<Alignment> ALIGNMENT = Endec.STRING.xmap(
        s -> switch (s) {
            case "top_left" -> Alignment.TOP_LEFT;
            case "top" -> Alignment.TOP;
            case "top_right" -> Alignment.TOP_RIGHT;
            case "left" -> Alignment.LEFT;
            case "center" -> Alignment.CENTER;
            case "right" -> Alignment.RIGHT;
            case "bottom_left" -> Alignment.BOTTOM_LEFT;
            case "bottom" -> Alignment.BOTTOM;
            case "bottom_right" -> Alignment.BOTTOM_RIGHT;
            default -> throw new IllegalStateException("invalid alignment type: " + s);
        },
        alignment -> {
            throw new UnsupportedOperationException("cannot serialize arbitrary alignment into a string");
        }
    );

    public static final Endec<Color> COLOR = Endec.INT.xmap(Color::new, Color::argb);

    public static final Endec<LayoutAxis> LAYOUT_AXIS = Endec.STRING.xmap(
        s -> switch (s) {
            case "column" -> LayoutAxis.VERTICAL;
            case "row" -> LayoutAxis.HORIZONTAL;
            default -> throw new IllegalStateException("invalid layout axis: " + s);
        },
        layoutAxis -> switch (layoutAxis) {
            case VERTICAL -> "column";
            case HORIZONTAL -> "row";
        }
    );
    public static final Endec<CrossAxisAlignment> CROSS_AXIS_ALIGNMENT = Endec.forEnum(CrossAxisAlignment.class, false);
    public static final Endec<MainAxisAlignment> MAIN_AXIS_ALIGNMENT = Endec.forEnum(MainAxisAlignment.class, false);

    public static final Endec<Vector2f> VECTOR2F = Endec.FLOAT.listOf().validate(floats -> {
        if (floats.size() != 2) {
            throw new IllegalStateException("Vector2f array must have two elements");
        }
    }).xmap(
        components -> new Vector2f(components.get(0), components.get(1)),
        vector -> List.of(vector.x, vector.y)
    );

    private sealed interface TransformStep {
        record Translate(Vector2f translation) implements TransformStep {
            public static final StructEndec<Translate> ENDEC = StructEndecBuilder.of(
                VECTOR2F.fieldOf("@arguments", Translate::translation),
                Translate::new
            );
        }

        record Scale(Vector2f scaling) implements TransformStep {
            public static final StructEndec<Scale> ENDEC = StructEndecBuilder.of(
                VECTOR2F.fieldOf("@arguments", Scale::scaling),
                Scale::new
            );
        }

        record Rotate(float angle) implements TransformStep {
            public static final StructEndec<Rotate> ENDEC = StructEndecBuilder.of(
                Endec.FLOAT.fieldOf("@argument", Rotate::angle),
                Rotate::new
            );
        }

        StructEndec<TransformStep> ENDEC = Endec.dispatchedStruct(
            variant -> switch (variant) {
                case "translate" -> Translate.ENDEC;
                case "scale" -> Scale.ENDEC;
                case "rotate" -> Rotate.ENDEC;
                default -> throw new IllegalStateException("invalid transform step: " + variant);
            },
            step -> switch (step) {
                case Translate ignored -> "translate";
                case Scale ignored -> "scale";
                case Rotate ignored -> "rotate";
            },
            Endec.STRING,
            "@name"
        );
    }

    public static final StructEndec<Matrix3x2f> TRANSFORM_MATRIX_2D = StructEndecBuilder.of(
        TransformStep.ENDEC.listOf().fieldOf("@children", s -> {throw new UnsupportedOperationException("cannot serialize a matrix into transform steps");}),
        transformSteps -> {
            var result = new Matrix3x2f();
            for (var step : transformSteps) {
                switch (step) {
                    case TransformStep.Translate(var translation) -> result.translate(translation);
                    case TransformStep.Scale(var scaling) -> result.scale(scaling);
                    case TransformStep.Rotate(var angle) -> result.rotate((float) Math.toRadians(angle));
                }
            }
            return result;
        }
    );

    public static final Endec<ItemStack> ITEM_STACK_STRING = Endec.STRING.xmap(
        s -> {
            try {
                var result = new ItemParser(Minecraft.getInstance().level.registryAccess()).parse(new StringReader(s));
                var stack = result.item().value().getDefaultInstance();
                stack.applyComponents(result.components());

                return stack;
            } catch (CommandSyntaxException e) {
                throw new IllegalStateException("invalid item stack: " + s, e);
            }
        },
        stack -> { throw new UnsupportedOperationException("cannot serialize an item stack to a string"); }
    );

    public static final Endec<BlockStateParser.BlockResult> BLOCK_STRING = Endec.STRING.xmap(
        s -> {
            try {
                return BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, s, true);
            } catch (CommandSyntaxException e) {
                throw new IllegalStateException("invalid block state: " + s, e);
            }
        },
        blockState -> { throw new UnsupportedOperationException("cannot serialize a block state to a string"); }
    );
}
