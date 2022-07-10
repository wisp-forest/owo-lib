package io.wispforest.uwu.client;

import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.component.BoundingBoxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.HoverContainer;
import io.wispforest.owo.ui.container.Layouts;
import io.wispforest.owo.ui.container.ScrollContainer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class ComponentTestScreen extends Screen {

    private OwoUIAdapter<FlowLayout> uiAdapter = null;

    public ComponentTestScreen() {
        super(Text.empty());
    }

    @Override
    protected void init() {
        this.uiAdapter = OwoUIAdapter.create(this, Layouts::horizontalFlow);
        final var rootComponent = uiAdapter.rootComponent;

        rootComponent.child(
                Layouts.verticalFlow(Sizing.content(), Sizing.content())
                        .child(Components.button(Text.of("Dark Background"), 95, 20, button -> rootComponent.surface(Surface.flat(0x77000000))))
                        .child(Components.button(Text.of("No Background"), 95, 20, button -> rootComponent.surface(Surface.BLANK)).margins(Insets.vertical(5)))
                        .child(Components.button(Text.of("Dirt Background"), 95, 20, button -> rootComponent.surface(Surface.OPTIONS_BACKGROUND)))
                        .padding(Insets.of(10))
                        .surface(Surface.flat(0x77000000))
                        .positioning(Positioning.relative(1, 1))
        );

        final var innerLayout = Layouts.verticalFlow(Sizing.content(100), Sizing.content());
        var verticalAnimation = innerLayout.verticalSizing().animate(350, Easing.SINE, Sizing.content(50));

        innerLayout.child(ScrollContainer.vertical(Sizing.content(), Sizing.fixed(50), Layouts.verticalFlow(Sizing.content(), Sizing.content())
                        .child(new BoundingBoxComponent(Sizing.fixed(20), Sizing.fixed(40)).margins(Insets.of(5)))
                        .child(new BoundingBoxComponent(Sizing.fixed(45), Sizing.fixed(45)).margins(Insets.of(5)))
                        .child(Components.textBox(Sizing.fixed(60)))
                        .horizontalAlignment(HorizontalAlignment.RIGHT)
                        .surface(Surface.flat(0x77000000)))
                )
                .child(Components.button(Text.of("Expand"), 60, 20, button -> {
                            verticalAnimation.reverse();

                            button.setMessage(verticalAnimation.direction() == Animation.Direction.FORWARDS
                                    ? Text.of("Contract")
                                    : Text.of("Expand")
                            );
                        }).margins(Insets.of(15, 15, 5, 5))
                )
                .child(new BoundingBoxComponent(Sizing.fixed(40), Sizing.fixed(20)).margins(Insets.of(5)))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(5));

        rootComponent.child(ScrollContainer.horizontal(Sizing.fill(20), Sizing.content(), innerLayout)
                .scrollbarThiccness(5)
                .surface(Surface.PANEL)
                .padding(Insets.of(3))
        );

        rootComponent.child(Components.label(Text.of("A vertical Flow Layout, as well as a really long text to demonstrate wrapping"))
                .shadow(true)
                .maxWidth(100)
                .margins(Insets.horizontal(15))
        );

        final var buttonPanel = Layouts.horizontalFlow(Sizing.content(), Sizing.content())
                .child(Components.label(Text.of("A horizontal Flow Layout\nwith a dark panel")).margins(Insets.of(5)))
                .child(Components.button(Text.of("⇄"), 20, 20, button -> this.clearAndInit()))
                .child(Components.button(Text.of("X"), 20, 20, button -> this.close()))
                .positioning(Positioning.relative(100, 0))
                .verticalAlignment(VerticalAlignment.CENTER)
                .surface(Surface.DARK_PANEL)
                .padding(Insets.of(5))
                .margins(Insets.of(10));

        var weeAnimation = buttonPanel.positioning().animate(450, Easing.SINE, Positioning.relative(0, 100));
        rootComponent.child(Layouts.verticalFlow(Sizing.content(), Sizing.content())
                .child(HoverContainer.forAnimation(
                        Components.textBox(Sizing.fixed(60)),
                        component -> component.horizontalSizing().animate(500, Easing.SINE, Sizing.fixed(80))
                ).margins(Insets.vertical(5)))
                .child(new TextFieldWidget(this.client.textRenderer, 0, 0, 60, 20, Text.empty()).margins(Insets.vertical(5)))
                .child(Components.button(Text.of("weeeee"), 0, 0, button -> {
                    weeAnimation.reverse();
                }).sizing(Sizing.content()).margins(Insets.vertical(5)))
                .padding(Insets.of(5))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .surface(Surface.DARK_PANEL)
        );

        final var buttonGrid = Layouts.grid(Sizing.content(), Sizing.content(), 3, 5);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 5; column++) {
                buttonGrid.child(
                        Components.button(Text.of("" + (row * 5 + column)), 20, 20, button -> {
                            if (button.getMessage().getString().equals("11")) {
                                buttonGrid.child(Components.button(Text.of("long boiii"), b -> buttonGrid.child(button, 2, 1)).margins(Insets.of(3)), 2, 1);
                            } else if (button.getMessage().getString().equals("8")) {
                                final var box = Components.textBox(Sizing.fill(10));
                                box.setSuggestion("thicc boi");
                                box.sizing(box.horizontalSizing().get(), Sizing.fixed(40));

                                buttonGrid.child(box.margins(Insets.of(3)), 1, 3);
                            }
                        }).margins(Insets.of(3)),
                        row, column
                );
            }
        }

        rootComponent.child(buttonGrid
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .surface(Surface.PANEL)
                .padding(Insets.of(4))
                .margins(Insets.left(15))
        );

        var data = IntStream.rangeClosed(1, 15).boxed().toList();
        rootComponent.child(
                ScrollContainer.horizontal(
                                Sizing.fill(25),
                                Sizing.content(),
                                Components.list(
                                        data,
                                        flowLayout -> {},
                                        integer -> Components.button(Text.literal(integer.toString()), button -> {}).margins(Insets.horizontal(3)).horizontalSizing(Sizing.fixed(20)),
                                        false
                                )
                        )
                        .surface(Surface.DARK_PANEL)
                        .padding(Insets.of(4))
                        .margins(Insets.left(15))
                        .positioning(Positioning.relative(50, 100))
        );

        rootComponent.child(buttonPanel);
        rootComponent.surface(Surface.flat(0x77000000))
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER);

        uiAdapter.inflateAndMount();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_F12) {
            try (var out = Files.newOutputStream(Path.of("component_tree.dot")); var writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                writer.write("digraph D {\n");

                final var tree = new ArrayList<Component>();
                this.uiAdapter.rootComponent.collectChildren(tree);

                for (var component : tree) {
                    writer.write("  \"" + format(component.parent()) + "\" -> \"" + format(component) + "\"\n");
                }

                writer.write("}");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return this.uiAdapter.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.uiAdapter.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Nullable
    @Override
    public Element getFocused() {
        return this.uiAdapter;
    }

    @Override
    public void removed() {
        this.uiAdapter.dispose();
    }

    private String format(@Nullable Component component) {
        if (component == null) {
            return "root";
        } else {
            return component.getClass().getSimpleName() + "@" + Integer.toHexString(component.hashCode())
                    + "(" + component.x() + " " + component.y() + ")";
        }
    }
}
