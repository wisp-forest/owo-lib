package io.wispforest.owo.braid.widgets.combobox;

import io.wispforest.owo.Owo;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.ListenableValue;
import io.wispforest.owo.braid.core.RelativePosition;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.SpriteWidget;
import io.wispforest.owo.braid.widgets.basic.HoverableBuilder;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Panel;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Flexible;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.intents.*;
import io.wispforest.owo.braid.widgets.overlay.Overlay;
import io.wispforest.owo.braid.widgets.overlay.OverlayEntry;
import io.wispforest.owo.braid.widgets.overlay.OverlayEntryBuilder;
import io.wispforest.owo.braid.widgets.textinput.EditableText;
import io.wispforest.owo.braid.widgets.textinput.TextEditingController;
import io.wispforest.owo.braid.widgets.textinput.TextEditingValue;
import io.wispforest.owo.braid.widgets.textinput.TextSelection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Function;

public class ComboBox<T> extends StatefulWidget {

    public static final Identifier ACTIVE_TEXTURE = Owo.id("braid_combobox/active");
    public static final Identifier HOVERED_TEXTURE = Owo.id("braid_combobox/hovered");
    public static final Identifier DISABLED_TEXTURE = Owo.id("braid_combobox/disabled");

    // ---

    public final Function<T, Component> optionToName;

    public final List<T> options;
    public final @Nullable T selectedOption;
    public final SelectCallback<T> onSelect;

    public ComboBox(Function<T, Component> optionToName, List<T> options, @Nullable T selectedOption, SelectCallback<T> onSelect) {
        this.optionToName = optionToName;
        this.options = options;
        this.selectedOption = selectedOption;
        this.onSelect = onSelect;
    }

    public ComboBox(List<T> options, @Nullable T selectedOption, SelectCallback<T> onSelect) {
        this(option -> Component.literal(Objects.toString(option)), options, selectedOption, onSelect);
    }

    @Override
    public WidgetState<ComboBox<T>> createState() {
        return new State<>();
    }

    public List<Component> optionNames() {
        return this.options.stream().map(this::nameOption).toList();
    }

    public Component nameOption(@Nullable T option) {
        return option != null
            ? this.optionToName.apply(option)
            : Component.empty();
    }

    public interface SelectCallback<T> {
        void onSelect(T option);
    }

    private static class State<T> extends WidgetState<ComboBox<T>> {

        private final Runnable listener = this::textListener;

        private TextEditingController controller;
        private String lastText;

        private @Nullable OverlayEntry currentOverlay;
        private @Nullable ListenableValue<ComboBoxButtonsState<T>> buttonsState;

        private boolean isOpen() {
            return this.currentOverlay != null;
        }

        @Override
        public void init() {
            this.controller = new TextEditingController(this.widget().nameOption(widget().selectedOption).getString());
            this.controller.addListener(this.listener);

        }

        @Override
        public void didUpdateWidget(ComboBox<T> oldWidget) {
            if (!Objects.equals(this.widget().selectedOption, oldWidget.selectedOption)) {
                this.resetTextInput();
            }
        }

        @Override
        public void dispose() {
            this.controller.removeListener(this.listener);
            if (this.currentOverlay != null) {
                this.currentOverlay.remove();
            }
        }

        private void textListener() {
            if (Objects.equals(this.controller.value().text(), this.lastText)) return;
            this.lastText = controller.value().text();

            if (this.widget().optionNames().stream().map(Component::getString).anyMatch(s -> s.equals(this.controller.value().text()))) {
                return;
            }

            if (!this.isOpen()) {
                this.open();
            }

            this.buttonsState.setValue(new ComboBoxButtonsState<>(
                this.widget().options.stream()
                    .filter(option -> this.widget().nameOption(option).getString().startsWith(this.controller.value().text()))
                    .toList(),
                OptionalInt.empty()
            ));
        }

        private void resetTextInput() {
            var text = this.widget().nameOption(this.widget().selectedOption).getString();
            this.controller.setValue(new TextEditingValue(
                text,
                TextSelection.collapsed(text.length())
            ));
        }

        private void select(T option) {
            this.widget().onSelect.onSelect(option);
            this.resetTextInput();

            if (this.currentOverlay != null) {
                this.currentOverlay.remove();
            }
        }

        private void trySelectHighlightedValue() {
            if (this.buttonsState == null) return;

            var state = this.buttonsState.value();
            if (state.highlightedOptionIdx().isEmpty() && state.options().isEmpty()) {
                return;
            }

            this.select(
                state.highlightedOptionIdx().isPresent()
                    ? state.options().get(state.highlightedOptionIdx().getAsInt())
                    : state.options().getFirst()
            );
        }

        private void cycle(int offset) {
            if (this.isOpen()) {
                var state = this.buttonsState.value();

                var currentOptionIdx = state.highlightedOptionIdx().orElse(offset > 0 ? -1 : 0);
                var nextOptionIdx = Math.floorMod(currentOptionIdx + offset, state.options().size());

                this.buttonsState.setValue(new ComboBoxButtonsState<>(
                    state.options(),
                    OptionalInt.of(nextOptionIdx)
                ));
            } else {
                var currentOptionIdx = this.widget().selectedOption != null
                    ? this.widget().options.indexOf(this.widget().selectedOption)
                    : -Integer.signum(offset);
                var nextOptionIdx = Math.floorMod(currentOptionIdx + offset, this.widget().options.size());

                this.select(this.widget().options.get(nextOptionIdx));
            }
        }

        private void open() {
            this.setState(() -> {
                this.buttonsState = new ListenableValue<>(new ComboBoxButtonsState<>(this.widget().options, OptionalInt.empty()));
                this.currentOverlay = Overlay.of(this.context()).add(
                    new OverlayEntryBuilder(
                        new ComboBoxButtons<>(
                            this.buttonsState,
                            this.context().instance().transform.width(),
                            this.widget()::nameOption,
                            this::select
                        ),
                        new RelativePosition(this.context(), 0, this.context().instance().transform.height() - 1)
                    )
                        .dismissOverlayOnClick()
                        .onRemove(() -> this.setState(() -> {
                            this.currentOverlay = null;
                            this.buttonsState = null;
                        }))
                );
            });
        }

        private void close() {
            if (this.currentOverlay != null) {
                this.currentOverlay.remove();
            }
        }

        @Override
        public Widget build(BuildContext context) {
            var expanded = this.isOpen();

            return new Interactable(
                SHORTCUTS,
                widget -> widget
                    .focusLostCallback(this::resetTextInput)
                    .cursorStyle(CursorStyle.HAND)
                    .addCallbackAction(CycleIntent.class, (actionCtx, intent) -> this.cycle(intent.previous() ? -1 : 1))
                    .addCallbackAction(SelectIntent.class, (actionCtx, intent) -> {
                        if (expanded) {
                            this.trySelectHighlightedValue();
                        } else {
                            this.open();
                        }
                    })
                    .addCallbackAction(PrimaryActionIntent.class, (actionCtx, intent) -> {
                        if (expanded) {
                            this.close();
                        } else {
                            this.open();
                        }
                    }),
                new HoverableBuilder(
                    (hoverableContext, hovered, child) -> new Panel(
                        (expanded || hovered) ? HOVERED_TEXTURE : ACTIVE_TEXTURE,
                        child
                    ),
                    new Padding(
                        Insets.of(4, 4, 6, 0),
                        new Row(
                            MainAxisAlignment.START,
                            CrossAxisAlignment.CENTER,
                            new Flexible(
                                new EditableText(
                                    this.controller,
                                    widget -> widget
                                        .textShadow(true)
                                        .singleLine()
                                )
                            ),
                            new Padding(
                                Insets.horizontal(3),
                                new SpriteWidget(Owo.id("braid_combo_box_arrow"))
                            )
                        )
                    )
                )
            );
        }
    }

    // ---

    private static final Map<List<ShortcutTrigger>, Intent> SHORTCUTS = Map.of(
        List.of(ShortcutTrigger.UP), new CycleIntent(true),
        List.of(ShortcutTrigger.DOWN), new CycleIntent(false),
        List.of(new ShortcutTrigger(Trigger.ofKey(GLFW.GLFW_KEY_ENTER), Trigger.ofKey(GLFW.GLFW_KEY_KP_ENTER))), new SelectIntent(),
        List.of(ShortcutTrigger.LEFT_CLICK), PrimaryActionIntent.INSTANCE
    );
}

record CycleIntent(boolean previous) implements Intent {}
record SelectIntent() implements Intent {}