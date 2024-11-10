package io.wispforest.owo.ui.base;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Size;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

@ApiStatus.Experimental
public abstract class BaseOwoToast<R extends ParentComponent> implements Toast {

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
        final var delta = MinecraftClient.getInstance().getRenderTickCounter().getLastFrameDuration();

        var client = manager.getClient();
        var window = client.getWindow();

        int mouseX = -1000; //(int)(client.mouse.getX() * (double) window.getScaledWidth() / (double) window.getWidth());
        int mouseY = -1000; //(int)(client.mouse.getY() * (double) window.getScaledHeight() / (double) window.getHeight());

        this.rootComponent.update(delta, mouseX, mouseY);

        this.visibility = this.visibilityPredicate.test(this, time);
    }

    @Override
    public Visibility getVisibility() {
        return this.visibility;
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        var tickCounter = MinecraftClient.getInstance().getRenderTickCounter();

        this.rootComponent.draw(OwoUIDrawContext.of(context), -1000, -1000, tickCounter.getTickDelta(false), tickCounter.getLastFrameDuration());
    }

    @Override
    public int getHeight() {
        return this.rootComponent.fullSize().height();
    }

    @Override
    public int getWidth() {
        return this.rootComponent.fullSize().width();
    }

    @FunctionalInterface
    public interface VisibilityPredicate<R extends ParentComponent> {
        Visibility test(BaseOwoToast<R> toast, long startTime);

        static <R extends ParentComponent> VisibilityPredicate<R> timeout(Duration timeout) {
            return (toast, startTime) -> System.currentTimeMillis() - startTime <= timeout.get(ChronoUnit.MILLIS) ? Visibility.HIDE : Visibility.SHOW;
        }
    }
}
