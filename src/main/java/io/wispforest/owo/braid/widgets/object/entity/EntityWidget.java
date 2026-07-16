package io.wispforest.owo.braid.widgets.object.entity;

import com.google.common.base.Preconditions;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.framework.widget.WidgetSetupCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class EntityWidget extends StatelessWidget {

    private static final Field ENTITY_ID_FIELD;

    static {
        Field f = null;
        try {
            f = net.minecraft.world.entity.Entity.class.getDeclaredField("id");
            f.setAccessible(true);
        } catch (Exception ignored) {}
        ENTITY_ID_FIELD = f;
    }

    public final double scale;
    public final Entity entity;
    public final @Nullable WidgetSetupCallback<EntityRenderStateWidget> setupCallback;

    public EntityWidget(double scale, Entity entity, @Nullable WidgetSetupCallback<EntityRenderStateWidget> setupCallback) {
        this.scale = scale;
        Preconditions.checkNotNull(entity, "The Entity provided to an EntityWidget cannot be null");
        this.entity = entity;
        this.setupCallback = setupCallback;
    }

    public EntityWidget(double scale, Entity entity) {
        this(scale, entity, null);
    }

    @Override
    public Widget build(BuildContext context) {
        assignClientEntityId(this.entity);
        return new EntityRenderStateWidget(
            this.scale,
            //TODO: can we get the client from the appstate?
            () -> Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(this.entity, 0),
            this.setupCallback
        );
    }

    private static void assignClientEntityId(Entity entity) {
        if (ENTITY_ID_FIELD == null) return;
        try {
            if ((int) ENTITY_ID_FIELD.get(entity) == 0) {
                ENTITY_ID_FIELD.set(entity, -1);
            }
        } catch (Exception ignored) {}
    }
}
