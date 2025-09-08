package io.wispforest.owo.itemgroup.core;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.owo.itemgroup.base.Icon;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

import static io.wispforest.owo.itemgroup.base.Icon.*;

public class IconEndec {
    private static final Map<Identifier, StructEndec<? extends Icon>> ENDECS = new HashMap<>();

    public static final StructEndec<Icon> ENDEC = Endec.dispatchedStruct(IconEndec::getEndec, Icon::type, MinecraftEndecs.identifierEndec("owo"), "type");

    public static <T extends Icon> void registerEndec(Identifier id, StructEndec<T> endec) {
        if (ENDECS.containsKey(id)) throw new IllegalStateException("Unable to register the given Icon endec as the given Identifier has already been used!");

        ENDECS.put(id, endec);
    }

    public static <T extends Icon> StructEndec<T> getEndec(Identifier id) {
        var endec = ENDECS.get(id);

        if (endec == null) throw new IllegalStateException("Unable to get needed Icon Endec for the following id: " + id);

        return (StructEndec<T>) endec;
    }

    static {
        registerEndec(ItemIcon.TYPE, ItemIcon.ENDEC);
        registerEndec(TextureIcon.TYPE, TextureIcon.ENDEC);
        registerEndec(AnimatedTextureIcon.TYPE, AnimatedTextureIcon.ENDEC);
    }
}
