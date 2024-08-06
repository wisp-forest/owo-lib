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

    @Shadow private Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;
    @Shadow @Final private Map<ResourceKey<T>, Holder.Reference<T>> byKey;
    @Shadow @Final private Map<Identifier, Holder.Reference<T>> byId;
    @Shadow @Final private Map<T, Holder.Reference<T>> byValue;
    @Shadow @Final private ObjectList<Holder.Reference<T>> byRawId;
    @Shadow @Final private Reference2IntMap<T> toRawId;
    @Shadow @Final private Map<ResourceKey<T>, RegistrationInfo> registrationInfos;
    @Shadow private Lifecycle registryLifecycle;

    //--

    /**
     * Copy of the {@link MappedRegistry#register} function but uses {@link List#set} instead of {@link List#add} for {@link MappedRegistry#byRawId}
     */
    public Holder.Reference<T> owo$set(int id, ResourceKey<T> arg, T object, RegistrationInfo arg2) {
        this.byValue.remove(object);

        OwoFreezer.checkRegister("Registry Set Calls"); //this.assertNotFrozen(arg);

        Objects.requireNonNull(arg);
        Objects.requireNonNull(object);

        Holder.Reference<T> reference;

        if (this.unregisteredIntrusiveHolders != null) {
            reference = this.unregisteredIntrusiveHolders.remove(object);

            if (reference == null) {
                throw new AssertionError("Missing intrusive holder for " + arg + ":" + object);
            }

            ((ReferenceAccessor<T>) reference).owo$setRegistryKey(arg);
        } else {
            reference = this.byKey.computeIfAbsent(arg, k -> Holder.Reference.createStandAlone(this.holderOwner(), k));
            ((ReferenceAccessor<T>) reference).owo$setValue((T)object);
        }

        this.byKey.put(arg, reference);
        this.byId.put(arg.value(), reference);
        this.byValue.put(object, reference);
        this.byRawId.set(id, reference);
        this.toRawId.put(object, id);
        this.registrationInfos.put(arg, arg2);
        this.registryLifecycle = this.registryLifecycle.add(arg2.lifecycle());

        // TODO: SHOULD WE BE REFIREING THE EVENT?
        RegistryEntryAddedCallback.event(this).invoker().onEntryAdded(id, arg.value(), (T)object);

        return reference;
    }
}
