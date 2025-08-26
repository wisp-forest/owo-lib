package io.wispforest.owo.braid.widgets.slider;

import net.minecraft.util.math.MathHelper;

import static net.minecraft.util.math.MathHelper.EPSILON;

public interface ValueMapper {
    double normalize(double value, double min, double max);

    double discretize(double normalizedValue, double min, double max);

    ValueMapper LINEAR = new ValueMapper() {
        @Override
        public double normalize(double value, double min, double max) {
            return (value - min) / (max - min);
        }

        @Override
        public double discretize(double normalizedValue, double min, double max) {
            return min + normalizedValue * (max - min);
        }
    };

    ValueMapper LOGARITHMIC = new ValueMapper() {

        @Override
        public double normalize(double value, double min, double max) {
            var inverted = min > max;
            var effectiveMin = inverted ? max : min;
            var effectiveMax = inverted ? min : max;
            var effectiveValue = value;

            if (effectiveMin <= 0) {
                var offset = EPSILON - effectiveMin;
                effectiveMin += offset;
                effectiveMax += offset;
                effectiveValue += offset;
            }

            effectiveValue = MathHelper.clamp(effectiveValue, effectiveMin, effectiveMax);

            var logMin = Math.log(effectiveMin);
            var logMax = Math.log(effectiveMax);

            if (logMin >= logMax) return (effectiveValue - effectiveMin) / (effectiveMax - effectiveMin);

            var normalized = (Math.log(effectiveValue) - logMin) / (logMax - logMin);

            return inverted ? 1.0 - normalized : normalized;
        }

        @Override
        public double discretize(double normalizedValue, double min, double max) {
            var inverted = min > max;
            var effectiveMin = inverted ? max : min;
            var effectiveMax = inverted ? min : max;

            if (effectiveMin <= 0) {
                var offset = EPSILON - effectiveMin;
                effectiveMin += offset;
                effectiveMax += offset;
            }

            normalizedValue = MathHelper.clamp(normalizedValue, 0.0, 1.0);
            if (inverted) normalizedValue = 1.0 - normalizedValue;

            var logMin = Math.log(effectiveMin);
            var logMax = Math.log(effectiveMax);

            var expValue = Math.exp(logMin + normalizedValue * (logMax - logMin));

            if (min <= 0 && max > min) expValue -= (EPSILON - min);

            return MathHelper.clamp(expValue, min, max);
        }
    };
}
