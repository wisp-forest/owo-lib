package io.wispforest.owo.ui.util;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.parsing.UIModelLoader;
import net.minecraft.TextFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Text;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ApiStatus.Internal
public class UIErrorToast implements Toast {

    private final List<FormattedCharSequence> errorMessage;
    private final Font textRenderer;
    private final int width;

    public UIErrorToast(Throwable error) {
        this.textRenderer = Minecraft.getInstance().font;
        var texts = this.initText(String.valueOf(error.getMessage()), (consumer) -> {
            var stackTop = error.getStackTrace()[0];
            var errorLocation = stackTop.getClassName().split("\\.");

            consumer.accept(Text.literal("Type: ").withStyle(TextFormatting.RED)
                    .append(Text.literal(error.getClass().getSimpleName()).withStyle(TextFormatting.GRAY)));
            consumer.accept(Text.literal("Thrown by: ").withStyle(TextFormatting.RED)
                    .append(Text.literal(errorLocation[errorLocation.length - 1] + ":" + stackTop.getLineNumber()).withStyle(TextFormatting.GRAY)));
        });

        this.width = Math.min(240, TextOps.width(textRenderer, texts) + 8);
        this.errorMessage = this.wrap(texts);
    }

    public UIErrorToast(String message) {
        this.textRenderer = Minecraft.getInstance().font;
        var texts = this.initText(message, (consumer) -> {
            consumer.accept(Text.literal("No context provided").withStyle(TextFormatting.GRAY));
        });
        this.width = Math.min(240, TextOps.width(textRenderer, texts) + 8);
        this.errorMessage = this.wrap(texts);
    }

    public static void report(String message) {
        logErrorsDuringInitialLoad();
        Minecraft.getInstance().getToasts().addToast(new UIErrorToast(message));
    }

    public static void report(Throwable error) {
        logErrorsDuringInitialLoad();
        Minecraft.getInstance().getToasts().addToast(new UIErrorToast(error));
    }

    private static void logErrorsDuringInitialLoad() {
        if (UIModelLoader.hasCompletedInitialLoad()) return;

        var throwable = new Throwable();
        Owo.LOGGER.error(
                "An owo-ui error has occurred during the initial resource reload (on thread {}). This is likely a bug caused by *some* other mod initializing an owo-config screen significantly too early - please report it at https://github.com/wisp-forest/owo-lib/issues",
                Thread.currentThread().getName(),
                throwable
        );
    }

    @Override
    public Visibility render(GuiGraphics context, ToastComponent manager, long startTime) {
        var owoContext = OwoUIDrawContext.of(context);

        owoContext.fill(0, 0, this.width(), this.height(), 0x77000000);
        owoContext.drawRectOutline(0, 0, this.width(), this.height(), 0xA7FF0000);

        int xOffset = this.width() / 2 - this.textRenderer.width(this.errorMessage.get(0)) / 2;
        owoContext.drawShadowedText(this.textRenderer, this.errorMessage.get(0), 4 + xOffset, 4, 0xFFFFFF);

        for (int i = 1; i < this.errorMessage.size(); i++) {
            owoContext.drawText(this.textRenderer, this.errorMessage.get(i), 4, 4 + i * 11, 0xFFFFFF, false);
        }

        return startTime > 10000 ? Visibility.HIDE : Visibility.SHOW;
    }


    @Override
    public int height() {
        return 6 + this.errorMessage.size() * 11;
    }

    @Override
    public int width() {
        return this.width;
    }

    private List<Text> initText(String errorMessage, Consumer<Consumer<Text>> contextAppender) {
        final var texts = new ArrayList<Text>();
        texts.add(Text.literal("owo-ui error").withStyle(TextFormatting.RED));

        texts.add(Text.literal(" "));
        contextAppender.accept(texts::add);
        texts.add(Text.literal(" "));

        texts.add(Text.literal(errorMessage));

        texts.add(Text.literal(" "));
        texts.add(Text.literal("Check your log for details").withStyle(TextFormatting.GRAY));

        return texts;
    }

    private List<FormattedCharSequence> wrap(List<Text> message) {
        var list = new ArrayList<FormattedCharSequence>();
        for (var text : message) list.addAll(this.textRenderer.wrapLines(text, this.width() - 8));
        return list;
    }

    @Override
    public Object getToken() {
        return Type.VERY_TYPE;
    }

    enum Type {
        VERY_TYPE
    }
}
