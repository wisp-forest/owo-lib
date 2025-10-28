package io.wispforest.owo.mixin.registry;

import com.mojang.serialization.Lifecycle;
import io.wispforest.owo.util.OwoFreezer;
import io.wispforest.owo.util.pond.OwoSimpleRegistryExtensions;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements MutableRegistry<T>, OwoSimpleRegistryExtensions<T> {

    @Shadow private Map<T, RegistryEntry.Reference<T>> intrusiveValueToEntry;
    @Shadow @Final private Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry;
    @Shadow @Final private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;
    @Shadow @Final private Map<T, RegistryEntry.Reference<T>> valueToEntry;
    @Shadow @Final private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;
    @Shadow @Final private Reference2IntMap<T> entryToRawId;
    @Shadow @Final private Map<RegistryKey<T>, RegistryEntryInfo> keyToEntryInfo;
    @Shadow private Lifecycle lifecycle;

    //--

    /**
     * Copy of the {@link SimpleRegistry#add} function but uses {@link List#set} instead of {@link List#add} for {@link SimpleRegistry#rawIdToEntry}
     */
    public RegistryEntry.Reference<T> owo$set(int id, RegistryKey<T> arg, T object, RegistryEntryInfo arg2) {
        this.valueToEntry.remove(object);

        OwoFreezer.checkRegister("Registry Set Calls"); //this.assertNotFrozen(arg);

        Objects.requireNonNull(arg);
        Objects.requireNonNull(object);

        RegistryEntry.Reference<T> reference;

        if (this.intrusiveValueToEntry != null) {
            reference = this.intrusiveValueToEntry.remove(object);

            if (reference == null) {
                throw new AssertionError("Missing intrusive holder for " + arg + ":" + object);
            }

            ((ReferenceAccessor<T>) reference).owo$setRegistryKey(arg);
        } else {
            reference = this.keyToEntry.computeIfAbsent(arg, k -> RegistryEntry.Reference.standAlone(this.getEntryOwner(), k));
            ((ReferenceAccessor<T>) reference).owo$setValue((T)object);
        }

        this.keyToEntry.put(arg, reference);
        this.idToEntry.put(arg.getValue(), reference);
        this.valueToEntry.put(object, reference);
        this.rawIdToEntry.set(id, reference);
        this.entryToRawId.put(object, id);
        this.keyToEntryInfo.put(arg, arg2);
        this.lifecycle = this.lifecycle.add(arg2.lifecycle());

        // TODO: SHOULD WE BE REFIREING THE EVENT?
        RegistryEntryAddedCallback.event(this).invoker().onEntryAdded(id, arg.getValue(), (T)object);

        return reference;
    }
}
