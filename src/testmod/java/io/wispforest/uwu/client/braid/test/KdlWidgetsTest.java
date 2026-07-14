package io.wispforest.uwu.client.braid.test;

import com.google.gson.GsonBuilder;
import dev.kdl.KdlNode;
import dev.kdl.parse.Kdl2Parser;
import io.wispforest.endec.SerializationAttributes;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.format.gson.GsonSerializer;
import io.wispforest.owo.braid.core.*;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.util.kdl.BraidKdlEndecs;
import io.wispforest.owo.braid.util.kdl.KdlDeserializer;
import io.wispforest.owo.braid.util.kdl.KdlMapper;
import io.wispforest.owo.braid.util.kdl.WidgetEndec;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.EmptyWidget;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.button.MessageButton;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.scroll.ScrollAnimationSettings;
import io.wispforest.owo.braid.widgets.scroll.VerticallyScrollable;
import io.wispforest.owo.braid.widgets.textinput.TextBox;
import io.wispforest.owo.braid.widgets.textinput.TextEditingController;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;

import java.util.Map;
import java.util.Objects;

public class KdlWidgetsTest extends StatefulWidget {
    @Override
    public WidgetState<KdlWidgetsTest> createState() {
        return new State();
    }

    public static class State extends WidgetState<KdlWidgetsTest> {

        private TextEditingController textController;

        private KdlNode rootNode;
        private Widget kdlWidget = EmptyWidget.INSTANCE;

        @Override
        public void init() {
            this.textController = new TextEditingController();
            this.textController.addListener(() -> {
                try {
                    var parsedKdl = new Kdl2Parser().parse(this.textController.value().text());
                    this.rootNode = parsedKdl.nodes().getFirst();

                    var deserializer = new KdlDeserializer(this.rootNode, KdlMapper.DEFAULT_MAPPERS);
                    var ctx = deserializer.setupContext(SerializationContext.attributes(
                        SerializationAttributes.HUMAN_READABLE,
                        BraidKdlEndecs.HANDLERS.instance(Map.of("lmao", (theArg) -> System.out.println("lmao: " + theArg)))
                    ));

                    var parsedWidget = WidgetEndec.ROOT.decode(ctx, deserializer);
                    this.setState(() -> {
                        this.kdlWidget = parsedWidget;
                    });
                } catch (Exception e) {
                    Throwable cause = e;
                    while (cause.getCause() != null) {
                        cause = cause.getCause();
                    }

                    var bruhJava = cause;
                    this.setState(() -> {
                        this.kdlWidget = new Label(
                            LabelStyle.SHADOW,
                            true,
                            Component.literal(Objects.requireNonNullElse(bruhJava.getMessage(), "no message")).withStyle(ChatFormatting.RED)
                        );
                    });
                }
            });
        }

        private void showJson() {
            var deserializer = new KdlDeserializer(this.rootNode, KdlMapper.DEFAULT_MAPPERS);
            var jsonOut = GsonSerializer.of();

            deserializer.readAny(SerializationContext.attributes(SerializationAttributes.HUMAN_READABLE), jsonOut);
            var jsonText = new GsonBuilder().setPrettyPrinting().create().toJson(jsonOut.result());

            var widget = new Box(
                Color.mix(.1, Color.BLACK, Color.WHITE),
                new VerticallyScrollable(
                    null,
                    ScrollAnimationSettings.DEFAULT,
                    new Padding(
                        Insets.all(5),
                        new Label(
                            new LabelStyle(Alignment.TOP_LEFT, null, Style.EMPTY.withFont(new FontDescription.Resource(Minecraft.DEFAULT_FONT)), false),
                            true,
                            Component.literal(jsonText)
                        )
                    )
                )
            );

            BraidWindow.open(
                "json preview",
                650, 650,
                widget
            );
        }

        @Override
        public Widget build(BuildContext context) {
            return new Row(
                MainAxisAlignment.START,
                CrossAxisAlignment.CENTER,
                new Column(
                    MainAxisAlignment.START,
                    CrossAxisAlignment.END,
                    new Sized(
                        350, 300,
                        new TextBox(
                            this.textController,
                            widget -> widget
                                .placeholder(Component.literal("KDL goes here"))
                                .softWrap(false)
                        )
                    ),
                    new MessageButton(
                        Component.literal("view as json"),
                        this::showJson
                    )
                ),
                new Sized(
                    Size.square(250),
                    this.kdlWidget
                )
            );
        }
    }
}
