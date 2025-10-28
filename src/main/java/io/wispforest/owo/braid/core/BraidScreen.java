package io.wispforest.owo.braid.core;

import io.wispforest.owo.braid.core.events.*;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.BraidApp;
import io.wispforest.owo.ui.util.DisposableScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class BraidScreen extends Screen implements DisposableScreen {

    protected final EventBinding eventBinding = new EventBinding.Default();
    protected final Surface.Default surface = new Surface.Default();

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

        this.eventBinding.add(new MouseMoveEvent(mouseX, mouseY));
        this.state.processEvents(
            this.client.getRenderTickCounter().getLastFrameDuration()
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

    public boolean mouseClicked(double mouseX, double mouseY, int button, int modifiers) {
        this.eventBinding.add(new MouseButtonPressEvent(button, new KeyModifiers(modifiers)));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.mouseClicked(mouseX, mouseY, button, 0);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button, int modifiers) {
        this.eventBinding.add(new MouseButtonReleaseEvent(button, new KeyModifiers(modifiers)));
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.mouseReleased(mouseX, mouseY, button, 0);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.eventBinding.add(new MouseScrollEvent(horizontalAmount, verticalAmount));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.eventBinding.add(new KeyPressEvent(keyCode, scanCode, new KeyModifiers(modifiers)));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.eventBinding.add(new KeyReleaseEvent(keyCode, scanCode, new KeyModifiers(modifiers)));
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        this.eventBinding.add(new CharInputEvent(chr, new KeyModifiers(modifiers)));
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