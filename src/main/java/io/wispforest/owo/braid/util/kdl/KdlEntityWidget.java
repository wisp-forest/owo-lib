package io.wispforest.owo.braid.util.kdl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.endec.Endec;
import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.AppState;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.object.EntityWidget;
import net.minecraft.IdentifierException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.TagValueInput;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

public class KdlEntityWidget extends StatefulWidget {

    public final double scale;
    public final EntitySpec spec;

    public final EntityWidget.DisplayMode mode;
    public final boolean scaleToFit;
    public final boolean showNametag;

    public KdlEntityWidget(double scale, EntitySpec spec, EntityWidget.DisplayMode mode, boolean scaleToFit, boolean showNametag) {
        this.scale = scale;
        this.spec = spec;
        this.mode = mode;
        this.scaleToFit = scaleToFit;
        this.showNametag = showNametag;
    }

    @Override
    public WidgetState<KdlEntityWidget> createState() {
        return new State();
    }

    public static class State extends WidgetState<KdlEntityWidget> {

        private Entity entity;

        @Override
        public void init() {
            this.recreateEntity();
        }

        @Override
        public void didUpdateWidget(KdlEntityWidget oldWidget) {
            if (!this.widget().spec.equals(oldWidget.spec)) {
                this.recreateEntity();
            }
        }

        private void recreateEntity() {
            var level = AppState.of(this.context()).client().level;

            var entity = this.widget().spec.type.create(level, EntitySpawnReason.LOAD);
            if (this.widget().spec.nbt != null) {
                entity.load(TagValueInput.create(new ProblemReporter.ScopedCollector(Owo.LOGGER), level.registryAccess(), this.widget().spec.nbt));
            }

            this.setState(() -> {
                this.entity = entity;
            });
        }

        @Override
        public Widget build(BuildContext context) {
            return new EntityWidget(
                this.widget().scale,
                this.entity,
                widget -> widget
                    .displayMode(this.widget().mode)
                    .scaleToFit(this.widget().scaleToFit)
                    .showNametag(this.widget().showNametag)
            );
        }
    }

    public record EntitySpec(EntityType<?> type, @Nullable CompoundTag nbt) {
        public static final Endec<EntitySpec> STRING_ENDEC = Endec.STRING.xmap(
            s -> {
                try {
                    CompoundTag nbt = null;

                    int nbtIndex = s.indexOf('{');
                    if (nbtIndex != -1) {

                        nbt = TagParser.parseCompoundAsArgument(new StringReader(s.substring(nbtIndex)));
                        s = s.substring(0, nbtIndex);
                    }

                    var entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(s)).orElseThrow();
                    return new EntitySpec(entityType, nbt);
                } catch (CommandSyntaxException | NoSuchElementException | IdentifierException e) {
                    throw new IllegalStateException("invalid entity: " + s, e);
                }
            },
            spec -> { throw new UnsupportedOperationException("cannot serialize an entity spec to a string"); }
        );
    }
}
