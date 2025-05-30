//package io.wispforest.owo.braid.widgets.cycle;
//
//import io.wispforest.owo.braid.core.Insets;
//import io.wispforest.owo.braid.framework.BuildContext;
//import io.wispforest.owo.braid.framework.proxy.WidgetState;
//import io.wispforest.owo.braid.framework.widget.StatefulWidget;
//import io.wispforest.owo.braid.framework.widget.StatelessWidget;
//import io.wispforest.owo.braid.framework.widget.Widget;
//import io.wispforest.owo.braid.widgets.basic.MouseArea;
//import io.wispforest.owo.braid.widgets.basic.Padding;
//import io.wispforest.owo.braid.widgets.basic.Panel;
//import io.wispforest.owo.braid.widgets.button.Button;
//import io.wispforest.owo.braid.widgets.button.RawButton;
//import io.wispforest.owo.braid.widgets.label.Label;
//import io.wispforest.owo.ui.component.ButtonComponent;
//import net.minecraft.text.Text;
//import net.minecraft.util.math.MathHelper;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.List;
//import java.util.function.Consumer;
//
//public class CyclingButton<T> extends StatefulWidget {
//
//    public final int index;
//
//    public final List<T> values;
//
//    public final @Nullable Consumer<T> onChanged;
//
//    public final Widget child;
//
//    public CyclingButton(int index, List<T> values, @Nullable Consumer<T> onChanged, Widget child) {
//        this.index = index;
//        this.values = values;
//        this.onChanged = onChanged;
//        this.child = child;
//    }
//
//    public CyclingButton(int index, List<T> values, Consumer<T> onChanged, Widget child) {
//        this(index, values, onChanged, new Label(label));
//    }
//
//
//
//    public CyclingButton(T value, List<T> values, @Nullable Consumer<T> onChanged, Widget child) {
//        this(value, values.indexOf(value), values, onChanged, child);
//    }
//
//    public CyclingButton(T value, List<T> values, @Nullable Consumer<T> onChanged, Text label) {
//        this(value, values, onChanged, new Label(label));
//    }
//
//    public CyclingButton(T value, List<T> values, Consumer<T> onChanged, boolean enabled, Widget child) {
//        this(value, values.indexOf(value), values, enabled ? onChanged : null, child);
//    }
//
//    public CyclingButton(T value, List<T> values, Consumer<T> onChanged, boolean enabled, Text label) {
//        this(value, values, onChanged, enabled, new Label(label));
//    }
//
//    public CyclingButton(int index, List<T> values, @Nullable Consumer<T> onChanged, Widget child) {
//        this(values.get(index), index, values, onChanged, child);
//    }
//
//    public CyclingButton(int index, List<T> values, @Nullable Consumer<T> onChanged, Text label) {
//        this(index, values, onChanged, new Label(label));
//    }
//
//    public CyclingButton(int index, List<T> values, Consumer<T> onChanged, boolean enabled, Widget child) {
//        this(values.get(index), index, values, enabled ? onChanged : null, child);
//    }
//
//    public CyclingButton(int index, List<T> values, Consumer<T> onChanged, boolean enabled, Text label) {
//        this( index, values, onChanged, enabled, new Label(label));
//    }
//
//    @Override
//    public WidgetState<?> createState() {
//        return new State();
//    }
//
//    public static class State extends WidgetState<CyclingButton<?>> {
//        private boolean hovered = false;
//
//        @Override
//        public Widget build(BuildContext context) {
//            var active = this.widget().onChanged != null;
//            return new MouseArea(
//                widget -> widget
//                    .enterCallback(() -> this.setState(() -> this.hovered = true))
//                    .exitCallback(() -> this.setState(() -> this.hovered = false)),
//                new CyclingRawButton<>(
//                    this.widget().index,
//                    this.widget().values,
//                    this.widget().onChanged,
//                    new Panel(
//                        active
//                            ? this.hovered ? ButtonComponent.HOVERED_TEXTURE : ButtonComponent.ACTIVE_TEXTURE
//                            : ButtonComponent.DISABLED_TEXTURE,
//                        new Padding(
//                            Insets.all(5),
//                            widget().child
//                        )
//                    )
//                )
//            );
//        }
//    }
//}
