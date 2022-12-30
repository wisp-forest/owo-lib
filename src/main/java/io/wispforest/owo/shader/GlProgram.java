package io.wispforest.owo.shader;

import io.wispforest.owo.mixin.shader.ShaderProgramAccessor;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class GlProgram {

    private static final List<Pair<Function<ResourceFactory, ShaderProgram>, Consumer<ShaderProgram>>> REGISTERED_PROGRAMS = new ArrayList<>();

    protected ShaderProgram backingProgram;

    protected GlProgram(Identifier id, VertexFormat vertexFormat) {
        REGISTERED_PROGRAMS.add(new Pair<>(
                resourceFactory -> {
                    try {
                        return new ShaderProgram(resourceFactory, id.toString(), vertexFormat);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to initialized owo shader program", e);
                    }
                },
                program -> {
                    this.backingProgram = program;
                    this.loadUniforms();
                }
        ));
    }

    protected void loadUniforms() {};

    protected @Nullable GlUniform findUniform(String name) {
        return ((ShaderProgramAccessor) this.backingProgram).owo$getLoadedUniforms().get(name);
    }

    public static void forEachProgram(Consumer<Pair<Function<ResourceFactory, ShaderProgram>, Consumer<ShaderProgram>>> loader) {
        REGISTERED_PROGRAMS.forEach(loader);
    }
}
