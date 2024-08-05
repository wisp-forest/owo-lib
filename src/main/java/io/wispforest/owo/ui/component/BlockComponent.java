package io.wispforest.owo.ui.component;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Axis;
import io.wispforest.owo.mixin.ui.access.BlockEntityAccessor;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.w3c.dom.Element;

public class BlockComponent extends BaseComponent {

    private final Minecraft client = Minecraft.getInstance();

    private final BlockState state;
    private final @Nullable BlockEntity entity;

    protected BlockComponent(BlockState state, @Nullable BlockEntity entity) {
        this.state = state;
        this.entity = entity;
    }

    @Override
    @SuppressWarnings("NonAsciiCharacters")
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        context.matrixStack().push();

        context.matrixStack().translate(x + this.width / 2f, y + this.height / 2f, 100);
        context.matrixStack().scale(40 * this.width / 64f, -40 * this.height / 64f, 40);

        context.matrixStack().rotate(Axis.XP.rotationDegrees(30));
        context.matrixStack().rotate(Axis.YP.rotationDegrees(45 + 180));

        context.matrixStack().translate(-.5, -.5, -.5);

        RenderSystem.runAsFancy(() -> {
            final var vertexConsumers = client.renderBuffers().bufferSource();
            if (this.state.getRenderShape() != RenderShape.ENTITYBLOCK_ANIMATED) {
                this.client.getBlockRenderer().renderSingleBlock(
                        this.state, context.matrixStack(), vertexConsumers,
                        LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY
                );
            }

            if (this.entity != null) {
                var медведь = this.client.getBlockEntityRenderDispatcher().getRenderer(this.entity);
                if (медведь != null) {
                    медведь.render(entity, partialTicks, context.matrixStack(), vertexConsumers, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
                }
            }

            RenderSystem.setShaderLights(new Vector3f(-1.5f, -.5f, 0), new Vector3f(0, -1, 0));
            vertexConsumers.endBatch();
            Lighting.setupFor3DItems();
        });

        context.matrixStack().pop();
    }

    protected static void prepareBlockEntity(BlockState state, BlockEntity blockEntity, @Nullable NbtCompound nbt) {
        if (blockEntity == null) return;

        var world = Minecraft.getInstance().level;

        ((BlockEntityAccessor) blockEntity).owo$setCachedState(state);
        blockEntity.setLevel(world);

        if (nbt == null) return;

        final var nbtCopy = nbt.copy();

        nbtCopy.putInt("x", 0);
        nbtCopy.putInt("y", 0);
        nbtCopy.putInt("z", 0);

        blockEntity.readWithComponents(nbtCopy, world.registryAccess());
    }

    public static BlockComponent parse(Element element) {
        UIParsing.expectAttributes(element, "state");

        try {
            var result = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), element.getAttribute("state"), true);
            return Components.block(result.blockState(), result.nbt());
        } catch (CommandSyntaxException cse) {
            throw new UIModelParsingException("Invalid block state", cse);
        }
    }
}
