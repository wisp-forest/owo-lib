package io.wispforest.owo.ui.component;

import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import org.w3c.dom.Element;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import net.minecraft.network.chat.Text;

public class DiscreteSliderComponent extends SliderComponent {

    protected double min, max;

    protected int decimalPlaces = 0;
    protected boolean snap = false;

    protected DiscreteSliderComponent(Sizing horizontalSizing, double min, double max) {
        super(horizontalSizing);

        this.min = min;
        this.max = max;

        this.updateMessage();
        this.message(Text::literal);
    }

    @Override
    protected void applyValue() {
        this.changedEvents.sink().onChanged(this.discreteValue());
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.messageProvider.apply(String.format("%." + decimalPlaces + "f", this.discreteValue())));
    }

    public double discreteValue() {
        return new BigDecimal(this.min + this.value * (this.max - this.min)).setScale(this.decimalPlaces, RoundingMode.HALF_UP).doubleValue();
    }

    public DiscreteSliderComponent setFromDiscreteValue(double discreteValue) {
        this.value((discreteValue - min) / (max - min));
        return this;
    }

    public DiscreteSliderComponent decimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
        return this;
    }

    public int decimalPlaces() {
        return this.decimalPlaces;
    }

    public double min() {
        return this.min;
    }

    public double max() {
        return this.max;
    }

    public DiscreteSliderComponent snap(boolean snap) {
        this.snap = snap;
        return this;
    }

    public boolean snap() {
        return this.snap;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        UIParsing.apply(children, "decimal-places", UIParsing::parseUnsignedInt, this::decimalPlaces);
        UIParsing.apply(children, "value", UIParsing::parseDouble, this::setFromDiscreteValue);
    }

    public static DiscreteSliderComponent parse(Element element) {
        UIParsing.expectAttributes(element, "min", "max");
        return new DiscreteSliderComponent(
                Sizing.content(),
                UIParsing.parseDouble(element.getAttributeNode("min")),
                UIParsing.parseDouble(element.getAttributeNode("max"))
        );
    }
}
