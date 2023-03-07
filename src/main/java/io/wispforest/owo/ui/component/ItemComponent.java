package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ItemComponent extends BaseComponent {

    protected final VertexConsumerProvider.Immediate entityBuffers;
    protected final ItemRenderer itemRenderer;
    protected ItemStack stack;
    protected boolean showOverlay = false;

    protected ItemComponent(ItemStack stack) {
        this.entityBuffers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        this.itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        this.stack = stack;
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return 16;
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return 16;
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        final boolean notSideLit = !this.itemRenderer.getModel(this.stack, null, null, 0).isSideLit();
        if (notSideLit) {
            DiffuseLighting.disableGuiDepthLighting();
        }

        matrices.push();

        // Translate to the root of the component
        matrices.translate(x, y, 100);

        // Scale according to component size and translate to the center
        matrices.scale(this.width / 16f, this.height / 16f, 1);
        matrices.translate(8.0, 8.0, 0.0);

        // Vanilla scaling and y inversion
        matrices.scale(16, -16, 16);

        this.itemRenderer.renderItem(this.stack, ModelTransformationMode.GUI, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, matrices, entityBuffers, null, 0);
        this.entityBuffers.draw();

        // Clean up
        matrices.pop();

        if (this.showOverlay) this.itemRenderer.renderGuiItemOverlay(matrices, MinecraftClient.getInstance().textRenderer, this.stack, this.x, this.y);
        if (notSideLit) {
            DiffuseLighting.enableGuiDepthLighting();
        }
    }

    public ItemComponent stack(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    public ItemStack stack() {
        return this.stack;
    }

    public ItemComponent showOverlay(boolean drawOverlay) {
        this.showOverlay = drawOverlay;
        return this;
    }

    public boolean showOverlay() {
        return this.showOverlay;
    }

    /**
     * Obtain the full item stack tooltip, including custom components
     * provided via {@link net.minecraft.item.Item#getTooltipData(ItemStack)}
     *
     * @param stack   The item stack from which to obtain the tooltip
     * @param player  The player to use for context, may be {@code null}
     * @param context The tooltip context - {@code null} to fall back to the default provided by
     *                {@link net.minecraft.client.option.GameOptions#advancedItemTooltips}
     */
    public static List<TooltipComponent> tooltipFromItem(ItemStack stack, @Nullable PlayerEntity player, @Nullable TooltipContext context) {
        if (context == null) {
            context = MinecraftClient.getInstance().options.advancedItemTooltips ? TooltipContext.ADVANCED : TooltipContext.BASIC;
        }

        var tooltip = new ArrayList<TooltipComponent>();
        stack.getTooltip(player, context)
                .stream()
                .map(Text::asOrderedText)
                .map(TooltipComponent::of)
                .forEach(tooltip::add);

        stack.getTooltipData().ifPresent(data -> {
            tooltip.add(1, Objects.requireNonNullElseGet(
                    TooltipComponentCallback.EVENT.invoker().getComponent(data),
                    () -> TooltipComponent.of(data)
            ));
        });

        return tooltip;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "show-overlay", UIParsing::parseBool, this::showOverlay);
        UIParsing.apply(children, "item", UIParsing::parseIdentifier, itemId -> {
            var item = Registries.ITEM.getOrEmpty(itemId).orElseThrow(() -> new UIModelParsingException("Unknown item " + itemId));
            this.stack(item.getDefaultStack());
        });
    }
}
