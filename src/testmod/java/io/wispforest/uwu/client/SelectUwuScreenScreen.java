package io.wispforest.uwu.client;

import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.uwu.Uwu;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SelectUwuScreenScreen extends BaseOwoScreen<FlowLayout> {

    private static final Map<String, Runnable> SCREEN_SELECTION = new LinkedHashMap<>();

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
            .child(
                Components.label(Text.literal("Available screens"))
                    .shadow(true)
                    .margins(Insets.of(3, 5, 0, 0))
            )
            .surface(Surface.flat(0x77000000))
            .verticalAlignment(VerticalAlignment.CENTER)
            .horizontalAlignment(HorizontalAlignment.CENTER);

        var panel = Containers.verticalFlow(Sizing.content(), Sizing.content()).<FlowLayout>configure(layout -> {
            layout.gap(3)
                .horizontalAlignment(HorizontalAlignment.CENTER);
        });

        SCREEN_SELECTION.forEach((text, setScreenCall) -> {
            panel.child(
                Components.button(Text.literal(text), btn -> setScreenCall.run())
                    .horizontalSizing(Sizing.fill(100))
                    .verticalSizing(Sizing.fixed(16))
            );
        });

        rootComponent.child(
            Containers.verticalScroll(Sizing.fixed(150), Sizing.expand(100), panel)
                .padding(Insets.of(5))
                .surface(Surface.PANEL)
                .margins(Insets.bottom(10))
        );
    }

    public static void registerScreen(String text, Supplier<Screen> screenSupplier) {
        SCREEN_SELECTION.put(text, () -> MinecraftClient.getInstance().setScreen(screenSupplier.get()));
    }

    public static <P extends ParentComponent> void registerModelScreen(String text, Class<P> parentType, Identifier modelId) {
        registerScreen(text, () -> new BaseUIModelScreen<>(parentType, modelId) { @Override protected void build(P rootComponent) {} });
    }

    static {
        registerScreen("code demo", ComponentTestScreen::new);
        registerScreen("xml demo", TestParseScreen::new);
        registerScreen("code config", TestConfigScreen::new);
        registerScreen("xml config", () -> ConfigScreen.create(Uwu.CONFIG, null));
        registerScreen("optimization test", TooManyComponentsScreen::new);
        registerModelScreen("focus cycle test", FlowLayout.class, Identifier.of("uwu", "focus_cycle_test"));
        registerModelScreen("expand gap test", FlowLayout.class, Identifier.of("uwu", "expand_gap_test"));
        registerScreen("smolnite", SmolComponentTestScreen::new);
        registerScreen("sizenite", SizingTestScreen::new);
        registerScreen("parse fail", ParseFailScreen::new);
        registerScreen("scissor test", ScissorTestScreen::new);
        registerScreen("expanded labels", ExpandLabelTesting::new);
        registerScreen("funny nest test", NestedComponentTesting::new);
    }
}
