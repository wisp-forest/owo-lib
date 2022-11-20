package io.wispforest.owo.config.ui.component;

import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.util.NumberReflection;
import org.jetbrains.annotations.ApiStatus;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@ApiStatus.Internal
@SuppressWarnings("UnusedReturnValue")
public class ConfigTextBox extends TextBoxComponent implements OptionComponent {

    protected int invalidColor = 0xEB1D36, validColor = 0x28FFBF;
    protected Function<String, Object> valueParser = s -> s;
    protected Predicate<String> inputPredicate = s -> true, applyPredicate = s -> true;

    public ConfigTextBox() {
        super(Sizing.fixed(0));
        this.setMaxLength(Integer.MAX_VALUE);

        this.textValue.observe(s -> {
            this.setEditableColor(this.applyPredicate.test(s) ? this.validColor : this.invalidColor);
        });
    }

    public ConfigTextBox configureForNumber(Class<? extends Number> fieldType) {
        final boolean floatingPoint = NumberReflection.isFloatingPointType(fieldType);
        final double min = NumberReflection.minValue(fieldType).doubleValue(), max = NumberReflection.maxValue(fieldType).doubleValue();

        this.valueParser = s -> {
            try {
                return NumberReflection.convert(floatingPoint ? Double.parseDouble(s) : Long.parseLong(s), fieldType);
            } catch (NumberFormatException nfe) {
                return NumberReflection.convert(0L, fieldType);
            }
        };

        this.inputPredicate(floatingPoint ? s -> s.matches("-?\\d*\\.?\\d*") : s -> s.matches("-?\\d*"));
        this.applyPredicate(s -> {
            try {
                var value = Double.parseDouble(s);
                return value >= min && value <= max;
            } catch (NumberFormatException nfe) {
                return false;
            }
        });

        return this;
    }

    @Override
    public boolean isValid() {
        return this.applyPredicate.test(this.getText());
    }

    @Override
    public Object parsedValue() {
        return this.valueParser.apply(this.getText());
    }

    public ConfigTextBox inputPredicate(Predicate<String> inputPredicate) {
        this.inputPredicate = inputPredicate;
        this.setTextPredicate(this.inputPredicate);
        return this;
    }

    public Predicate<String> inputPredicate() {
        return inputPredicate;
    }

    public ConfigTextBox applyPredicate(Predicate<String> applyPredicate) {
        this.applyPredicate = applyPredicate;
        return this;
    }

    public Predicate<String> applyPredicate() {
        return applyPredicate;
    }

    public ConfigTextBox invalidColor(int invalidColor) {
        this.invalidColor = invalidColor;
        return this;
    }

    public int invalidColor() {
        return invalidColor;
    }

    public ConfigTextBox validColor(int validColor) {
        this.validColor = validColor;
        return this;
    }

    public int validColor() {
        return validColor;
    }

    public Function<String, Object> valueParser() {
        return this.valueParser;
    }

    public ConfigTextBox valueParser(Function<String, Object> valueParser) {
        this.valueParser = valueParser;
        return this;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);
        UIParsing.apply(children, "invalid-color", Color::parseAndPack, this::invalidColor);
        UIParsing.apply(children, "valid-color", Color::parseAndPack, this::validColor);
    }
}
