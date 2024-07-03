package io.wispforest.owo.ui.window;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface WindowIcon {
    static WindowIcon fromResources(Identifier... iconIds) {
        return fromResources(List.of(iconIds));
    }

    static WindowIcon fromResources(List<Identifier> iconIds) {
        return new WindowIcon() {
            @Override
            public List<NativeImage> listIconImages() {
                List<NativeImage> iconImages = new ArrayList<>(iconIds.size());

                for (Identifier iconId : iconIds) {
                    var icon = MinecraftClient.getInstance().getResourceManager().getResource(iconId).orElse(null);

                    if (icon == null) continue;

                    try {
                        iconImages.add(NativeImage.read(icon.getInputStream()));
                    } catch (IOException e) {
                        throw new RuntimeException("Couldn't open icon " + iconId, e);
                    }
                }

                return iconImages;
            }

            @Override
            public boolean closeAfterUse() {
                return true;
            }
        };
    }

    static WindowIcon fromNativeImages(List<NativeImage> icons, boolean closeAfterUse) {
        return new WindowIcon() {
            @Override
            public List<NativeImage> listIconImages() {
                return icons;
            }

            @Override
            public boolean closeAfterUse() {
                return closeAfterUse;
            }
        };
    }

    List<NativeImage> listIconImages();

    boolean closeAfterUse();
}
