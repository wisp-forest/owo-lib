package io.wispforest.owo.ui.component;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.renderstate.OwoItemElementRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class ItemComponent extends BaseComponent {

    protected final ItemModelManager itemModelManager;
    protected ItemStack stack;
    protected boolean showOverlay = false;
    protected boolean setTooltipFromStack = false;

    protected ItemComponent(ItemStack stack) {
        this.itemModelManager = MinecraftClient.getInstance().getItemModelManager();
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
        var matrices = context.getMatrices();
        matrices.pushMatrix();

        // Translate to the root of the component
        matrices.translate(this.x, this.y);

        // Scale according to component size and translate to the center
        matrices.scale(this.width / 16f, this.height / 16f);

        var client = MinecraftClient.getInstance();

        if (this.width <= 16 && this.height <= 16) {
            context.drawItem(this.stack, 0, 0);
        } else {
            var state = new ItemRenderState();
            this.itemModelManager.update(state, this.stack, ItemDisplayContext.GUI, MinecraftClient.getInstance().world, MinecraftClient.getInstance().player, 0);

            context.state.addSpecialElement(new OwoItemElementRenderState(
                state,
                new ScreenRect(this.x, this.y, this.width, this.height),
                context.scissorStack.peekLast()
            ));
        }

        // Clean up
        matrices.popMatrix();

        if (this.showOverlay) {
            context.drawStackOverlay(client.textRenderer, this.stack, this.x, this.y);
        }
    }

    protected void updateTooltipForStack() {
        if (!this.setTooltipFromStack) return;

        if (!this.stack.isEmpty()) {
            MinecraftClient client = MinecraftClient.getInstance();
            this.tooltip(tooltipFromItem(this.stack, Item.TooltipContext.create(client.world), client.player, null));
        } else {
            this.tooltip((List<TooltipComponent>) null);
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
     * provided via {@link net.minecraft.item.Item#getTooltipData(ItemStack)}
     *
     * @param stack   The item stack from which to obtain the tooltip
     * @param context the tooltip context
     * @param player  The player to use for context, may be {@code null}
     * @param type    The tooltip type - {@code null} to fall back to the default provided by
     *                {@link net.minecraft.client.option.GameOptions#advancedItemTooltips}
     */
    public static List<TooltipComponent> tooltipFromItem(ItemStack stack, Item.TooltipContext context, @Nullable PlayerEntity player, @Nullable TooltipType type) {
        if (type == null) {
            type = MinecraftClient.getInstance().options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC;
        }

        var tooltip = new ArrayList<TooltipComponent>();
        stack.getTooltip(context, player, type)
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
        UIParsing.apply(children, "set-tooltip-from-stack", UIParsing::parseBool, this::setTooltipFromStack);

        UIParsing.apply(children, "item", UIParsing::parseIdentifier, itemId -> {
            Owo.debugWarn(Owo.LOGGER, "Deprecated <item> property populated on item component - migrate to <stack> instead");

            var item = Registries.ITEM.getOptionalValue(itemId).orElseThrow(() -> new UIModelParsingException("Unknown item " + itemId));
            this.stack(item.getDefaultStack());
        });

        UIParsing.apply(children, "stack", $ -> $.getTextContent().strip(), stackString -> {
            try {
                var result = new ItemStringReader(RegistryWrapper.WrapperLookup.of(Stream.of(Registries.ITEM)))
                    .consume(new StringReader(stackString));

                var stack = new ItemStack(result.item());
                stack.applyChanges(result.components());

                this.stack(stack);
            } catch (CommandSyntaxException cse) {
                throw new UIModelParsingException("Invalid item stack", cse);
            }
        });
    }
}
