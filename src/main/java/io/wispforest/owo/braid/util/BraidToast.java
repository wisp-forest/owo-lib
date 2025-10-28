package io.wispforest.owo.braid.util;

import com.google.common.base.Preconditions;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.braid.core.EventBinding;
import io.wispforest.owo.braid.core.Surface;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.InheritedWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Align;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class BraidToast implements Toast {

    private Visibility visibility = Visibility.SHOW;
    private final @Nullable Duration timeout;
    private final Object token;
    private final AppState app;

    private EmbedderRoot.Instance rootInstance;

    private BraidToast(@Nullable Duration timeout, @Nullable Object token, Widget widget) {
        this.timeout = timeout;
        this.token = token != null ? token : new Object();
        this.app = new AppState(
            Owo.LOGGER,
            AppState.formatName("BraidToast", widget),
            MinecraftClient.getInstance(),
            new Surface.Default(),
            new EventBinding.Headless(),
            new Align(
                Alignment.TOP_LEFT,
                new EmbedderRoot(
                    instance -> this.rootInstance = instance,
                    new BraidToastProvider(
                        this,
                        widget
                    )
                )
            )
        );

        this.app.processEvents(0);
    }

    public static void show(@Nullable Duration timeout, @Nullable Object token, Widget widget) {
        MinecraftClient.getInstance().getToastManager().add(new BraidToast(timeout, token, widget));
    }

    public static void hideWithToken(Object token) {
        var toast = MinecraftClient.getInstance().getToastManager().getToast(BraidToast.class, token);
        if (toast != null) {
            toast.visibility = Visibility.HIDE;
        }
    }

    public static void hide(BuildContext context) {
        var provider = context.getAncestor(BraidToastProvider.class);
        Preconditions.checkNotNull(provider, "BraidToast.hide can only be used from inside a BraidToast's widget tree");

        provider.toast.visibility = Visibility.HIDE;
    }

    // ---

    @ApiStatus.Internal
    public void dispose() {
        this.app.dispose();
    }

    @Override
    public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        this.app.draw(context);

        if (this.timeout != null && startTime > this.timeout.toMillis()) {
            this.visibility = Visibility.HIDE;
        }

        return this.visibility;
    }

    @Override
    public int getWidth() {
        return (int) this.rootInstance.transform.width();
    }

    @Override
    public int getHeight() {
        return (int) this.rootInstance.transform.height();
    }

    // ---

    @Override
    public Object getType() {
        return this.token;
    }
}

class BraidToastProvider extends InheritedWidget {

    public final BraidToast toast;

    public BraidToastProvider(BraidToast toast, Widget child) {
        super(child);
        this.toast = toast;
    }

    @Override
    public boolean mustRebuildDependents(InheritedWidget newWidget) {
        return false;
    }
}
