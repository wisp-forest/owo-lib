package io.wispforest.owo.braid.core;

import io.wispforest.owo.braid.core.events.*;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.BraidApp;
import io.wispforest.owo.ui.util.DisposableScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class BraidScreen extends Screen implements DisposableScreen {

    protected final EventBinding eventBinding = new EventBinding.Default();
    protected final Surface.Default surface = new Surface.Default();

    protected final Settings settings;
    protected final Widget rootWidget;
    public AppState state;

    public BraidScreen(Settings settings, Widget rootWidget) {
        super(Component.empty());
        this.settings = settings;
        this.rootWidget = rootWidget;
    }

    public BraidScreen(Widget rootWidget) {
        this(new Settings(), rootWidget);
    }

    @Override
    protected void init() {
        super.init();

        if (this.state == null) {
            var widget = this.settings.useBraidAppWidget
                ? new BraidApp(this.rootWidget)
                : this.rootWidget;

            this.state = new AppState(
                null,
                AppState.formatName("BraidScreen", this.rootWidget),
                this.minecraft,
                this.surface,
                this.eventBinding,
                new BraidScreenProvider(this, widget)
            );
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        this.eventBinding.add(new MouseMoveEvent(mouseX, mouseY));
        this.state.processEvents(
            this.minecraft.getDeltaTracker().getGameTimeDeltaTicks()
        );

        this.state.draw(graphics);
    }

    @Override
    public void dispose() {
        this.state.dispose();
    }

    @Override
    public boolean isPauseScreen() {
        return this.settings.shouldPause;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        this.eventBinding.add(new MouseButtonPressEvent(click.button(), click.modifiers()));
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        this.eventBinding.add(new MouseButtonReleaseEvent(click.button(), click.modifiers()));
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.eventBinding.add(new MouseScrollEvent(horizontalAmount, verticalAmount));
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        this.eventBinding.add(new KeyPressEvent(input.key(), input.scancode(), input.modifiers()));
        return super.keyPressed(input);
    }

    @Override
    public boolean keyReleased(KeyEvent input) {
        this.eventBinding.add(new KeyReleaseEvent(input.key(), input.scancode(), input.modifiers()));
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        this.eventBinding.add(new CharInputEvent((char) input.codepoint(), input.modifiers()));
        return true;
    }

    // ---

    public static @Nullable BraidScreen maybeOf(BuildContext context) {
        var provider = context.getAncestor(BraidScreenProvider.class);
        return provider != null ? provider.screen : null;
    }

    public static class Settings {
        public boolean shouldPause = true;
        public boolean useBraidAppWidget = true;
    }
}

class BraidScreenProvider extends InheritedWidget {

    public final BraidScreen screen;

    public BraidScreenProvider(BraidScreen screen, Widget child) {
        super(child);
        this.screen = screen;
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        return false;
    }
}