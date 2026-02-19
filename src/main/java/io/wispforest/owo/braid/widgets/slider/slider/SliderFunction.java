package io.wispforest.owo.braid.widgets.slider.slider;

import net.minecraft.util.Mth;

import static net.minecraft.util.Mth.EPSILON;

public interface SliderFunction {
    double normalize(double value, double min, double max);

    double deNormalize(double normalizedValue, double min, double max);

    SliderFunction LINEAR = new SliderFunction() {
        @Override
        public double normalize(double value, double min, double max) {
            return (value - min) / (max - min);
        }

        @Override
        public double deNormalize(double normalizedValue, double min, double max) {
            return min + normalizedValue * (max - min);
        }
    };

    SliderFunction LOGARITHMIC = new SliderFunction() {

        @Override
        public double normalize(double value, double min, double max) {
            if (min <= 0) {
                var offset = EPSILON - min;
                min += offset;
                max += offset;
                value += offset;
            }

            value = Mth.clamp(value, min, max);

            var logMin = Math.log(min);
            var logMax = Math.log(max);

            if (logMin >= logMax) return (value - min) / (max - min);

            return (Math.log(value) - logMin) / (logMax - logMin);
        }

        @Override
        public double deNormalize(double normalizedValue, double min, double max) {
            if (min <= 0) {
                var offset = EPSILON - min;
                min += offset;
                max += offset;
            }

            var logMin = Math.log(min);
            var logMax = Math.log(max);

            var expValue = Math.exp(logMin + normalizedValue * (logMax - logMin));

            if (min <= 0 && max > min) expValue -= (EPSILON - min);

            return expValue;
        }
    };
}
