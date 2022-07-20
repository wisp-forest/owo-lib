package io.wispforest.owo.ui.component;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class PlayerComponent extends EntityComponent {

    public PlayerComponent(GameProfile profile) {
        super(new RenderablePlayerEntity(profile));
    }

    @Override
    public ClientPlayerEntity entity() {
        return (ClientPlayerEntity) this.entity;
    }

    protected static class RenderablePlayerEntity extends ClientPlayerEntity {

        protected Identifier skinTextureId = null;
        protected String model = null;

        public RenderablePlayerEntity(GameProfile profile) {
            super(MinecraftClient.getInstance(),
                    MinecraftClient.getInstance().world,
                    new ClientPlayNetworkHandler(MinecraftClient.getInstance(),
                            null,
                            new ClientConnection(NetworkSide.CLIENTBOUND),
                            profile,
                            MinecraftClient.getInstance().createTelemetrySender()
                    ),
                    null, null, false, false
            );

            this.client.getSkinProvider().loadSkin(this.getGameProfile(), (type, identifier, texture) -> {
                if (type != MinecraftProfileTexture.Type.SKIN) return;

                this.skinTextureId = identifier;
                this.model = texture.getMetadata("model");
            }, true);
        }

        @Override
        public boolean hasSkinTexture() {
            return skinTextureId != null;
        }

        @Override
        public Identifier getSkinTexture() {
            return this.skinTextureId != null ? this.skinTextureId : super.getSkinTexture();
        }


        @Override
        public boolean isPartVisible(PlayerModelPart modelPart) {
            return true;
        }

        @Override
        public String getModel() {
            return this.model != null ? this.model : super.getModel();
        }

        @Nullable
        @Override
        protected PlayerListEntry getPlayerListEntry() {
            return null;
        }
    }

}
