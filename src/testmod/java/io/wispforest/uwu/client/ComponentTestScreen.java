package io.wispforest.uwu.client;

import com.mojang.authlib.GameProfile;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.IntStream;

public class ComponentTestScreen extends Screen {

    private OwoUIAdapter<FlowLayout> uiAdapter = null;
//    private RenderEffectWrapper<?>.RenderEffectSlot fadeSlot = null;

    public ComponentTestScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        this.uiAdapter = OwoUIAdapter.create(this, UIContainers::horizontalFlow);
        final var rootComponent = uiAdapter.rootComponent;

        rootComponent.child(
                UIContainers.verticalFlow(Sizing.content(), Sizing.content())
                        .child(UIComponents.button(Component.nullToEmpty("Dark Background"), button -> rootComponent.surface(Surface.flat(0x77000000))).horizontalSizing(Sizing.fixed(95)))
                        .child(UIComponents.button(Component.nullToEmpty("No Background"), button -> rootComponent.surface(Surface.BLANK)).margins(Insets.vertical(5)).horizontalSizing(Sizing.fixed(95)))
                        .child(UIComponents.button(Component.nullToEmpty("Dirt Background"), button -> rootComponent.surface(Surface.optionsBackground())).horizontalSizing(Sizing.fixed(95)))
                        .child(UIComponents.checkbox(Component.nullToEmpty("bruh")).onChanged(aBoolean -> this.minecraft.player.displayClientMessage(Component.nullToEmpty("bruh: " + aBoolean), false)).margins(Insets.top(5)))
                        .padding(Insets.of(10))
                        .surface(Surface.vanillaPanorama(true))
                        .positioning(Positioning.relative(1, 1))
        );

        final var innerLayout = UIContainers.verticalFlow(Sizing.content(100), Sizing.content());
        var verticalAnimation = innerLayout.verticalSizing().animate(350, Easing.SINE, Sizing.content(50));

        verticalAnimation.finished().subscribe((direction, looping) -> {
            minecraft.gui.getChat().addMessage(Component.literal("vertical animation finished in direction " + direction.name()));
        });

        final var bruh = UIComponents.box(Sizing.fixed(150), Sizing.fixed(20));
        bruh.horizontalSizing().animate(5000, Easing.QUARTIC, Sizing.fixed(10)).forwards();
        innerLayout.child(bruh);

        final var otherBox = UIContainers.verticalFlow(Sizing.fixed(150), Sizing.fixed(20));
        otherBox.surface(Surface.flat(Color.BLACK.argb())).horizontalSizing().animate(5000, Easing.QUARTIC, Sizing.fixed(10)).forwards();
        innerLayout.child(otherBox);

        innerLayout.child(UIContainers.verticalScroll(Sizing.content(), Sizing.fixed(50), UIContainers.verticalFlow(Sizing.content(), Sizing.content())
                                .child(new BoxComponent(Sizing.fixed(20), Sizing.fixed(40)).margins(Insets.of(5)))
                                .child(new BoxComponent(Sizing.fixed(45), Sizing.fixed(45)).margins(Insets.of(5)))
                                .child(UIComponents.textBox(Sizing.fixed(60)))
                                .horizontalAlignment(HorizontalAlignment.RIGHT)
                                .surface(Surface.flat(0x77000000)))
                        .scrollbar(ScrollContainer.Scrollbar.vanilla())
                        .fixedScrollbarLength(15)
                        .scrollbarThiccness(12)
                        .id("scrollnite")
                )
                .child(UIComponents.button(Component.nullToEmpty("+"), (ButtonComponent button) -> {
                            verticalAnimation.reverse();

                            button.setMessage(verticalAnimation.direction() == Animation.Direction.FORWARDS
                                    ? Component.nullToEmpty("-")
                                    : Component.nullToEmpty("+")
                            );
                        }).<ButtonComponent>configure(button -> {
                            button.setTooltip(Tooltip.create(Component.nullToEmpty("a vanilla tooltip")));
                            button.margins(Insets.of(5)).sizing(Sizing.fixed(12));
                        })
                )
                .child(new BoxComponent(Sizing.fixed(40), Sizing.fixed(20)).margins(Insets.of(5)))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(5));

        innerLayout.child(UIComponents.textArea(Sizing.fixed(75), Sizing.content()).maxLines(5).displayCharCount(true));
        innerLayout.child(UIComponents.textArea(Sizing.fixed(75), Sizing.fixed(75)).<TextAreaComponent>configure(textArea -> {
            textArea.displayCharCount(true).setCharacterLimit(100);
        }));

        rootComponent.child(UIContainers.horizontalScroll(Sizing.fill(20), Sizing.content(), innerLayout)
                .scrollbarThiccness(6)
                .scrollbar(ScrollContainer.Scrollbar.vanillaFlat())
                .surface(Surface.DARK_PANEL)
                .padding(Insets.of(3))
        );

        rootComponent.child(UIContainers.verticalFlow(Sizing.content(), Sizing.content())
                .child(UIComponents.label(Component.literal("A profound vertical Flow Layout, as well as a leally long text to demonstrate wrapping").withStyle(style -> style.withFont(new FontDescription.Resource(Minecraft.UNIFORM_FONT)))
                                .withStyle(style -> {
                                    return style.withClickEvent(new ClickEvent.CopyToClipboard("yes"))
                                            .withHoverEvent(new HoverEvent.ShowItem(Items.SCULK_SHRIEKER.getDefaultInstance()));
                                }))
                        .shadow(true)
                        .lineHeight(7)
                        .lineSpacing(0)
                        .maxWidth(100)
                        .margins(Insets.horizontal(15)))
        );

        final var buttonPanel = UIContainers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(UIComponents.label(Component.literal("AAAAAAAAAAAAAAAAAAA").append(Component.literal("Layout")
                                .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowItem(Items.SCULK_SHRIEKER.getDefaultInstance()))))
                        .append(Component.literal("\nAAAAAAAAAAAAAAA"))).margins(Insets.of(5)))
                .child(UIComponents.button(Component.nullToEmpty("⇄"), button -> this.rebuildWidgets()).sizing(Sizing.fixed(20)))
                .child(UIComponents.button(Component.nullToEmpty("X"), button -> this.onClose()).sizing(Sizing.fixed(20)))
                .positioning(Positioning.relative(100, 0))
                .verticalAlignment(VerticalAlignment.CENTER)
                .surface(Surface.TOOLTIP)
                .padding(Insets.of(5))
                .margins(Insets.of(10));

        final var growingTextBox = UIComponents.textBox(Sizing.fixed(60));
        final var growAnimation = growingTextBox.horizontalSizing().animate(500, Easing.SINE, Sizing.fixed(80));
        growingTextBox.mouseEnter().subscribe(growAnimation::forwards);
        growingTextBox.mouseLeave().subscribe(growAnimation::backwards);

        var weeAnimation = buttonPanel.positioning().animate(1000, Easing.CUBIC, Positioning.relative(0, 100));
        rootComponent.child(UIContainers.verticalFlow(Sizing.content(), Sizing.content())
                .child(growingTextBox)
                .child(new SmallCheckboxComponent())
                .child(UIComponents.textBox(Sizing.fixed(60)))
                .child(UIComponents.button(Component.nullToEmpty("weeeee"), button -> {
                    weeAnimation.loop(!weeAnimation.looping());
                    rootComponent.<FlowLayout>configure(layout -> {
                        var padding = layout.padding().get();
                        for (int i = 0; i < 696969; i++) {
                            layout.padding(Insets.of(i));
                        }
                        layout.padding(padding.add(5, 5, 5, 5));
                    });
                }).renderer(ButtonComponent.Renderer.flat(0x77000000, 0x77070707, 0xA0000000)).sizing(Sizing.content()))
                .child(UIComponents.discreteSlider(Sizing.fill(10), 0, 5).<DiscreteSliderComponent>configure(
                        slider -> slider.snap(true)
                                .decimalPlaces(1)
                                .message(value -> Component.translatable("text.ui.test_slider", value))
                                .onChanged().subscribe(value -> {
                                    slider.parent().surface(Surface.blur(3, (float) (value * 3)));
                                    this.minecraft.player.displayClientMessage(Component.nullToEmpty("sliding towards " + value), false);
                                })
                ))
                .gap(10)
                .padding(Insets.both(5, 10))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .surface(Surface.blur(3, 0))
        );

        var dropdown = UIComponents.dropdown(Sizing.content())
                .checkbox(Component.nullToEmpty("more checking"), true, aBoolean -> {})
                .text(Component.nullToEmpty("hahayes"))
                .button(Component.nullToEmpty("epic button"), dropdownComponent -> {})
                .divider()
                .text(Component.nullToEmpty("very good"))
                .checkbox(Component.nullToEmpty("checking time"), false, aBoolean -> {})
                .nested(Component.nullToEmpty("nested entry"), Sizing.content(), nested -> {
                    nested.text(Component.nullToEmpty("nest title"))
                            .divider()
                            .button(Component.nullToEmpty("nest button"), dropdownComponent -> {});
                });

        var dropdownButton = UIComponents.button(Component.nullToEmpty("Dropdown"), button -> {
            if (dropdown.hasParent()) return;
            rootComponent.child(dropdown.positioning(Positioning.absolute(button.x(), button.y() + button.height())));
        }).margins(Insets.horizontal(8));
        dropdown.mouseLeave().subscribe(() -> dropdown.closeWhenNotHovered(true));

//        rootComponent.child(
//                Containers.renderEffect(
//                        Containers.verticalFlow(Sizing.content(), Sizing.content())
//                                .child(Containers.renderEffect(
//                                        Components.sprite(new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier.of("block/stone"))).margins(Insets.of(5))
//                                ).<RenderEffectWrapper<?>>configure(wrapper -> {
//                                    wrapper.effect(RenderEffectWrapper.RenderEffect.rotate(RotationAxis.POSITIVE_Z, -45));
//                                    wrapper.effect(RenderEffectWrapper.RenderEffect.color(Color.ofHsv(.5f, 1f, 1f)));
//                                }))
//                                .child(dropdownButton)
//                ).<RenderEffectWrapper<?>>configure(wrapper -> {
//                    wrapper.effect(RenderEffectWrapper.RenderEffect.transform(matrices -> matrices.translate(0, 25, 0)));
//
//                    wrapper.effect(RenderEffectWrapper.RenderEffect.rotate(90f));
//                    this.fadeSlot = wrapper.effect(RenderEffectWrapper.RenderEffect.color(Color.WHITE));
//                })
//        );

        rootComponent.mouseDown().subscribe((click, doubled) -> {
            if (click.button() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return false;
            DropdownComponent.openContextMenu(this, rootComponent, FlowLayout::child, click.x(), click.y(), contextMenu -> {
                contextMenu.text(Component.literal("That's a context menu"));
                contextMenu.checkbox(Component.literal("Yup"), true, aBoolean -> {});
                contextMenu.divider();
                contextMenu.button(Component.literal("Delet"), UIComponent::remove);
            });
            return true;
        });

//        rootComponent.child(
//                new BaseComponent() {
//                    @Override
//                    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
//                        context.drawCircle(
//                                this.x + this.width / 2,
//                                this.y + this.height / 2,
//                                75,
//                                this.width / 2f,
//                                Color.ofArgb(0x99000000)
//                        );
//
//                        context.drawRing(
//                                this.x + this.width / 2,
//                                this.y + this.height / 2,
//                                75,
//                                (this.width - 125) / 2f,
//                                this.width / 2f,
//                                Color.ofArgb(0x99000000),
//                                Color.ofArgb(0x99000000)
//                        );
//
//                        var time = (System.currentTimeMillis() / 1000d) % (Math.PI * 2);
//                        context.drawLine(
//                                (int) (this.x + this.width / 2 + Math.cos(time) * this.width / 2),
//                                (int) (this.y + this.height / 2 + Math.sin(time) * this.height / 2),
//                                (int) (this.x + this.width / 2 + Math.sin(time) * this.width / 2),
//                                (int) (this.y + this.height / 2 + Math.cos(time) * this.height / 2),
//                                1,
//                                Color.BLUE
//                        );
//
//                        context.drawSpectrum(this.x, this.y, this.width, (int) (this.height * (Math.sin(time) * .5 + .5)), true);
//                    }
//                }.positioning(Positioning.relative(50, 50)).sizing(Sizing.fixed(350)));
        rootComponent.child(
                UIComponents.button(Component.nullToEmpty("overlay"), button -> {
                    rootComponent.child(UIContainers.overlay(
                            UIContainers.verticalFlow(Sizing.content(), Sizing.content())
                                    .child(new ColorPickerComponent()
                                            .showAlpha(true)
                                            .selectedColor(Color.ofArgb(0x7F3955E5))
                                            .sizing(Sizing.fixed(160), Sizing.fixed(100))
                                    ).padding(Insets.of(5)).surface(Surface.DARK_PANEL)
                    ));
                })
        );


        // i knew it all along, chyz truly is a pig
        var pig = EntityComponent.createRenderablePlayer(new GameProfile(UUID.fromString("09de8a6d-86bf-4c15-bb93-ce3384ce4e96"), "chyzman"));
        pig.setSharedFlagOnFire(true);

        rootComponent.child(
                UIComponents.entity(Sizing.fixed(100), pig)
                        .allowMouseRotation(true)
                        .scaleToFit(true)
                        .showNametag(true)
                    .lookAtCursor(true)
        );

        rootComponent.child(
                UIComponents.block(Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.LIT, true), (CompoundTag) null).sizing(Sizing.fixed(100))
        );

        var bundle = Items.BUNDLE.getDefaultInstance();
        var itemList = new ArrayList<ItemStack>();
        itemList.add(new ItemStack(Items.EMERALD, 16));

        bundle.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(itemList));

        rootComponent.child(UIComponents.item(new ItemStack(Items.EMERALD, 16))
                .showOverlay(true)
                .setTooltipFromStack(true)
                .positioning(Positioning.absolute(120, 30))
        );

        final var buttonGrid = UIContainers.grid(Sizing.content(), Sizing.fixed(85), 3, 5);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 5; column++) {
                buttonGrid.child(
                        UIComponents.button(Component.nullToEmpty("" + (row * 5 + column)), button -> {
                            if (button.getMessage().getString().equals("11")) {
                                buttonGrid.child(UIComponents.button(Component.nullToEmpty("long boiii"), b -> buttonGrid.child(button, 2, 1)).margins(Insets.of(3)), 2, 1);
                            } else if (button.getMessage().getString().equals("8")) {
                                final var box = UIComponents.textBox(Sizing.fill(10));
                                box.setSuggestion("thicc boi");
                                box.sizing(box.horizontalSizing().get(), Sizing.fixed(40));

                                buttonGrid.child(box.margins(Insets.of(3)), 1, 3);
                            }
                        }).margins(Insets.of(3)).sizing(Sizing.fixed(20)),
                        row, column
                );
            }
        }

        rootComponent.child(buttonGrid
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .surface(Surface.PANEL)
                .padding(Insets.of(4))
        );

        var data = IntStream.rangeClosed(1, 15).boxed().toList();
        rootComponent.child(
                UIContainers.horizontalScroll(
                                Sizing.fixed(26 * 7 + 8),
                                Sizing.content(),
                                UIComponents.list(
                                        data,
                                        flowLayout -> flowLayout.margins(Insets.bottom(10)),
                                        integer -> UIComponents.button(Component.literal(integer.toString()), (ButtonComponent button) -> {}).margins(Insets.horizontal(3)).horizontalSizing(Sizing.fixed(20)),
                                        false
                                )
                        )
                        .scrollStep(26)
                        .scrollbarThiccness(7)
                        .scrollbar(ScrollContainer.Scrollbar.vanilla())
                        .surface(Surface.PANEL)
                        .padding(Insets.of(4, 5, 5, 5))
                        .margins(Insets.bottom(5))
                        .positioning(Positioning.relative(50, 100))
        );

        rootComponent.child(
                UIContainers.verticalFlow(Sizing.content(), Sizing.content())
                        .child(UIComponents.label(Component.literal("Cursor Tester").withColor(CommonColors.GRAY))
                                .tooltip(Component.literal("by chyzman")))
                        .child(
                                UIComponents.list(
                                        Arrays.stream(CursorStyle.values()).toList(),
                                        flowLayout -> flowLayout.margins(Insets.bottom(10)),
                                        cursor -> UIComponents.label(Component.literal(cursor.toString()).withColor(CommonColors.GRAY))
                                                .cursorStyle(cursor)
                                                .margins(Insets.horizontal(3)),
                                        true
                                )
                        )
                        .surface(Surface.PANEL)
                        .padding(Insets.of(4, 5, 5, 5))
                        .margins(Insets.bottom(5))
                        .positioning(Positioning.relative(100, 100))
        );

        // infinity scroll test
//        rootComponent.child(
//                Containers.verticalScroll(Sizing.fixed(243), Sizing.fixed(145),
//                        Components.box(Sizing.fixed(235), Sizing.fixed(144))
//                                .startColor(Color.GREEN)
//                                .endColor(Color.BLUE)
//                                .direction(BoxComponent.GradientDirection.TOP_TO_BOTTOM)
//                                .fill(true)
//                ).padding(Insets.of(4)).positioning(Positioning.absolute(150, 40))
//        );

        rootComponent.child(buttonPanel);
        rootComponent.surface(Surface.flat(0x77000000))
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER);

        uiAdapter.inflateAndMount();
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {}

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
//        this.fadeSlot.update(RenderEffectWrapper.RenderEffect.color(new Color(
//                1f, 1f, 1f,
//                (float) (Math.sin(System.currentTimeMillis() / 1000d) * .5 + .5)
//        )));
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.isEscape()) {
            this.onClose();
            return true;
        }

        if (input.key() == GLFW.GLFW_KEY_F12) {
            try (var out = Files.newOutputStream(Path.of("component_tree.dot")); var writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                writer.write("digraph D {\n");

                final var tree = new ArrayList<UIComponent>();
                this.uiAdapter.rootComponent.collectDescendants(tree);

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
            return this.uiAdapter.keyPressed(input);
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        return this.uiAdapter.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return this.uiAdapter;
    }

    @Override
    public void removed() {
        this.uiAdapter.dispose();
    }

    private String format(@Nullable UIComponent component) {
        if (component == null) {
            return "root";
        } else {
            return component.getClass().getSimpleName() + "@" + Integer.toHexString(component.hashCode())
                    + "(" + component.x() + " " + component.y() + ")";
        }
    }
}
