package io.wispforest.owo.mixin;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {
    @Accessor("building") boolean isBuilding();

    @Accessor("format") VertexFormat getFormat();

    @Accessor("drawMode") VertexFormat.DrawMode getDrawMode();
}
