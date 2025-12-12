package io.wispforest.owo.ui.component;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.Owo;
import io.wispforest.owo.mixin.ui.access.BlockEntityAccessor;
import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.renderstate.BlockElementRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

public class BlockComponent extends BaseUIComponent {

    private final BlockState state;
    private final @Nullable BlockEntity entity;

    protected BlockComponent(BlockState state, @Nullable BlockEntity entity) {
        this.state = state;
        this.entity = entity;
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        BlockEntityRenderState entity = null;
        if (this.entity != null) {
            var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(this.entity);
            if (renderer != null) {
                entity = renderer.createRenderState();

                renderer.extractRenderState(
                    this.entity, entity, partialTicks, Vec3.ZERO, null
                );
            }
        }

        graphics.guiRenderState.submitPicturesInPictureState(new BlockElementRenderState(
            this.state,
            entity,
            new ScreenRectangle(this.x, this.y, this.width, this.height),
            graphics.scissorStack.peek()
        ));
    }

    protected static void prepareBlockEntity(BlockState state, BlockEntity blockEntity, @Nullable CompoundTag nbt) {
        if (blockEntity == null) return;

        var world = Minecraft.getInstance().level;

        ((BlockEntityAccessor) blockEntity).owo$setBlockState(state);
        blockEntity.setLevel(world);

        if (nbt == null) return;

        final var nbtCopy = nbt.copy();

        nbtCopy.putInt("x", 0);
        nbtCopy.putInt("y", 0);
        nbtCopy.putInt("z", 0);

        blockEntity.loadWithComponents(TagValueInput.create(new ProblemReporter.ScopedCollector(Owo.LOGGER), world.registryAccess(), nbtCopy));
    }

    public static BlockComponent parse(Element element) {
        UIParsing.expectAttributes(element, "state");

        try {
            var result = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, element.getAttribute("state"), true);
            return UIComponents.block(result.blockState(), result.nbt());
        } catch (CommandSyntaxException cse) {
            throw new UIModelParsingException("Invalid block state", cse);
        }
    }
}
