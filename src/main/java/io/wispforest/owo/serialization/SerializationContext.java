package io.wispforest.owo.serialization;

import java.util.*;

public class SerializationContext {

    private static final SerializationContext EMPTY = new SerializationContext(Map.of(), Set.of());

    private final Map<SerializationAttribute, Object> attributeValues;
    private final Set<SerializationAttribute> suppressedAttributes;

    private SerializationContext(Map<SerializationAttribute, Object> attributeValues, Set<SerializationAttribute> suppressedAttributes) {
        this.attributeValues = Collections.unmodifiableMap(attributeValues);
        this.suppressedAttributes = Collections.unmodifiableSet(suppressedAttributes);
    }

    public static SerializationContext empty() {
        return EMPTY;
    }

    public static SerializationContext attributes(SerializationAttribute.Instance... attributes) {
        if (attributes.length == 0) return EMPTY;
        return new SerializationContext(unpackAttributes(attributes), Set.of());
    }

    public static SerializationContext suppressed(SerializationAttribute... attributes) {
        if (attributes.length == 0) return EMPTY;
        return new SerializationContext(Map.of(), Set.of(attributes));
    }

    public SerializationContext withAttributes(SerializationAttribute.Instance... attributes) {
        var newAttributes = unpackAttributes(attributes);
        this.attributeValues.forEach((attribute, value) -> {
            if (!newAttributes.containsKey(attribute)) {
                newAttributes.put(attribute, value);
            }
        });

        return new SerializationContext(newAttributes, this.suppressedAttributes);
    }

    public SerializationContext withoutAttributes(SerializationAttribute... attributes) {
        var newAttributes = new HashMap<>(this.attributeValues);
        for (var attribute : attributes) {
            newAttributes.remove(attribute);
        }

        return new SerializationContext(newAttributes, this.suppressedAttributes);
    }

    public SerializationContext withSuppressed(SerializationAttribute... attributes) {
        var newSuppressed = new HashSet<SerializationAttribute>(this.suppressedAttributes);
        newSuppressed.addAll(Arrays.asList(attributes));

        return new SerializationContext(this.attributeValues, newSuppressed);
    }

    public SerializationContext withoutSuppressed(SerializationAttribute... attributes) {
        var newSuppressed = new HashSet<>(this.suppressedAttributes);
        for (var attribute : attributes) {
            newSuppressed.remove(attribute);
        }

        return new SerializationContext(this.attributeValues, newSuppressed);
    }

    public SerializationContext and(SerializationContext other) {
        var newAttributeValues = new HashMap<>(this.attributeValues);
        newAttributeValues.putAll(other.attributeValues);

        var newSuppressed = new HashSet<>(this.suppressedAttributes);
        newSuppressed.addAll(other.suppressedAttributes);

        return new SerializationContext(newAttributeValues, newSuppressed);
    }

    public boolean hasAttribute(SerializationAttribute attribute) {
        return this.attributeValues.containsKey(attribute) && !this.suppressedAttributes.contains(attribute);
    }

    @SuppressWarnings("unchecked")
    public <A> A getAttributeValue(SerializationAttribute.WithValue<A> attribute) {
        return (A) this.attributeValues.get(attribute);
    }

    public <A> A requireAttributeValue(SerializationAttribute.WithValue<A> attribute) {
        if (!this.hasAttribute(attribute)) {
            throw new IllegalStateException("Context did not provide a value for attribute '" + attribute.name + "'");
        }

        return this.getAttributeValue(attribute);
    }

    private static Map<SerializationAttribute, Object> unpackAttributes(SerializationAttribute.Instance[] attributes) {
        var attributeValues = new HashMap<SerializationAttribute, Object>();
        for (var instance : attributes) {
            attributeValues.put(instance.attribute(), instance.value());
        }

        return attributeValues;
    }
}
