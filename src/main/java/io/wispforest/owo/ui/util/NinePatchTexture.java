package io.wispforest.owo.ui.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.PositionedRectangle;
import io.wispforest.owo.ui.core.Size;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class NinePatchTexture {

    private final Identifier texture;
    private final int u, v;
    private final Size cornerPatchSize;
    private final Size centerPatchSize;
    private final Size textureSize;
    private final boolean repeat;

    public NinePatchTexture(Identifier texture, int u, int v, Size cornerPatchSize, Size centerPatchSize, Size textureSize, boolean repeat) {
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.textureSize = textureSize;
        this.cornerPatchSize = cornerPatchSize;
        this.centerPatchSize = centerPatchSize;
        this.repeat = repeat;
    }

    public NinePatchTexture(Identifier texture, int u, int v, Size patchSize, Size textureSize, boolean repeat) {
        this(texture, u, v, patchSize, patchSize, textureSize, repeat);
    }

    public NinePatchTexture(Identifier texture, Size patchSize, Size textureSize, boolean repeat) {
        this(texture, 0, 0, patchSize, textureSize, repeat);
    }

    public void draw(OwoUIDrawContext context, PositionedRectangle rectangle) {
        this.draw(context, rectangle.x(), rectangle.y(), rectangle.width(), rectangle.height());
    }

    public void draw(OwoUIDrawContext context, int x, int y, int width, int height) {
        context.recordQuads();

        int rightEdge = this.cornerPatchSize.width() + this.centerPatchSize.width();
        int bottomEdge = this.cornerPatchSize.height() + this.centerPatchSize.height();

        context.drawTexture(this.texture, x, y, this.u, this.v, this.cornerPatchSize.width(), this.cornerPatchSize.height(), this.textureSize.width(), this.textureSize.height());
        context.drawTexture(this.texture, x + width - this.cornerPatchSize.width(), y, this.u + rightEdge, this.v, this.cornerPatchSize.width(), this.cornerPatchSize.height(), this.textureSize.width(), this.textureSize.height());
        context.drawTexture(this.texture, x, y + height - this.cornerPatchSize.height(), this.u, this.v + bottomEdge, this.cornerPatchSize.width(), this.cornerPatchSize.height(), this.textureSize.width(), this.textureSize.height());
        context.drawTexture(this.texture, x + width - this.cornerPatchSize.width(), y + height - this.cornerPatchSize.height(), this.u + rightEdge, this.v + bottomEdge, this.cornerPatchSize.width(), this.cornerPatchSize.height(), this.textureSize.width(), this.textureSize.height());

        if (this.repeat) {
            this.drawRepeated(context, x, y, width, height);
        } else {
            this.drawStretched(context, x, y, width, height);
        }
        context.submitQuads();
    }

    protected void drawStretched(OwoUIDrawContext context, int x, int y, int width, int height) {
        int doubleCornerHeight = this.cornerPatchSize.height() * 2;
        int doubleCornerWidth = this.cornerPatchSize.width() * 2;

        int rightEdge = this.cornerPatchSize.width() + this.centerPatchSize.width();
        int bottomEdge = this.cornerPatchSize.height() + this.centerPatchSize.height();

        if (width > doubleCornerWidth && height > doubleCornerHeight) {
            context.drawTexture(this.texture, x + this.cornerPatchSize.width(), y + this.cornerPatchSize.height(), width - doubleCornerWidth, height - doubleCornerHeight, this.u + this.cornerPatchSize.width(), this.v + this.cornerPatchSize.height(), this.centerPatchSize.width(), this.centerPatchSize.height(), this.textureSize.width(), this.textureSize.height());
        }

        if (width > doubleCornerWidth) {
            context.drawTexture(this.texture, x + this.cornerPatchSize.width(), y, width - doubleCornerWidth, this.cornerPatchSize.height(), this.u + this.cornerPatchSize.width(), this.v, this.centerPatchSize.width(), this.cornerPatchSize.height(), this.textureSize.width(), this.textureSize.height());
            context.drawTexture(this.texture, x + this.cornerPatchSize.width(), y + height - this.cornerPatchSize.height(), width - doubleCornerWidth, this.cornerPatchSize.height(), this.u + this.cornerPatchSize.width(), this.v + bottomEdge, this.centerPatchSize.width(), this.cornerPatchSize.height(), this.textureSize.width(), this.textureSize.height());
        }

        if (height > doubleCornerHeight) {
            context.drawTexture(this.texture, x, y + this.cornerPatchSize.height(), this.cornerPatchSize.width(), height - doubleCornerHeight, this.u, this.v + this.cornerPatchSize.height(), this.cornerPatchSize.width(), this.centerPatchSize.height(), this.textureSize.width(), this.textureSize.height());
            context.drawTexture(this.texture, x + width - this.cornerPatchSize.width(), y + this.cornerPatchSize.height(), this.cornerPatchSize.width(), height - doubleCornerHeight, this.u + rightEdge, this.v + this.cornerPatchSize.height(), this.cornerPatchSize.width(), this.centerPatchSize.height(), this.textureSize.width(), this.textureSize.height());
        }
    }

    protected void drawRepeated(OwoUIDrawContext context, int x, int y, int width, int height) {
        int doubleCornerHeight = this.cornerPatchSize.height() * 2;
        int doubleCornerWidth = this.cornerPatchSize.width() * 2;

        int rightEdge = this.cornerPatchSize.width() + this.centerPatchSize.width();
        int bottomEdge = this.cornerPatchSize.height() + this.centerPatchSize.height();

        if (width > doubleCornerWidth && height > doubleCornerHeight) {
            int leftoverHeight = height - doubleCornerHeight;
            while (leftoverHeight > 0) {
                int drawHeight = Math.min(this.centerPatchSize.height(), leftoverHeight);

                int leftoverWidth = width - doubleCornerWidth;
                while (leftoverWidth > 0) {
                    int drawWidth = Math.min(this.centerPatchSize.width(), leftoverWidth);
                    context.drawTexture(this.texture, x + this.cornerPatchSize.width() + leftoverWidth - drawWidth, y + this.cornerPatchSize.height() + leftoverHeight - drawHeight, drawWidth, drawHeight, this.u + this.cornerPatchSize.width() + this.centerPatchSize.width() - drawWidth, this.v + this.cornerPatchSize.height() + this.centerPatchSize.height() - drawHeight, drawWidth, drawHeight, this.textureSize.width(), this.textureSize.height());

                    leftoverWidth -= this.centerPatchSize.width();
                }
                leftoverHeight -= this.centerPatchSize.height();
            }
        }

        if (width > doubleCornerWidth) {
            int leftoverWidth = width - doubleCornerWidth;
            while (leftoverWidth > 0) {
                int drawWidth = Math.min(this.centerPatchSize.width(), leftoverWidth);

                context.drawTexture(this.texture, x + this.cornerPatchSize.width() + leftoverWidth - drawWidth, y, drawWidth, this.cornerPatchSize.height(), this.u + this.cornerPatchSize.width() + this.centerPatchSize.width() - drawWidth, this.v, drawWidth, this.cornerPatchSize.height(), this.textureSize.width(), this.textureSize.height());
                context.drawTexture(this.texture, x + this.cornerPatchSize.width() + leftoverWidth - drawWidth, y + height - this.cornerPatchSize.height(), drawWidth, this.cornerPatchSize.height(), this.u + this.cornerPatchSize.width() + this.centerPatchSize.width() - drawWidth, this.v + bottomEdge, drawWidth, this.cornerPatchSize.height(), this.textureSize.width(), this.textureSize.height());

                leftoverWidth -= this.centerPatchSize.width();
            }
        }

        if (height > doubleCornerHeight) {
            int leftoverHeight = height - doubleCornerHeight;
            while (leftoverHeight > 0) {
                int drawHeight = Math.min(this.centerPatchSize.height(), leftoverHeight);
                context.drawTexture(this.texture, x, y + this.cornerPatchSize.height() + leftoverHeight - drawHeight, this.cornerPatchSize.width(), drawHeight, this.u, this.v + this.cornerPatchSize.height() + this.centerPatchSize.height() - drawHeight, this.cornerPatchSize.width(), drawHeight, this.textureSize.width(), this.textureSize.height());
                context.drawTexture(this.texture, x + width - this.cornerPatchSize.width(), y + this.cornerPatchSize.height() + leftoverHeight - drawHeight, this.cornerPatchSize.width(), drawHeight, this.u + rightEdge, this.v + this.cornerPatchSize.height() + this.centerPatchSize.height() - drawHeight, this.cornerPatchSize.width(), drawHeight, this.textureSize.width(), this.textureSize.height());

                leftoverHeight -= this.centerPatchSize.height();
            }
        }
    }

    public static void draw(Identifier texture, OwoUIDrawContext context, int x, int y, int width, int height) {
        ifPresent(texture, ninePatchTexture -> ninePatchTexture.draw(context, x, y, width, height));
    }

    public static void draw(Identifier texture, OwoUIDrawContext context, PositionedRectangle rectangle) {
        ifPresent(texture, ninePatchTexture -> ninePatchTexture.draw(context, rectangle));
    }

    private static void ifPresent(Identifier texture, Consumer<NinePatchTexture> action) {
        if (!MetadataLoader.LOADED_TEXTURES.containsKey(texture)) return;
        action.accept(MetadataLoader.LOADED_TEXTURES.get(texture));
    }

    public static class MetadataLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {

        private static final Map<Identifier, NinePatchTexture> LOADED_TEXTURES = new HashMap<>();

        public MetadataLoader() {
            super(new Gson(), "nine_patch_textures");
        }

        @Override
        public Identifier getFabricId() {
            return new Identifier("owo", "nine_patch_metadata");
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
            prepared.forEach((resourceId, jsonElement) -> {
                if (!(jsonElement instanceof JsonObject object)) return;

                var texture = new Identifier(JsonHelper.getString(object, "texture"));
                var textureSize = Size.of(JsonHelper.getInt(object, "texture_width"), JsonHelper.getInt(object, "texture_height"));

                int u = JsonHelper.getInt(object, "u", 0), v = JsonHelper.getInt(object, "v", 0);
                boolean repeat = JsonHelper.getBoolean(object, "repeat");

                if (object.has("corner_patch_size")) {
                    var cornerPatchObject = JsonHelper.getObject(object, "corner_patch_size");
                    var centerPatchObject = JsonHelper.getObject(object, "center_patch_size");

                    var cornerPatchSize = Size.of(JsonHelper.getInt(cornerPatchObject, "width"), JsonHelper.getInt(cornerPatchObject, "height"));
                    var centerPatchSize = Size.of(JsonHelper.getInt(centerPatchObject, "width"), JsonHelper.getInt(centerPatchObject, "height"));

                    LOADED_TEXTURES.put(resourceId, new NinePatchTexture(texture, u, v, cornerPatchSize, centerPatchSize, textureSize, repeat));
                } else {
                    var patchSizeObject = JsonHelper.getObject(object, "patch_size");
                    var patchSize = Size.of(JsonHelper.getInt(patchSizeObject, "width"), JsonHelper.getInt(patchSizeObject, "height"));

                    LOADED_TEXTURES.put(resourceId, new NinePatchTexture(texture, u, v, patchSize, textureSize, repeat));
                }
            });
        }
    }

}
