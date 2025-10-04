package io.wispforest.owo.braid.widgets.intents;

public record AdjustIntent(Direction direction) implements Intent {
    public enum Direction {
        INCREMENT, DECREMENT;

        public int offset() {
            return switch (this) {
                case INCREMENT -> 1;
                case DECREMENT -> -1;
            };
        }
    }
}
