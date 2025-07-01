package io.wispforest.owo.ui.component;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.Owo;
import io.wispforest.owo.mixin.ui.access.BlockEntityAccessor;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.renderstate.BlockElementRenderState;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.storage.NbtReadView;
import net.minecraft.util.ErrorReporter;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

public class BlockComponent extends BaseComponent {

    private final BlockState state;
    private final @Nullable BlockEntity entity;

    protected BlockComponent(BlockState state, @Nullable BlockEntity entity) {
        this.state = state;
        this.entity = entity;
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        context.state.addSpecialElement(new BlockElementRenderState(
            this.state,
            this.entity,
            new ScreenRect(this.x, this.y, this.width, this.height),
            context.scissorStack.peekLast()
        ));
    }

    protected static void prepareBlockEntity(BlockState state, BlockEntity blockEntity, @Nullable NbtCompound nbt) {
        if (blockEntity == null) return;

        var world = MinecraftClient.getInstance().world;

        ((BlockEntityAccessor) blockEntity).owo$setCachedState(state);
        blockEntity.setWorld(world);

        if (nbt == null) return;

        final var nbtCopy = nbt.copy();

        nbtCopy.putInt("x", 0);
        nbtCopy.putInt("y", 0);
        nbtCopy.putInt("z", 0);

        blockEntity.read(NbtReadView.create(new ErrorReporter.Logging(Owo.LOGGER), world.getRegistryManager(), nbtCopy));
    }

    public static BlockComponent parse(Element element) {
        UIParsing.expectAttributes(element, "state");

        try {
            var result = BlockArgumentParser.block(Registries.BLOCK, element.getAttribute("state"), true);
            return Components.block(result.blockState(), result.nbt());
        } catch (CommandSyntaxException cse) {
            throw new UIModelParsingException("Invalid block state", cse);
        }
    }
}
