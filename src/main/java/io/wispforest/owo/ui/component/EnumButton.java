package io.wispforest.owo.ui.component;

import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.function.Consumer;

public class EnumButton extends IncrementButton {
    private Class<? extends Enum<?>> clazz;
    private Enum<?>[] enumEntries;

    protected EnumButton(Text message, Consumer<ButtonComponent> onPress) {
        super(message, onPress, 0, 0, 1);
    }

    public EnumButton(Text message, Consumer<ButtonComponent> onPress, Class<? extends Enum<?>> clazz, int startingIndex) {
        this(message, onPress);

        this.setup(clazz, startingIndex);
    }

    public EnumButton setup(Class<? extends Enum<?>> clazz, int startingIndex) {
        this.clazz = clazz;
        this.enumEntries = clazz.getEnumConstants();

        this.maxValue = clazz.getEnumConstants().length;

        this.rawEnumValue(enumEntries[startingIndex]);

        return this;
    }

    public EnumButton rawEnumValue(Enum<?> e) {
        if (e.getClass().equals(clazz)) {
            throw new ClassCastException("Unable to set the given enum value [" + e.name() + "] as its not the correct type!");
        }

        this.setValue(e.ordinal());

        return this;
    }

    public Enum<?> rawEnumValue() {
        return this.enumEntries[this.getValue()];
    }

    public EnumButton setRenderer(ValueRenderer<EnumButton> renderer) {
        this.renderer = (ValueRenderer<IncrementButton>) (Object) renderer;

        return this;
    }

    @Override
    protected Text getValueText() {
        var enumName = StringUtils.uncapitalize(clazz.componentType().getSimpleName());
        var valueName = this.rawEnumValue().name().toLowerCase(Locale.ROOT);

        return Text.translatable("text.enum." + enumName + "." + valueName);
    }
}
