package io.wispforest.owo.ui;

import io.wispforest.owo.ops.TextOps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ApiStatus.Internal
public class UIErrorToast implements Toast {

    private final List<OrderedText> errorMessage;
    private final TextRenderer textRenderer;
    private final int width;

    public UIErrorToast(Throwable error) {
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        var texts = this.initText(error.getMessage(), (consumer) -> {
            var stackTop = error.getStackTrace()[0];
            var errorLocation = stackTop.getClassName().split("\\.");

            consumer.accept(Text.literal("Type: ").formatted(Formatting.RED)
                    .append(Text.literal(error.getClass().getSimpleName()).formatted(Formatting.GRAY)));
            consumer.accept(Text.literal("Thrown by: ").formatted(Formatting.RED)
                    .append(Text.literal(errorLocation[errorLocation.length - 1] + ":" + stackTop.getLineNumber()).formatted(Formatting.GRAY)));
        });

        this.width = Math.min(240, TextOps.width(textRenderer, texts) + 8);
        this.errorMessage = this.wrap(texts);
    }

    public UIErrorToast(String message) {
        this.textRenderer = MinecraftClient.getInstance().textRenderer;
        var texts = this.initText(message, (consumer) -> {
            consumer.accept(Text.literal("No context provided").formatted(Formatting.GRAY));
        });
        this.width = Math.min(240, TextOps.width(textRenderer, texts) + 8);
        this.errorMessage = this.wrap(texts);
    }

    public static void report(String message) {
        MinecraftClient.getInstance().getToastManager().add(new UIErrorToast(message));
    }

    public static void report(Throwable error) {
        MinecraftClient.getInstance().getToastManager().add(new UIErrorToast(error));
    }

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        Drawer.fill(matrices, 0, 0, this.getWidth(), this.getHeight(), 0x77000000);
        Drawer.drawRectOutline(matrices, 0, 0, this.getWidth(), this.getHeight(), 0xA7FF0000);

        int xOffset = this.getWidth() / 2 - this.textRenderer.getWidth(this.errorMessage.get(0)) / 2;
        this.textRenderer.drawWithShadow(matrices, this.errorMessage.get(0), 4 + xOffset, 4, 0xFFFFFF);

        for (int i = 1; i < this.errorMessage.size(); i++) {
            this.textRenderer.draw(matrices, this.errorMessage.get(i), 4, 4 + i * 11, 0xFFFFFF);
        }

        return startTime > 10000 ? Visibility.HIDE : Visibility.SHOW;
    }


    @Override
    public int getHeight() {
        return 6 + this.errorMessage.size() * 11;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    private List<Text> initText(String errorMessage, Consumer<Consumer<Text>> contextAppender) {
        final var texts = new ArrayList<Text>();
        texts.add(Text.literal("owo-ui error").formatted(Formatting.RED));

        texts.add(Text.literal(" "));
        contextAppender.accept(texts::add);
        texts.add(Text.literal(" "));

        texts.add(Text.literal(errorMessage));

        texts.add(Text.literal(" "));
        texts.add(Text.literal("Check your log for details").formatted(Formatting.GRAY));

        return texts;
    }

    private List<OrderedText> wrap(List<Text> message) {
        var list = new ArrayList<OrderedText>();
        for (var text : message) list.addAll(this.textRenderer.wrapLines(text, this.getWidth() - 8));
        return list;
    }

    @Override
    public Object getType() {
        return Type.VERY_TYPE;
    }

    enum Type {
        VERY_TYPE
    }
}
