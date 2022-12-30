package io.wispforest.owo.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class HsvProgram extends GlProgram {

    public static final HsvProgram INSTANCE = new HsvProgram();

    private HsvProgram() {
        super(new Identifier("owo", "spectrum"), VertexFormats.POSITION_COLOR);
    }

    public void use() {
        RenderSystem.setShader(() -> this.backingProgram);
    }

    public static void init() {}
}
