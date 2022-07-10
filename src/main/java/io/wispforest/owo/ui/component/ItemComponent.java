package io.wispforest.owo.ui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import org.w3c.dom.Element;

import java.util.Map;

public class ItemComponent extends BaseComponent {

    protected final VertexConsumerProvider.Immediate entityBuffers;
    protected final ItemRenderer itemRenderer;
    protected ItemStack stack;

    public ItemComponent(ItemStack stack) {
        this.entityBuffers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        this.itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        this.stack = stack;
    }

    @Override
    protected void applyHorizontalContentSizing(Sizing sizing) {
        this.width = 16;
    }

    @Override
    protected void applyVerticalContentSizing(Sizing sizing) {
        this.height = 16;
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        final boolean notSideLit = !this.itemRenderer.getModel(this.stack, null, null, 0).isSideLit();
        if (notSideLit) {
            DiffuseLighting.disableGuiDepthLighting();
        }

        var modelView = RenderSystem.getModelViewStack();
        modelView.push();

        // Translate to the root of the component
        modelView.translate(x, y, 100);

        // Scale according to component size and translate to the center
        modelView.scale(this.width / 16f, this.height / 16f, 1);
        modelView.translate(8.0, 8.0, 0.0);

        // Vanilla scaling and y inversion
        modelView.scale(16, -16, 16);
        RenderSystem.applyModelViewMatrix();

        this.itemRenderer.renderItem(this.stack, ModelTransformation.Mode.GUI, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, new MatrixStack(), entityBuffers, 0);
        this.entityBuffers.draw();

        // Clean up
        modelView.pop();
        RenderSystem.applyModelViewMatrix();

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

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "item", UIParsing::parseIdentifier, itemId -> {
            var item = Registry.ITEM.getOrEmpty(itemId).orElseThrow(() -> new UIModelParsingException("Unknown item " + itemId));
            this.stack(item.getDefaultStack());
        });
    }
}
