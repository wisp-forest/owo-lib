package io.wispforest.owo.serialization;

import io.wispforest.endec.SerializationAttribute;
import io.wispforest.owo.mixin.serialization.CachedRegistryInfoGetterAccessor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RegistriesAttribute implements SerializationAttribute.Instance {

    public static final SerializationAttribute.WithValue<RegistriesAttribute> REGISTRIES = SerializationAttribute.withValue("registries");

    private final RegistryOps.RegistryInfoLookup infoLookup;
    private final @Nullable RegistryAccess registryAccess;

    private RegistriesAttribute(RegistryOps.RegistryInfoLookup infoLookup, @Nullable RegistryAccess registryAccess) {
        this.infoLookup = infoLookup;
        this.registryAccess = registryAccess;
    }

    public static RegistriesAttribute of(RegistryAccess registryAccess) {
        return new RegistriesAttribute(
                new RegistryOps.HolderLookupAdapter(registryAccess),
                registryAccess
        );
    }

    @ApiStatus.Internal
    public static RegistriesAttribute tryFromCachedInfoGetter(RegistryOps.RegistryInfoLookup lookup) {
        return (lookup instanceof RegistryOps.HolderLookupAdapter cachedGetter)
                ? fromCachedInfoGetter(cachedGetter)
                : fromInfoGetter(lookup);
    }

    public static RegistriesAttribute fromCachedInfoGetter(RegistryOps.HolderLookupAdapter cachedGetter) {
        RegistryAccess registryAccess = null;

        if(((CachedRegistryInfoGetterAccessor) (Object) cachedGetter).owo$getRegistriesLookup() instanceof RegistryAccess drm) {
            registryAccess = drm;
        }

        return new RegistriesAttribute(cachedGetter, registryAccess);
    }

    public static RegistriesAttribute fromInfoGetter(RegistryOps.RegistryInfoLookup lookup) {
        return new RegistriesAttribute(lookup, null);
    }

    public RegistryOps.RegistryInfoLookup infoGetter() {
        return this.infoLookup;
    }

    public boolean hasRegistryAccess() {
        return this.registryAccess != null;
    }

    public @NotNull RegistryAccess registryAccess() {
        if (!this.hasRegistryAccess()) {
            throw new IllegalStateException("This instance of RegistriesAttribute does not supply RegistryAccess");
        }

        return this.registryAccess;
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
