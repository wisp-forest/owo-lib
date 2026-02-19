package io.wispforest.owo.ui.base;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.core.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

@ApiStatus.Experimental
public abstract class BaseOwoToast<R extends ParentUIComponent> implements Toast {

    protected final R rootComponent;
    protected final VisibilityPredicate<R> visibilityPredicate;

    protected int virtualWidth = 1000, virtualHeight = 1000;

    protected BaseOwoToast(Supplier<R> components, VisibilityPredicate<R> predicate) {
        this.rootComponent = components.get();
        this.visibilityPredicate = predicate;

        this.rootComponent.inflate(Size.of(this.virtualWidth, this.virtualHeight));
        this.rootComponent.mount(null, 0, 0);
    }

    protected BaseOwoToast(Supplier<R> rootComponent, Duration timeout) {
        this(rootComponent, VisibilityPredicate.timeout(timeout));
    }

    private Visibility visibility = Visibility.HIDE;

    @Override
    public void update(ToastManager manager, long time) {
        final var delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();

        var client = manager.getMinecraft();
        var window = client.getWindow();

        int mouseX = -1000; //(int)(client.mouse.getX() * (double) window.getScaledWidth() / (double) window.getWidth());
        int mouseY = -1000; //(int)(client.mouse.getY() * (double) window.getScaledHeight() / (double) window.getHeight());

        this.rootComponent.update(delta, mouseX, mouseY);

        this.visibility = this.visibilityPredicate.test(this, time);
    }

    @Override
    public Visibility getWantedVisibility() {
        return this.visibility;
    }

    @Override
    public void render(GuiGraphics context, Font textRenderer, long startTime) {
        var tickCounter = Minecraft.getInstance().getDeltaTracker();

        this.rootComponent.draw(OwoUIGraphics.of(context), -1000, -1000, tickCounter.getGameTimeDeltaPartialTick(false), tickCounter.getGameTimeDeltaTicks());
    }

    @Override
    public int height() {
        return this.rootComponent.fullSize().height();
    }

    @Override
    public int width() {
        return this.rootComponent.fullSize().width();
    }

    @FunctionalInterface
    public interface VisibilityPredicate<R extends ParentUIComponent> {
        Visibility test(BaseOwoToast<R> toast, long startTime);

        static <R extends ParentUIComponent> VisibilityPredicate<R> timeout(Duration timeout) {
            return (toast, startTime) -> System.currentTimeMillis() - startTime <= timeout.get(ChronoUnit.MILLIS) ? Visibility.HIDE : Visibility.SHOW;
        }
    }
}
