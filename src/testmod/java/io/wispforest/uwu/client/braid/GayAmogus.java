package io.wispforest.uwu.client.braid;

import io.wispforest.owo.braid.core.Color;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;

import java.time.Duration;

public class GayAmogus extends StatefulWidget {

    public final double pixelSize;

    public GayAmogus(double pixelSize) {
        this.pixelSize = pixelSize;
    }

    @Override
    public WidgetState<GayAmogus> createState() {
        return new State();
    }

    public static class State extends WidgetState<GayAmogus> {

        @Override
        public void init() {
            this.update(Duration.ZERO);
        }

        private void update(Duration delta) {
            this.setState(() -> {});
            this.scheduleAnimationCallback(this::update);
        }

        @Override
        public Widget build(BuildContext context) {
            return new Amogus(
                new Box(Color.hsv(System.currentTimeMillis() / 5000d % 1d, .85, 1)),
                new Box(Color.WHITE),
                this.widget().pixelSize
            );
        }
    }
}
