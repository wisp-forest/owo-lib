package io.wispforest.owo.serialization;

public interface StructSerializer extends Endable {

    <F> StructSerializer field(String name, Endec<F> endec, F value);

}
