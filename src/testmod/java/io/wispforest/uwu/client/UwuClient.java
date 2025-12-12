package io.wispforest.uwu.client;

import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.util.BraidHudElement;
import io.wispforest.owo.braid.util.layers.BraidLayersBinding;
import io.wispforest.owo.braid.util.BraidTooltipComponent;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.Clip;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.basic.Transform;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.grid.Grid;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.owo.particles.ClientParticles;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import io.wispforest.owo.ui.layers.Layer;
import io.wispforest.owo.ui.layers.Layers;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.uwu.Uwu;
import io.wispforest.uwu.client.braid.TestSelector;
import io.wispforest.uwu.items.UwuBraidItem;
import io.wispforest.uwu.network.UwuNetworkExample;
import io.wispforest.uwu.network.UwuOptionalNetExample;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class UwuClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        UwuNetworkExample.Client.init();
        UwuOptionalNetExample.Client.init();

        MenuScreens.register(Uwu.EPIC_SCREEN_HANDLER_TYPE, EpicContainerScreen::new);
//        HandledScreens.register(EPIC_SCREEN_HANDLER_TYPE, EpicHandledModelScreen::new);

        final var binding = new KeyMapping("key.uwu.hud_test", GLFW.GLFW_KEY_J, KeyMapping.Category.MISC);
        KeyBindingHelper.registerKeyBinding(binding);

        final var bindingButCooler = new KeyMapping("key.uwu.hud_test_two", GLFW.GLFW_KEY_K, KeyMapping.Category.MISC);
        KeyBindingHelper.registerKeyBinding(bindingButCooler);

        final var hudComponentId = Identifier.fromNamespaceAndPath("uwu", "test_element");
        final Supplier<UIComponent> hudComponent = () ->
            UIContainers.verticalFlow(Sizing.content(), Sizing.content())
                .child(UIComponents.item(Items.DIAMOND.getDefaultInstance()).margins(Insets.of(3)))
                .child(UIComponents.label(Component.literal("epic stuff in hud")))
                .child(UIComponents.entity(Sizing.fixed(50), EntityType.ALLAY, null))
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                .padding(Insets.of(5))
                .surface(Surface.PANEL)
                .margins(Insets.of(5))
                .positioning(Positioning.relative(100, 25));

        final var coolerComponentId = Identifier.fromNamespaceAndPath("uwu", "test_element_two");
        final Supplier<UIComponent> coolerComponent = () -> UIModel.load(Path.of("../src/testmod/resources/assets/uwu/owo_ui/test_element_two.xml")).expandTemplate(FlowLayout.class, "hud-element", Map.of());
        Hud.add(coolerComponentId, coolerComponent);

        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof UwuBraidItem.Tooltip tooltip) {
                var random = new Random(System.currentTimeMillis() / 450);
                return new BraidTooltipComponent(new Sized(
                    32 * 5, 32 * 5, new Clip(
                        true, true,
                    new Row(
                        new Transform(
                            new Matrix3x2f().translation(((float) (System.currentTimeMillis() / 450d - Math.floor(System.currentTimeMillis() / 450d))) * -32, 0),
                            new Grid(
                                LayoutAxis.VERTICAL,
                                6,
                                Grid.CellFit.loose(),
                                Stream.generate(() -> new TestSelector.Amogus(
                                        new Box(io.wispforest.owo.braid.core.Color.hsv(random.nextDouble(), .75, 1)),
                                        new Box(Color.WHITE.toBraid()),
                                        8
                                    ))
                                    .limit(6 * 5).toList()
                            )
                        )
                    )
                )
                ));
            }

            return null;
        });

        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("uwu", "braid_test"),
            new BraidHudElement(new HudTestWidget())
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (binding.consumeClick()) {
                if (Hud.hasComponent(hudComponentId)) {
                    Hud.remove(hudComponentId);
                } else {
                    Hud.add(hudComponentId, hudComponent);
                }
            }

            if (bindingButCooler.consumeClick()) {
                Hud.remove(coolerComponentId);
                Hud.add(coolerComponentId, coolerComponent);

                //noinspection StatementWithEmptyBody
                while (bindingButCooler.consumeClick()) {}
            }
        });

        Uwu.CHANNEL.registerClientbound(Uwu.OtherTestMessage.class, (message, access) -> {
            access.player().displayClientMessage(Component.nullToEmpty("Message '" + message.message() + "' from " + message.pos()), false);
        });

        if (Uwu.WE_TESTEN_HANDSHAKE) {
            OwoNetChannel.create(Identifier.fromNamespaceAndPath("uwu", "client_only_channel"));

            Uwu.CHANNEL.registerServerbound(WeirdMessage.class, (data, access) -> {
            });
            Uwu.CHANNEL.registerClientbound(WeirdMessage.class, (data, access) -> {
            });

            new ParticleSystemController(Identifier.fromNamespaceAndPath("uwu", "client_only_particles"));
            Uwu.PARTICLE_CONTROLLER.register(WeirdMessage.class, (world, pos, data) -> {
            });
        }

        Uwu.CUBE.setHandler((world, pos, data) -> {
            ClientParticles.setParticleCount(5);
            ClientParticles.spawnCubeOutline(ParticleTypes.END_ROD, world, pos, 1, .01f);
        });

        Layers.add(UIContainers::verticalFlow, instance -> {
            if (Minecraft.getInstance().level == null) return;

            instance.adapter.rootComponent.child(
                UIContainers.horizontalFlow(Sizing.content(), Sizing.content())
                    .child(UIComponents.entity(Sizing.fixed(20), EntityType.ALLAY, null).<EntityComponent<Allay>>configure(component -> {
                        component.allowMouseRotation(true)
                            .scale(.75f);

                        component.mouseDown().subscribe((click, doubled) -> {
                            UISounds.playInteractionSound();
                            return true;
                        });
                    })).child(UIComponents.textBox(Sizing.fixed(100), "allay text").<EditBox>configure(textBox -> {
                        textBox.verticalSizing(Sizing.fixed(9));
                        textBox.setBordered(false);
                    })).<FlowLayout>configure(layout -> {
                        layout.gap(5).margins(Insets.left(4)).verticalAlignment(VerticalAlignment.CENTER);

                        instance.alignComponentToWidget(widget -> {
                            if (!(widget instanceof Button button)) return false;
                            return button.getMessage().getContents() instanceof TranslatableContents translatable && translatable.getKey().equals("gui.stats");
                        }, Layer.Instance.AnchorSide.RIGHT, 0, layout);
                    })
            );
        }, PauseScreen.class);

        Layers.add(UIContainers::verticalFlow, instance -> {
            ButtonComponent button;
            instance.adapter.rootComponent.child(
                (button = UIComponents.button(Component.literal(":)"), buttonComponent -> {
                    Minecraft.getInstance().player.displayClientMessage(Component.literal("handled screen moment"), false);
                })).verticalSizing(Sizing.fixed(12))
            );

            instance.alignComponentToHandledScreenCoordinates(button, 125, 65);
        }, InventoryScreen.class);

        BraidLayersBinding.add(
            screen -> screen instanceof InventoryScreen,
            new LayersTestWidget()
        );

        BlockEntityRenderers.register(Uwu.BRAID_DISPLAY_ENTITY, BraidDisplayBlockEntityRenderer::new);
    }

    public record WeirdMessage(int e) {}
}
