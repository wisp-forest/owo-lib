//package io.wispforest.owo.braid.widgets.cycle;
//
//import io.wispforest.owo.braid.framework.BuildContext;
//import io.wispforest.owo.braid.framework.widget.StatelessWidget;
//import io.wispforest.owo.braid.framework.widget.Widget;
//import io.wispforest.owo.braid.widgets.basic.MouseArea;
//import io.wispforest.owo.braid.widgets.button.RawButton;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.function.Consumer;
//
//public class RawCyclingButton<T> extends StatelessWidget {
//
//    public final T value;
//
//    public final Cycler<T> cycler;
//
//    public final @Nullable Consumer<T> onChanged;
//
//    public final Widget child;
//
//    public RawCyclingButton(T value, Cycler<T> cycler, @Nullable Consumer<T> onChanged, Widget child) {
//        this.value = value;
//        this.cycler = cycler;
//        this.onChanged = onChanged;
//        this.child = child;
//    }
//
//    public RawCyclingButton(T value, Cycler<T> cycler, Consumer<T> onChanged, boolean enabled, Widget child) {
//        this(value, cycler, enabled ? onChanged : null, child);
//    }
//
//    @Override
//    public Widget build(BuildContext context) {
//        var active = this.onChanged != null;
//        return new MouseArea(
//            widget -> widget.scrollCallback((horizontal, vertical) -> {
//                if (this.onChanged == null || vertical == 0) return;
//                this.onChanged.accept(this.cycler.cycle(vertical < 0 ? -1 : 1));
//            }),
//            new RawButton(
//                button -> {
//                    if (button != 0 && button != 1) return false;
//                    this.onChanged.accept(this.cycler.cycle(button == 0 ? 1 : -1));
//                    return true;
//                },
//                active,
//                child
//            )
//        );
//    }
//
//    @FunctionalInterface
//    public interface Cycler<T> {
//        T cycle(int amount);
//    }
//}
