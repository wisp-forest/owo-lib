package io.wispforest.owo.mixin.registry;

import com.mojang.serialization.Lifecycle;
import io.wispforest.owo.util.OwoFreezer;
import io.wispforest.owo.util.pond.OwoSimpleRegistryExtensions;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mixin(MappedRegistry.class)
public abstract class SimpleRegistryMixin<T> implements WritableRegistry<T>, OwoSimpleRegistryExtensions<T> {

    @Shadow private Map<T, Holder.Reference<T>> intrusiveValueToEntry;
    @Shadow @Final private Map<ResourceKey<T>, Holder.Reference<T>> keyToEntry;
    @Shadow @Final private Map<Identifier, Holder.Reference<T>> idToEntry;
    @Shadow @Final private Map<T, Holder.Reference<T>> valueToEntry;
    @Shadow @Final private ObjectList<Holder.Reference<T>> rawIdToEntry;
    @Shadow @Final private Reference2IntMap<T> entryToRawId;
    @Shadow @Final private Map<ResourceKey<T>, RegistrationInfo> keyToEntryInfo;
    @Shadow private Lifecycle lifecycle;

    //--

    /**
     * Copy of the {@link MappedRegistry#register} function but uses {@link List#set} instead of {@link List#add} for {@link MappedRegistry#rawIdToEntry}
     */
    public Holder.Reference<T> owo$set(int id, ResourceKey<T> arg, T object, RegistrationInfo arg2) {
        this.valueToEntry.remove(object);

        OwoFreezer.checkRegister("Registry Set Calls"); //this.assertNotFrozen(arg);

        Objects.requireNonNull(arg);
        Objects.requireNonNull(object);

        Holder.Reference<T> reference;

        if (this.intrusiveValueToEntry != null) {
            reference = this.intrusiveValueToEntry.remove(object);

            if (reference == null) {
                throw new AssertionError("Missing intrusive holder for " + arg + ":" + object);
            }

            ((ReferenceAccessor<T>) reference).owo$setRegistryKey(arg);
        } else {
            reference = this.keyToEntry.computeIfAbsent(arg, k -> Holder.Reference.createStandAlone(this.holderOwner(), k));
            ((ReferenceAccessor<T>) reference).owo$setValue((T)object);
        }

        this.keyToEntry.put(arg, reference);
        this.idToEntry.put(arg.value(), reference);
        this.valueToEntry.put(object, reference);
        this.rawIdToEntry.set(id, reference);
        this.entryToRawId.put(object, id);
        this.keyToEntryInfo.put(arg, arg2);
        this.lifecycle = this.lifecycle.add(arg2.lifecycle());

        // TODO: SHOULD WE BE REFIREING THE EVENT?
        RegistryEntryAddedCallback.event(this).invoker().onEntryAdded(id, arg.value(), (T)object);

        return reference;
    }
}
