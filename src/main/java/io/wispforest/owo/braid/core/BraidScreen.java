package io.wispforest.owo.braid.core;

import io.wispforest.owo.braid.core.events.*;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.BraidApp;
import io.wispforest.owo.ui.util.DisposableScreen;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

public class BraidScreen extends Screen implements DisposableScreen {

    protected final EventBinding eventBinding = new EventBinding.Default();
    protected final Surface.Default surface = new Surface.Default();
    protected final Vector2i cursorPos = new Vector2i();

    protected final Settings settings;
    protected final Widget rootWidget;
    public AppState state;

    public BraidScreen(Settings settings, Widget rootWidget) {
        super(Text.empty());
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
                this.client,
                this.surface,
                this.eventBinding,
                new BraidScreenProvider(this, widget)
            );
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        var deltaX = mouseX - this.cursorPos.x;
        var deltaY = mouseY - this.cursorPos.y;

        this.cursorPos.x = mouseX;
        this.cursorPos.y = mouseY;

        if (deltaX != 0 || deltaY != 0) {
            this.eventBinding.add(new MouseMoveEvent(this.cursorPos.x, this.cursorPos.y, deltaX, deltaY));
        }

        this.state.processEvents(
            this.client.getRenderTickCounter().getDynamicDeltaTicks()
        );

        this.state.draw(context);
    }

    @Override
    public void dispose() {
        this.state.dispose();
    }

    @Override
    public boolean shouldPause() {
        return this.settings.shouldPause;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        this.eventBinding.add(new MouseButtonPressEvent(click.button(), click.modifiers()));
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        this.eventBinding.add(new MouseButtonReleaseEvent(click.button(), click.modifiers()));
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.eventBinding.add(new MouseScrollEvent(horizontalAmount, verticalAmount));
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        this.eventBinding.add(new KeyPressEvent(input.key(), input.scancode(), input.modifiers()));
        return super.keyPressed(input);
    }

    @Override
    public boolean keyReleased(KeyInput input) {
        this.eventBinding.add(new KeyReleaseEvent(input.key(), input.scancode(), input.modifiers()));
        return true;
    }

    @Override
    public boolean charTyped(CharInput input) {
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