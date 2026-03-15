package io.wispforest.uwu.client.braid.test;

import com.mojang.authlib.GameProfile;
import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.KeyModifiers;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.basic.Center;
import io.wispforest.owo.braid.widgets.basic.EmptyWidget;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.checkbox.Checkbox;
import io.wispforest.owo.braid.widgets.checkbox.CheckboxStyle;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.focus.Focusable;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.object.entity.EntityDisplayMode;
import io.wispforest.owo.braid.widgets.object.entity.EntityWidget;
import io.wispforest.owo.braid.widgets.textinput.MaxLengthFormatter;
import io.wispforest.owo.braid.widgets.textinput.PatternFormatter;
import io.wispforest.owo.braid.widgets.textinput.TextBox;
import io.wispforest.owo.braid.widgets.textinput.TextEditingController;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.uwu.client.braid.TestSelector;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class TextInputTest extends StatefulWidget {
    @Override
    public WidgetState<TextInputTest> createState() {
        return new State();
    }

    public static class State extends WidgetState<TextInputTest> {
        private final TextEditingController controller1 = new TextEditingController();
        private final TextEditingController controller2 = new TextEditingController();
        private final TextEditingController controller3 = new TextEditingController();
        private final TextEditingController controller4 = new TextEditingController();
        private final TextEditingController controller5 = new TextEditingController();

        private Color numbersColor = Color.randomHue();

        @Override
        public Widget build(BuildContext context) {
            return new Row(
                MainAxisAlignment.CENTER,
                CrossAxisAlignment.CENTER,
                new Padding(Insets.horizontal(10)),
                List.of(
                    new Column(
                        MainAxisAlignment.START,
                        CrossAxisAlignment.CENTER,
                        new Sized(
                            100.0,
                            50.0,
                            new TextBox(
                                this.controller1,
                                widget -> widget
                                    .suggestion(Component.literal("Soft Wrapping Moment"))
                                    .formatter(PatternFormatter.deny(Pattern.compile("\\*\\*\\*\\*\\*"), "Penis"))
                                    .formatter(PatternFormatter.deny(Pattern.compile("\\*\\*\\*\\*"), "cunt"))
                            )
                        ),
                        new Sized(
                            100.0,
                            50.0,
                            new TextBox(
                                this.controller2,
                                widget -> widget
                                    .softWrap(false)
                                    .autoFocus(true)
                                    .placeholder(Component.literal("No Soft Wrapping Moment (also auto focused)"))
                            )
                        ),
                        new Sized(
                            100.0,
                            20,
                            new Focusable(
                                widget -> widget
                                    .skipTraversal(true)
                                    .keyDownCallback((keyCode, modifiers) -> {
                                        if (keyCode != GLFW.GLFW_KEY_ENTER || !modifiers.equals(KeyModifiers.NONE)) {
                                            return false;
                                        }

                                        this.setState(() -> this.numbersColor = Color.randomHue());
                                        return true;
                                    }),
                                new TextBox(
                                    this.controller3,
                                    widget -> widget
                                        .baseStyle(Style.EMPTY.withColor(this.numbersColor.argb()))
                                        .formatter(PatternFormatter.allow(Pattern.compile("[0-9]")))
                                        .placeholder(Component.literal("only numbers"))
                                )
                            )
                        ),
                        new Sized(
                            100.0,
                            20.0,
                            new TextBox(
                                this.controller4,
                                widget -> widget
                                    .singleLine()
                                    .placeholder(Component.literal("Single Line Moment"))
                            )
                        ),
                        new Sized(
                            100.0,
                            20.0,
                            new TextBox(
                                this.controller5,
                                widget -> widget
                                    .singleLine()
                                    .formatter(new MaxLengthFormatter(3))
                                    .placeholder(Component.literal("3 chars, TILI"))
                            )
                        )
                    ),
                    new ToggleFest()
                )
            );
        }
    }

    public static class ToggleFest extends StatefulWidget {
        @Override
        public WidgetState<ToggleFest> createState() {
            return new ToggleFest.State();
        }

        public static class State extends WidgetState<ToggleFest> {

            private final Entity chyz = EntityComponent.createRenderablePlayer(new GameProfile(
                UUID.fromString("09de8a6d-86bf-4c15-bb93-ce3384ce4e96"),
                "chyzman"
            ));

            private boolean checked = false;

            @Override
            public Widget build(BuildContext context) {
                return new Column(
                    MainAxisAlignment.CENTER,
                    CrossAxisAlignment.START,
                    new Padding(Insets.vertical(5)),
                    List.of(
                        new ToggleFest.LabelBox(
                            new Checkbox(
                                new CheckboxStyle(
                                    active -> new Sized(
                                        20,
                                        20,
                                        new EntityWidget(1.5d, this.chyz, widget -> widget.displayMode(EntityDisplayMode.CURSOR))
                                    ),
                                    EmptyWidget.INSTANCE,
                                    null
                                ), this.checked,
                                this::onUpdate
                            ),
                            "chyzbox"
                        ),
                        new ToggleFest.LabelBox(
                            new Checkbox(
                                new CheckboxStyle(
                                    null,
                                    new Center(new SpriteWidget(new Material(SpriteWidget.GUI_ATLAS_ID, Identifier.fromNamespaceAndPath("uwu", "czechbox")))),
                                    null
                                ), this.checked,
                                this::onUpdate
                            ),
                            this.checked ? "czechbox" : "checkbox"
                        ),
                        new ToggleFest.LabelBox(
                            new Checkbox(this.checked, this::onUpdate),
                            "checkbox"
                        ),
                        new ToggleFest.LabelBox(
                            new Checkbox(CheckboxStyle.BRAID, this.checked, this::onUpdate),
                            "smolbox"
                        )
                    )
                );
            }

            private void onUpdate(Boolean newState) {
                this.setState(() -> {
                    this.checked = newState;
                    this.chyz.setSharedFlagOnFire(this.checked);
                });
            }
        }

        public static class LabelBox extends StatelessWidget {
            public final Widget widget;
            public final String label;

            public LabelBox(Widget widget, String label) {
                this.widget = widget;
                this.label = label;
            }

            @Override
            public Widget build(BuildContext context) {
                return new Row(
                    MainAxisAlignment.START,
                    CrossAxisAlignment.CENTER,
                    new Padding(Insets.horizontal(4)),
                    List.of(
                        new Sized(
                            20,
                            20,
                            new Center(
                                this.widget
                            )
                        ),
                        Label.literal(this.label)
                    )
                );
            }
        }
    }
}
