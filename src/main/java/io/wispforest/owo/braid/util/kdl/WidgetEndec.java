package io.wispforest.owo.braid.util.kdl;

import io.wispforest.endec.Endec;
import io.wispforest.endec.SelfDescribedDeserializer;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.format.java.JavaSerializer;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.button.Button;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Flex;
import io.wispforest.owo.braid.widgets.flex.Flexible;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.grid.Grid;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.object.BlockWidget;
import io.wispforest.owo.braid.widgets.object.EntityWidget;
import io.wispforest.owo.braid.widgets.object.ItemStackWidget;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.braid.widgets.stack.StackBase;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.client.resources.model.Material;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WidgetEndec {

    private static final Map<String, StructEndec<? extends Widget>> REGISTRY = new HashMap<>();
    private static final Map<Class<? extends Widget>, String> WIDGET_TYPE_NAMES = new HashMap<>();

    public static final Endec<Widget> ROOT = Endec.dispatchedStruct(
        variant -> {
            var endec = REGISTRY.get(variant);
            if (endec == null) {
                throw new IllegalStateException("Unknown widget type: " + variant);
            }

            return endec;
        },
        widget -> WIDGET_TYPE_NAMES.get(widget.getClass()),
        Endec.STRING,
        "@name"
    );


    public static <W extends Widget> void register(Identifier key, Class<W> widgetClass, StructEndec<W> endec) {
        register(key.toLanguageKey(), widgetClass, endec);
    }

    @ApiStatus.Internal
    public static <W extends Widget> void register(String key, Class<W> widgetClass, StructEndec<W> endec) {
        if (REGISTRY.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate widget endec key: " + key);
        }

        REGISTRY.put(key, endec);
        WIDGET_TYPE_NAMES.put(widgetClass, key);
    }

    // ---

    static {
        register(
            "align",
            Align.class,
            StructEndecBuilder.of(
                BraidKdlEndecs.ALIGNMENT.fieldOf("@argument", s -> s.alignment),
                Endec.DOUBLE.nullableOf().optionalFieldOf("width_factor", s -> s.widthFactor.isPresent() ? s.widthFactor.getAsDouble() : null, (Double) null),
                Endec.DOUBLE.nullableOf().optionalFieldOf("height_factor", s -> s.heightFactor.isPresent() ? s.heightFactor.getAsDouble() : null, (Double) null),
                ROOT.fieldOf("@child", s -> s.child),
                Align::new
            )
        );

        register(
            "center",
            Center.class,
            StructEndecBuilder.of(
                Endec.DOUBLE.nullableOf().optionalFieldOf("width_factor", s -> s.widthFactor.isPresent() ? s.widthFactor.getAsDouble() : null, (Double) null),
                Endec.DOUBLE.nullableOf().optionalFieldOf("height_factor", s -> s.heightFactor.isPresent() ? s.heightFactor.getAsDouble() : null, (Double) null),
                ROOT.fieldOf("@child", s -> s.child),
                Center::new
            )
        );

        register(
            "padding",
            Padding.class,
            StructEndecBuilder.of(
                Endec.DOUBLE.optionalOf().optionalFieldOf("@argument", s -> Optional.<Double>empty(), Optional::empty),
                Endec.DOUBLE.optionalOf().optionalFieldOf("horizontal", s -> Optional.<Double>empty(), Optional::empty),
                Endec.DOUBLE.optionalOf().optionalFieldOf("vertical", s -> Optional.<Double>empty(), Optional::empty),
                Endec.DOUBLE.optionalOf().optionalFieldOf("top", s -> Optional.of(s.insets.top()), Optional::empty),
                Endec.DOUBLE.optionalOf().optionalFieldOf("bottom", s -> Optional.of(s.insets.bottom()), Optional::empty),
                Endec.DOUBLE.optionalOf().optionalFieldOf("left", s -> Optional.of(s.insets.left()), Optional::empty),
                Endec.DOUBLE.optionalOf().optionalFieldOf("right", s -> Optional.of(s.insets.right()), Optional::empty),
                ROOT.optionalOf().optionalFieldOf("@child", s -> Optional.ofNullable(s.child), Optional.empty()),
                (all, horizontal, vertical, top, bottom, left, right, child) -> {
                    var dTop = top.orElse(vertical.orElse(all.orElse(0.0)));
                    var dBottom = bottom.orElse(vertical.orElse(all.orElse(0.0)));
                    var dLeft = left.orElse(horizontal.orElse(all.orElse(0.0)));
                    var dRight = right.orElse(horizontal.orElse(all.orElse(0.0)));

                    return new Padding(
                        Insets.of(dTop, dBottom, dLeft, dRight),
                        child.orElse(null)
                    );
                }
            )
        );

        register(
            "sized",
            Sized.class,
            StructEndecBuilder.of(
                Endec.DOUBLE.nullableOf().optionalFieldOf("width", s -> s.width, (Double) null),
                Endec.DOUBLE.nullableOf().optionalFieldOf("height", s -> s.height, (Double) null),
                ROOT.fieldOf("@child", s -> s.child),
                Sized::new
            )
        );

        //noinspection unchecked
        register(
            "flex",
            Flex.class,
            StructEndecBuilder.of(
                BraidKdlEndecs.LAYOUT_AXIS.fieldOf("@argument", s -> s.mainAxis),
                BraidKdlEndecs.MAIN_AXIS_ALIGNMENT.optionalFieldOf("main_axis_alignment", s -> s.mainAxisAlignment, MainAxisAlignment.START),
                BraidKdlEndecs.CROSS_AXIS_ALIGNMENT.optionalFieldOf("cross_axis_alignment", s -> s.crossAxisAlignment, CrossAxisAlignment.START),
                ROOT.listOf().fieldOf("@children", s -> (java.util.List<Widget>) s.children),
                (mainAxis, mainAxisAlignment, crossAxisAlignment, children) ->
                    new Flex(mainAxis, mainAxisAlignment, crossAxisAlignment, null, children)
            )
        );

        register(
            "flexible",
            Flexible.class,
            StructEndecBuilder.of(
                Endec.DOUBLE.optionalFieldOf("flex_factor", s -> s.flexFactor, 1.0),
                ROOT.fieldOf("@child", s -> s.child),
                Flexible::new
            )
        );

        //noinspection unchecked
        register(
            "stack",
            Stack.class,
            StructEndecBuilder.of(
                BraidKdlEndecs.ALIGNMENT.optionalFieldOf("alignment", s -> s.alignment, Alignment.TOP_LEFT),
                ROOT.listOf().fieldOf("@children", s -> (java.util.List<Widget>) s.children),
                Stack::new
            )
        );

        register(
            "stack_base",
            StackBase.class,
            StructEndecBuilder.of(
                ROOT.fieldOf("@child", s -> s.child),
                StackBase::new
            )
        );

        var tightCellFitEndec = Endec.unit(Grid.CellFit.tight());
        var looseCellFitEndec = StructEndecBuilder.of(
            BraidKdlEndecs.ALIGNMENT.fieldOf("@argument", s -> ((Grid.CellFit.Loose)s).alignment),
            Grid.CellFit::loose
        );

        var cellFitEndec = Endec.dispatchedStruct(
            s -> switch (s) {
                case "tight" -> tightCellFitEndec;
                case "loose" -> looseCellFitEndec;
                default -> throw new IllegalStateException("invalid cell fit: " + s);
            },
            cellFit -> switch (cellFit) {
                case Grid.CellFit.Tight ignored -> "tight";
                case Grid.CellFit.Loose ignored -> "loose";
            },
            Endec.STRING,
            "@name"
        );

        //noinspection unchecked
        register(
            "grid",
            Grid.class,
            StructEndecBuilder.of(
                BraidKdlEndecs.LAYOUT_AXIS.fieldOf("@argument", s -> s.mainAxis),
                Endec.INT.fieldOf("cross_axis_cells", s -> s.crossAxisCells),
                StructEndecBuilder.of(cellFitEndec.fieldOf("@child", s -> s), cellFit -> cellFit).fieldOf(".fit", s -> s.cellFit),
                ROOT.listOf().fieldOf("@children", s ->  (java.util.List<Widget>) s.children),
                Grid::new
            )
        );

        var labelStyleEndec = StructEndecBuilder.of(
            BraidKdlEndecs.ALIGNMENT.nullableOf().optionalFieldOf("text_alignment", LabelStyle::textAlignment, (Alignment) null),
            BraidKdlEndecs.COLOR.nullableOf().optionalFieldOf("base_color", LabelStyle::baseColor, (Color) null),
            Endec.BOOLEAN.nullableOf().optionalFieldOf("shadow", LabelStyle::shadow, (Boolean) null),
            (alignment, color, shadow) ->
                new LabelStyle(alignment, color, null, shadow)
        );

        register(
            "label",
            Label.class,
            StructEndecBuilder.of(
                labelStyleEndec.nullableOf().optionalFieldOf(".style", s -> s.style, (LabelStyle) null),
                Endec.BOOLEAN.optionalFieldOf("soft_wrap", s -> s.softWrap, true),
                Endec.forEnum(Label.Overflow.class, false).optionalFieldOf("overflow", s -> s.overflow, Label.Overflow.CLIP),
                Endec.BOOLEAN.optionalFieldOf("translate", s -> s.text.getContents() instanceof TranslatableContents, false),
                Endec.STRING.fieldOf("@argument", s -> s.text.getString()),
                (style, softWrap, overflow, translate, text) -> new Label(
                    style,
                    softWrap,
                    overflow,
                    translate ? Component.translatable(text) : Component.literal(text)
                )
            )
        );

        register(
            "texture",
            TextureWidget.class,
            StructEndecBuilder.of(
                MinecraftEndecs.IDENTIFIER.fieldOf("@argument", s -> s.texture),
                Endec.forEnum(TextureWidget.Wrap.class, false).optionalFieldOf("wrap", s -> s.wrap, TextureWidget.Wrap.STRETCH),
                Endec.forEnum(TextureWidget.Filter.class, false).optionalFieldOf("filter", s -> s.filter, TextureWidget.Filter.TEXTURE_DEFAULT),
                BraidKdlEndecs.COLOR.optionalFieldOf("color", s -> s.color, Color.WHITE),
                TextureWidget::new
            )
        );

        register(
            "sprite",
            SpriteWidget.class,
            StructEndecBuilder.of(
                MinecraftEndecs.IDENTIFIER.fieldOf("@argument", s -> s.spriteIdentifier.texture()),
                MinecraftEndecs.IDENTIFIER.optionalFieldOf("atlas", s -> s.spriteIdentifier.atlasLocation(), SpriteWidget.GUI_ATLAS_ID),
                (id, atlas) -> new SpriteWidget(new Material(atlas, id))
            )
        );

        register(
            "box",
            Box.class,
            StructEndecBuilder.of(
                BraidKdlEndecs.COLOR.fieldOf("@argument", s -> s.color),
                Endec.BOOLEAN.optionalFieldOf("outline", s -> s.outline, false),
                ROOT.nullableOf().optionalFieldOf("@child", s -> s.child, (Widget) null),
                Box::new
            )
        );

        register(
            "transform",
            Transform.class,
            StructEndecBuilder.of(
                BraidKdlEndecs.TRANSFORM_MATRIX_2D.fieldOf(".steps", s -> s.matrix),
                ROOT.fieldOf("@child", s -> s.child),
                Transform::new
            )
        );

        register(
            "item",
            ItemStackWidget.class,
            StructEndecBuilder.of(
                Endec.forEnum(ItemDisplayContext.class, false).optionalFieldOf("display_context", ItemStackWidget::displayContext, ItemDisplayContext.GUI),
                Endec.BOOLEAN.optionalFieldOf("show_overlay", ItemStackWidget::showOverlay, true),
                Endec.forEnum(ItemStackWidget.LightOverride.class, false).nullableOf().optionalFieldOf("light_override", ItemStackWidget::lightOverride, (ItemStackWidget.LightOverride) null),
                BraidKdlEndecs.ITEM_STACK_STRING.fieldOf("@argument", s -> s.stack),
                (displayContext, showOverlay, lightOverride, stack) -> new ItemStackWidget(
                    stack,
                    widget -> widget
                        .displayContext(displayContext)
                        .showOverlay(showOverlay)
                        .lightOverride(lightOverride)
                )
            )
        );

        register(
            "block",
            BlockWidget.class,
            StructEndecBuilder.of(
                BraidKdlEndecs.BLOCK_STRING.fieldOf("@argument", s -> new BlockStateParser.BlockResult(s.blockState, s.blockState.getValues(), s.blockEntityNbt)),
                blockResult -> new BlockWidget(blockResult.blockState(), blockResult.nbt())
            )
        );

        register(
            "entity",
            KdlEntityWidget.class,
            StructEndecBuilder.of(
                Endec.DOUBLE.optionalFieldOf("scale", s -> s.scale, 1.0),
                KdlEntityWidget.EntitySpec.STRING_ENDEC.fieldOf("@argument", s -> s.spec),
                Endec.forEnum(EntityWidget.DisplayMode.class, false).optionalFieldOf("mode", s -> s.mode, EntityWidget.DisplayMode.FIXED),
                Endec.BOOLEAN.optionalFieldOf("scale_to_fit", s -> s.scaleToFit, true),
                Endec.BOOLEAN.optionalFieldOf("show_nametag", s -> s.showNametag, false),
                KdlEntityWidget::new
            )
        );

        var handlerEndec = Endec.STRING
            .optionalOf()
            .xmapWithContext(
                (ctx, maybeHandlerId) -> maybeHandlerId.flatMap(handlerId -> {
                    var handler = ctx.getAttributeValue(BraidKdlEndecs.HANDLERS).get(handlerId);
                    if (handler == null) {
                        throw new UnsupportedOperationException("missing handler with id: " + handlerId);
                    }

                    return Optional.of(handler);
                }),
                (context, o) -> { throw new UnsupportedOperationException("cannot serialize a braid kdl handler"); }
            );

        var handlerArgEndec = Endec.of(
            (ctx, serializer, o) -> { throw new UnsupportedOperationException("cannot serialize a braid kdl handler argument"); },
            (ctx, deserializer) -> {
                if (!(deserializer instanceof SelfDescribedDeserializer<?> selfDescribedDeserializer)) {
                    throw new UnsupportedOperationException("can only deserialize braid kdl handler arguments from self-described input");
                }

                var visitor = JavaSerializer.of();
                selfDescribedDeserializer.readAny(ctx, visitor);

                return visitor.result();
            }
        );

        register(
            "message_button",
            MessageButton.class,
            StructEndecBuilder.of(
                Endec.STRING.fieldOf("message", s -> s.text.getString()),
                Endec.BOOLEAN.optionalFieldOf("translate_message", s -> s.text.getContents() instanceof TranslatableContents, () -> false),
                handlerEndec.fieldOf("handler", s -> { throw new UnsupportedOperationException("cannot serialize a button callback"); }),
                handlerArgEndec.nullableOf().optionalFieldOf("handler_arg", s -> { throw new UnsupportedOperationException("cannot serialize a button's callback argument"); }, (Object) null),
                (message, translateMessage, handler, handlerArg) -> new MessageButton(
                    translateMessage
                        ? Component.translatable(message)
                        : Component.literal(message),
                    handler.<Runnable>flatMap(o -> {
                        return Optional.of(() -> o.accept(handlerArg));
                    }).orElse(null)
                )
            )
        );

        register(
            "button",
            Button.class,
            StructEndecBuilder.of(
                handlerEndec.fieldOf("handler", s -> { throw new UnsupportedOperationException("cannot serialize a button callback"); }),
                handlerArgEndec.nullableOf().optionalFieldOf("handler_arg", s -> { throw new UnsupportedOperationException("cannot serialize a button's callback argument"); }, (Object) null),
                ROOT.fieldOf("@child", s -> s.child),
                (handler, handlerArg, child) -> {
                    return new Button(
                        handler.<Runnable>flatMap(o -> {
                            return Optional.of(() -> o.accept(handlerArg));
                        }).orElse(null),
                        child
                    );
                }
            )
        );
    }
}
