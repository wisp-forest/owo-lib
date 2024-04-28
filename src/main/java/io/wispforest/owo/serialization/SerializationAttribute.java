package io.wispforest.owo.serialization;

public sealed abstract class SerializationAttribute permits SerializationAttribute.Marker, SerializationAttribute.WithValue {

    public final String name;
    protected SerializationAttribute(String name) {
        this.name = name;
    }

    public static SerializationAttribute.Marker marker(String name) {
        return new Marker(name);
    }

    public static <T> SerializationAttribute.WithValue<T> withValue(String name) {
        return new WithValue<>(name);
    }

    public static final class Marker extends SerializationAttribute implements Instance {
        private Marker(String name) {
            super(name);
        }

        @Override
        public SerializationAttribute attribute() {
            return this;
        }

        @Override
        public Object value() {
            return null;
        }
    }

    public static final class WithValue<T> extends SerializationAttribute {
        private WithValue(String name) {
            super(name);
        }

        public Instance instance(T value) {
            return new Instance() {
                @Override
                public SerializationAttribute attribute() {
                    return WithValue.this;
                }

                @Override
                public Object value() {
                    return value;
                }
            };
        }
    }

    public interface Instance {
        SerializationAttribute attribute();
        Object value();
    }
}
