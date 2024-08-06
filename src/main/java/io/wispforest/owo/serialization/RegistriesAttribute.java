package io.wispforest.owo.serialization;

import io.wispforest.endec.SerializationAttribute;
import io.wispforest.owo.mixin.HolderLookupAdapterAccessor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RegistriesAttribute implements SerializationAttribute.Instance {

    public static final SerializationAttribute.WithValue<RegistriesAttribute> REGISTRIES = SerializationAttribute.withValue("registries");

    private final RegistryOps.RegistryInfoLookup infoGetter;
    private final @Nullable RegistryAccess registryManager;

    private RegistriesAttribute(RegistryOps.RegistryInfoLookup infoGetter, @Nullable RegistryAccess registryManager) {
        this.infoGetter = infoGetter;
        this.registryManager = registryManager;
    }

    public static RegistriesAttribute of(RegistryAccess registryManager) {
        return new RegistriesAttribute(
                new RegistryOps.HolderLookupAdapter(registryManager),
                registryManager
        );
    }

    @ApiStatus.Internal
    public static RegistriesAttribute tryFromCachedInfoGetter(RegistryOps.RegistryInfoLookup lookup) {
        return (lookup instanceof RegistryOps.HolderLookupAdapter cachedGetter)
                ? fromCachedInfoGetter(cachedGetter)
                : fromInfoGetter(lookup);
    }

    public static RegistriesAttribute fromCachedInfoGetter(RegistryOps.HolderLookupAdapter cachedGetter) {
        RegistryAccess registryManager = null;

        if(((HolderLookupAdapterAccessor) (Object) cachedGetter).owo$getLookupProvider() instanceof RegistryAccess drm) {
            registryManager = drm;
        }

        return new RegistriesAttribute(cachedGetter, registryManager);
    }

    public static RegistriesAttribute fromInfoGetter(RegistryOps.RegistryInfoLookup lookup) {
        return new RegistriesAttribute(lookup, null);
    }

    public RegistryOps.RegistryInfoLookup infoGetter() {
        return this.infoGetter;
    }

    public boolean hasRegistryManager() {
        return this.registryManager != null;
    }

    public @NotNull RegistryAccess registryManager() {
        if (!this.hasRegistryManager()) {
            throw new IllegalStateException("This instance of RegistriesAttribute does not supply a DynamicRegistryManager");
        }

        return this.registryManager;
    }

    @Override
    public SerializationAttribute attribute() {
        return REGISTRIES;
    }

    @Override
    public Object value() {
        return this;
    }
}
