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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class BraidToast implements Toast {

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
            Minecraft.getInstance(),
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
        Minecraft.getInstance().getToastManager().addToast(new BraidToast(timeout, token, widget));
    }

    public static void hideWithToken(Object token) {
        var toast = Minecraft.getInstance().getToastManager().getToast(BraidToast.class, token);
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
    public void render(GuiGraphics graphics, Font font, long startTime) {
        this.app.draw(graphics);
    }

    @Override
    public int width() {
        return (int) this.rootInstance.transform.width();
    }

    @Override
    public int height() {
        return (int) this.rootInstance.transform.height();
    }

    // ---

    private Visibility visibility = Visibility.SHOW;

    @Override
    public void update(ToastManager manager, long time) {
        if (this.timeout != null && time > this.timeout.toMillis()) {
            this.visibility = Visibility.HIDE;
        }

        var tickCounter = Minecraft.getInstance().getDeltaTracker();
        this.app.processEvents(
            tickCounter.getGameTimeDeltaTicks()
        );
    }

    @Override
    public Visibility getWantedVisibility() {
        return this.visibility;
    }

    @Override
    public Object getToken() {
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
