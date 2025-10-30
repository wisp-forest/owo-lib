package io.wispforest.owo.ui.component;

import net.minecraft.text.Text;

import java.util.function.Consumer;

public class TypedEnumButton<E extends Enum<E>> extends EnumButton {
    private final Class<E> clazz;

    public TypedEnumButton(Text message, Consumer<ButtonComponent> onPress, Class<E> clazz, int startingIndex) {
        super(message, onPress, clazz, startingIndex);

        this.clazz = clazz;
    }

    public TypedEnumButton<E> value(E e) {
        this.rawEnumValue(e);

        return this;
    }

    public E value() {
        return (E) (Object) rawEnumValue();
    }

    public TypedEnumButton<E> renderer(ValueRenderer<TypedEnumButton<E>> renderer) {
        this.renderer = (ValueRenderer<IncrementButton>) (Object) renderer;

        return this;
    }
}
