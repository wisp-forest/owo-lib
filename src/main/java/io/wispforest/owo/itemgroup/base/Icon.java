package io.wispforest.owo.itemgroup.base;

import com.mojang.datafixers.util.Either;
import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.DispatchedEndec;
import io.wispforest.owo.serialization.IdentifiedData;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

///
/// An icon used for rendering on buttons in {@link OwoItemGroup} with
/// default implementations for [ItemStack], textures, and animated textures
///
public interface Icon extends IdentifiedData {

    @ApiStatus.Internal
    Icon NONE = () -> DispatchedEndec.EMPTY_ID;

    DispatchedEndec<Icon> ENDEC = DispatchedEndec.<Icon>of()
        .idEndec(MinecraftEndecs.identifierEndec("owo"))
        .emptyValue(() -> NONE)
        .allowTypelessData()
        .baseClasses(ItemIcon.class, TextureIcon.class, AnimatedTextureIcon.class)
        .create();

    static Icon of(ItemStack stack) {
        return new ItemIcon(stack);
    }

    static Icon of(ItemConvertible item) {
        return of(new ItemStack(item));
    }

    record ItemIcon(ItemStack stack) implements Icon {
        public static final Identifier ID = Identifier.of("owo", "itemstack");

        public static final StructEndec<ItemIcon> ENDEC = Icon.ENDEC.registerEndec(ID,
            StructEndecBuilder.of(
                CodecUtils.eitherEndec(MinecraftEndecs.ITEM_STACK, MinecraftEndecs.ofRegistry(Registries.ITEM))
                    .xmap(either -> Either.unwrap(either.mapRight(Item::getDefaultStack)), Either::left)
                    .fieldOf("stack", ItemIcon::stack),
                ItemIcon::new
            ));

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    static Icon of(Identifier texture, int u, int v, int textureWidth, int textureHeight) {
        return new TextureIcon(texture, u, v, textureWidth, textureHeight);
    }

    record TextureIcon(Identifier texture, int u, int v, int textureWidth, int textureHeight) implements Icon {
        public static final Identifier ID = Identifier.of("owo", "texture");

        public static final StructEndec<TextureIcon> ENDEC = Icon.ENDEC.registerEndec(ID,
            StructEndecBuilder.of(
                MinecraftEndecs.IDENTIFIER.fieldOf("texture", TextureIcon::texture),
                Endec.INT.fieldOf("u", TextureIcon::u),
                Endec.INT.fieldOf("v", TextureIcon::v),
                Endec.INT.fieldOf("texture_width", TextureIcon::textureWidth),
                Endec.INT.fieldOf("texture_height", TextureIcon::textureHeight),
                TextureIcon::new
            ));

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    /**
     * Creates an Animated ItemGroup Icon
     *
     * @param texture       The texture to render, this is the spritesheet
     * @param textureWidth  The width of the texture
     * @param textureHeight The width of the texture
     * @param frameDelay    The delay in milliseconds between frames
     * @param loop          Whether the given animation should loop or only play once
     * @return The created icon instance
     */
    static Icon of(Identifier texture, int textureWidth, int textureHeight, int frameDelay, boolean loop, boolean reversible) {
        return new AnimatedTextureIcon(texture, textureWidth, textureHeight, frameDelay, loop, reversible);
    }

    record AnimatedTextureIcon(Identifier texture, int textureWidth, int textureHeight, int frameDelay, boolean loop, boolean reversible) implements Icon {
        public static final Identifier ID = Identifier.of("owo", "animated_texture");

        public static final StructEndec<AnimatedTextureIcon> ENDEC = Icon.ENDEC.registerEndec(ID,
            StructEndecBuilder.of(
                MinecraftEndecs.IDENTIFIER.fieldOf("texture", AnimatedTextureIcon::texture),
                Endec.INT.fieldOf("texture_width", AnimatedTextureIcon::textureWidth),
                Endec.INT.fieldOf("texture_height", AnimatedTextureIcon::textureHeight),
                Endec.INT.fieldOf("frame_delay", AnimatedTextureIcon::frameDelay),
                Endec.BOOLEAN.optionalFieldOf("should_loop", AnimatedTextureIcon::loop, true),
                Endec.BOOLEAN.optionalFieldOf("reversible", AnimatedTextureIcon::reversible, false),
                AnimatedTextureIcon::new
            ));

        @Override
        public Identifier getTypeId() {
            return ID;
        }
    }

    Identifier getTypeId();
}
