package io.wispforest.owo.ui.component;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.MatrixStack;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.commands.arguments.item.ItemParser.ItemResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Text;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class ItemComponent extends BaseComponent {

    protected static final Matrix4f ITEM_SCALING = new Matrix4f().scaling(16, -16, 16);

    protected final MultiBufferSource.BufferSource entityBuffers;
    protected final ItemRenderer itemRenderer;
    protected ItemStack stack;
    protected boolean showOverlay = false;
    protected boolean setTooltipFromStack = false;

    protected ItemComponent(ItemStack stack) {
        this.entityBuffers = Minecraft.getInstance().renderBuffers().bufferSource();
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
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
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        final boolean notSideLit = !this.itemRenderer.getModel(this.stack, null, null, 0).usesBlockLight();
        if (notSideLit) {
            Lighting.setupForFlatItems();
        }

        var matrices = context.matrixStack();
        matrices.push();

        // Translate to the root of the component
        matrices.translate(this.x, this.y, 100);

        // Scale according to component size and translate to the center
        matrices.scale(this.width / 16f, this.height / 16f, 1);
        matrices.translate(8.0, 8.0, 0.0);

        // Vanilla scaling and y inversion
        if (notSideLit) {
            matrices.scale(16, -16, 16);
        } else {
            matrices.multiply(ITEM_SCALING);
        }

        var client = Minecraft.getInstance();

        this.itemRenderer.renderStatic(this.stack, ItemDisplayContext.GUI, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, matrices, entityBuffers, client.level, 0);
        this.entityBuffers.endBatch();

        // Clean up
        matrices.pop();

        if (this.showOverlay) {
            context.drawItemDecorations(client.font, this.stack, this.x, this.y);
        }
        if (notSideLit) {
            Lighting.setupFor3DItems();
        }
    }

    protected void updateTooltipForStack() {
        if (!this.setTooltipFromStack) return;

        if (!this.stack.isEmpty()) {
            Minecraft client = Minecraft.getInstance();
            this.tooltip(tooltipFromItem(this.stack, Item.TooltipContext.of(client.level), client.player, null));
        } else {
            this.tooltip((List<ClientTooltipComponent>) null);
        }
    }

    public ItemComponent setTooltipFromStack(boolean setTooltipFromStack) {
        this.setTooltipFromStack = setTooltipFromStack;
        this.updateTooltipForStack();

        return this;
    }

    public boolean setTooltipFromStack() {
        return setTooltipFromStack;
    }

    public ItemComponent stack(ItemStack stack) {
        this.stack = stack;
        this.updateTooltipForStack();

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
     * provided via {@link net.minecraft.world.item.Item#getTooltipData(ItemStack)}
     *
     * @param stack   The item stack from which to obtain the tooltip
     * @param context the tooltip context
     * @param player  The player to use for context, may be {@code null}
     * @param type    The tooltip type - {@code null} to fall back to the default provided by
     *                {@link net.minecraft.client.Options#advancedItemTooltips}
     */
    public static List<ClientTooltipComponent> tooltipFromItem(ItemStack stack, Item.TooltipContext context, @Nullable Player player, @Nullable TooltipFlag type) {
        if (type == null) {
            type = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
        }

        var tooltip = new ArrayList<ClientTooltipComponent>();
        stack.getTooltipLines(context, player, type)
                .stream()
                .map(Text::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .forEach(tooltip::add);

        stack.getTooltipData().ifPresent(data -> {
            tooltip.add(1, Objects.requireNonNullElseGet(
                    TooltipComponentCallback.EVENT.invoker().getComponent(data),
                    () -> ClientTooltipComponent.create(data)
            ));
        });

        return tooltip;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "show-overlay", UIParsing::parseBool, this::showOverlay);
        UIParsing.apply(children, "set-tooltip-from-stack", UIParsing::parseBool, this::setTooltipFromStack);

        UIParsing.apply(children, "item", UIParsing::parseIdentifier, itemId -> {
            Owo.debugWarn(Owo.LOGGER, "Deprecated <item> property populated on item component - migrate to <stack> instead");

            var item = BuiltInRegistries.ITEM.getOptional(itemId).orElseThrow(() -> new UIModelParsingException("Unknown item " + itemId));
            this.stack(item.getDefaultInstance());
        });

        UIParsing.apply(children, "stack", $ -> $.getTextContent().strip(), stackString -> {
            try {
                var result = new ItemParser(HolderLookup.Provider.create(Stream.of(BuiltInRegistries.ITEM.asLookup())))
                    .parse(new StringReader(stackString));

                var stack = new ItemStack(result.item());
                stack.applyComponentsAndValidate(result.components());

                this.stack(stack);
            } catch (CommandSyntaxException cse) {
                throw new UIModelParsingException("Invalid item stack", cse);
            }
        });
    }
}
