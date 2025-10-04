package io.wispforest.owo.compat.rei;

import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.screen.Screen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReiUIAdapter<T extends ParentComponent> extends Widget {

    public static final Point LAYOUT = new Point(-69, -69);

    public final OwoUIAdapter<T> adapter;

    public ReiUIAdapter(Rectangle bounds, BiFunction<Sizing, Sizing, T> rootComponentMaker) {
        this.adapter = OwoUIAdapter.createWithoutScreen(bounds.x, bounds.y, bounds.width, bounds.height, rootComponentMaker);
        this.adapter.inspectorZOffset = 900;

        var screenWithREI = MinecraftClient.getInstance().currentScreen;

        if (screenWithREI != null) {
            currentREIAdapters.computeIfAbsent(MinecraftClient.getInstance().currentScreen, screen -> new HashSet<>()).add(this.adapter);
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
    public boolean mouseClicked(Click click, boolean doubled) {
        return this.adapter.mouseClicked(new Click(click.x() - this.adapter.x(), click.y() - this.adapter.y(), click.buttonInfo()), doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.adapter.mouseScrolled(mouseX - this.adapter.x(), mouseY - this.adapter.y(), horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseReleased(Click click) {
        return this.adapter.mouseReleased(new Click(click.x() - this.adapter.x(), click.y() - this.adapter.y(), click.buttonInfo()));
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        return this.adapter.mouseDragged(new Click(click.x() - this.adapter.x(), click.y() - this.adapter.y(), click.buttonInfo()), deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        return this.adapter.keyPressed(input);
    }

    @Override
    public boolean keyReleased(KeyInput input) {
        return this.adapter.keyReleased(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        return this.adapter.charTyped(input);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        context.enableScissor(this.adapter.x(), this.adapter.y(), this.adapter.width(), this.adapter.height());
        this.adapter.render(context, mouseX, mouseY, partialTicks);
        context.disableScissor();
    }

    @Override
    public List<? extends Element> children() {
        return List.of();
    }

    private static final Map<Screen, Set<OwoUIAdapter<?>>> currentREIAdapters = new HashMap<>();

    static {
        NeoForge.EVENT_BUS.<ScreenEvent.Closing>addListener((event) -> {
            var adapters = currentREIAdapters.remove(event.getScreen());

            if (adapters != null) adapters.forEach(OwoUIAdapter::dispose);
        });
        NeoForge.EVENT_BUS.<ScreenEvent.Render.Post>addListener((event) -> {
            var adapters = currentREIAdapters.get(event.getScreen());

            if (adapters != null) adapters.forEach(adapter -> {
                adapter.drawTooltip(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
            });
        });
    }
}
