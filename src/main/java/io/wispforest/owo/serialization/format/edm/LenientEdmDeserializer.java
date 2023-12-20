package io.wispforest.owo.serialization.format.edm;

import io.wispforest.owo.serialization.SerializationAttribute;

public class LenientEdmDeserializer extends EdmDeserializer {

    public LenientEdmDeserializer(EdmElement<?> serialized, SerializationAttribute... extraAttributes) {
        super(serialized, extraAttributes);
    }

    // ---

    @Override
    public byte readByte() {
        return this.getValue().<Number>cast().byteValue();
    }

    @Override
    public short readShort() {
        return this.getValue().<Number>cast().shortValue();
    }

    @Override
    public int readInt() {
        return this.getValue().<Number>cast().intValue();
    }

    @Override
    public long readLong() {
        return this.getValue().<Number>cast().longValue();
    }

    // ---

    @Override
    public float readFloat() {
        return this.getValue().<Number>cast().floatValue();
    }

    @Override
    public double readDouble() {
        return this.getValue().<Number>cast().doubleValue();
    }

    // ---

    @Override
    public boolean readBoolean() {
        if(this.getValue().value() instanceof Number number){
            return number.byteValue() == 1;
        }

        return super.readBoolean();
    }
}
