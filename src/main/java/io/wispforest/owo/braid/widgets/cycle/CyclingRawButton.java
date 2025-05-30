//package io.wispforest.owo.braid.widgets.cycle;
//
//import io.wispforest.owo.braid.framework.BuildContext;
//import io.wispforest.owo.braid.framework.widget.StatelessWidget;
//import io.wispforest.owo.braid.framework.widget.Widget;
//import net.minecraft.util.math.MathHelper;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.List;
//import java.util.function.Consumer;
//
//public class CyclingRawButton<T> extends StatelessWidget {
//
//    public final int index;
//
//    public final List<T> values;
//
//    public final @Nullable Consumer<T> onChanged;
//
//    public final Widget child;
//
//    public CyclingRawButton(int index, List<T> values, @Nullable Consumer<T> onChanged, Widget child) {
//        this.index = index;
//        this.values = values;
//        this.onChanged = onChanged;
//        this.child = child;
//    }
//
//    public CyclingRawButton(int index, List<T> values, Consumer<T> onChanged, boolean enabled, Widget child) {
//        this(index, values, enabled ? onChanged : null, child);
//    }
//
//    public CyclingRawButton(T value, List<T> values, @Nullable Consumer<T> onChanged, Widget child) {
//        this(value, values.indexOf(value), values, onChanged, child);
//    }
//
//    public CyclingRawButton(T value, List<T> values, Consumer<T> onChanged, boolean enabled, Widget child) {
//        this(value, values.indexOf(value), values, enabled ? onChanged : null, child);
//    }
//
//    public CyclingRawButton(int index, List<T> values, @Nullable Consumer<T> onChanged, Widget child) {
//        this(values.get(index), index, values, onChanged, child);
//    }
//
//    public CyclingRawButton(int index, List<T> values, Consumer<T> onChanged, boolean enabled, Widget child) {
//        this(values.get(index), index, values, enabled ? onChanged : null, child);
//    }
//
//
//    @Override
//    public Widget build(BuildContext context) {
//        return new RawCyclingButton<>(
//            this.value,
//            amount -> this.values.get(MathHelper.floorMod(this.index + amount, this.values.size())),
//            this.onChanged,
//            this.child
//        );
//    }
//}
