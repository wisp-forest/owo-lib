package io.wispforest.owo.compat.rei;

import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.core.Sizing;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReiUIAdapter<T extends ParentUIComponent> extends Widget {

    public static final Point LAYOUT = new Point(-69, -69);

    public final OwoUIAdapter<T> adapter;

    public ReiUIAdapter(Rectangle bounds, BiFunction<Sizing, Sizing, T> rootComponentMaker) {
        this.adapter = OwoUIAdapter.createWithoutScreen(bounds.x, bounds.y, bounds.width, bounds.height, rootComponentMaker);
        this.adapter.inspectorZOffset = 900;

        var screenWithREI = Minecraft.getInstance().screen;

        if (screenWithREI != null) {
            ScreenEvents.remove(screenWithREI).register(screen -> this.adapter.dispose());
            ScreenEvents.afterRender(screenWithREI).register((screen, drawContext, mouseX, mouseY, tickDelta) -> {
                this.adapter.drawTooltip(drawContext, mouseX, mouseY, tickDelta);
            });
        }
    }

    public void prepare() {
        this.adapter.inflateAndMount();
    }

    public T rootComponent() {
        return this.adapter.rootComponent;
    }

    public <W extends WidgetWithBounds> ReiWidgetComponent wrap(W widget) {
        return new ReiWidgetComponent(widget);
    }

    public <W extends WidgetWithBounds> ReiWidgetComponent wrap(Function<Point, W> widgetFactory, Consumer<W> widgetConfigurator) {
        var widget = widgetFactory.apply(LAYOUT);
        widgetConfigurator.accept(widget);
        return new ReiWidgetComponent(widget);
    }

    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return this.adapter.isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        return this.adapter.mouseClicked(new MouseButtonEvent(click.x() - this.adapter.x(), click.y() - this.adapter.y(), click.buttonInfo()), doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.adapter.mouseScrolled(mouseX - this.adapter.x(), mouseY - this.adapter.y(), horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        return this.adapter.mouseReleased(new MouseButtonEvent(click.x() - this.adapter.x(), click.y() - this.adapter.y(), click.buttonInfo()));
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        return this.adapter.mouseDragged(new MouseButtonEvent(click.x() - this.adapter.x(), click.y() - this.adapter.y(), click.buttonInfo()), deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        return this.adapter.keyPressed(input);
    }

    @Override
    public boolean keyReleased(KeyEvent input) {
        return this.adapter.keyReleased(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        return this.adapter.charTyped(input);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        context.enableScissor(this.adapter.x(), this.adapter.y(), this.adapter.width(), this.adapter.height());
        this.adapter.render(context, mouseX, mouseY, partialTicks);
        context.disableScissor();
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of();
    }
}
