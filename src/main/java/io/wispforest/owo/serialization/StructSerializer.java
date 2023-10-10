package io.wispforest.owo.serialization;

public interface StructSerializer extends Endable {

    <F> StructSerializer field(String name, Codeck<F> codec, F value);

}
