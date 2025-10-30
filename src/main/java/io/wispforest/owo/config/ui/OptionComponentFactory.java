package io.wispforest.owo.config.ui;

import io.wispforest.owo.Owo;
import io.wispforest.owo.config.ConfigReflectionUtils;
import io.wispforest.owo.config.options.FieldOption;
import io.wispforest.owo.config.options.OptionControlSpec;
import io.wispforest.owo.config.options.ReflectiveOption;
import io.wispforest.owo.config.annotation.Expanded;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.annotation.WithAlpha;
import io.wispforest.owo.config.ui.component.ListOptionContainer;
import io.wispforest.owo.config.ui.component.OptionValueProvider;
import io.wispforest.owo.config.ui.component.SearchAnchorComponent;
import io.wispforest.owo.config.ui.component.struct.*;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.CollapsibleContainer;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.owo.util.NumberReflection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A function which creates an instance of {@link OptionValueProvider}
 * fitting for the given config option. Whatever component is created
 * should accurately reflect if the option is currently detached
 * and thus immutable - ideally it is non-interactable
 *
 * @param <T> The type of option for which this factory can create components
 */
public interface OptionComponentFactory<T> {

    OptionComponentFactory<? extends Number> NUMBER = withOptionLabel((model, option) -> {
        var data = ConfigReflectionUtils.getRangeConstraintData(option.clazz(), option);

        return OptionComponents.createNumberComponent(model, option, data.decimalPlaces(), data.min(), data.max(), data.useSlider(), option.detached());
    });

    OptionComponentFactory<String> STRING = withOptionLabel((model, option) -> OptionComponents.createStringComponent(model, option, option.detached()));

    OptionComponentFactory<Identifier> IDENTIFIER = withOptionLabel((model, option) -> OptionComponents.createIdentifierComponent(model, option, option.detached()));

    OptionComponentFactory<Color> COLOR = withOptionLabel((model, option) -> OptionComponents.createColorComponent(model, option, option.isAnnotationPresent(WithAlpha.class), option.detached()));

    OptionComponentFactory<Boolean> BOOLEAN = withOptionLabel((model, option) -> OptionComponents.createToggleButton(model, option, option.detached()));

    OptionComponentFactory<? extends Enum<?>> ENUM = withOptionLabel((model, option) -> OptionComponents.createEnumButton(model, option, option.detached()));

    @SuppressWarnings({"unchecked"})
    OptionComponentFactory<List<?>> LIST = (model, option) -> {
        var expanded = option.isAnnotationPresent(Expanded.class);
        return Result.of(new ListOptionContainer<>(model, (OptionControlSpec) option, ArrayList::new, expanded, option.detached()));
    };

    @SuppressWarnings({"unchecked"})
    OptionComponentFactory<Set<?>> SET = (model, option) -> {
        var expanded = option.isAnnotationPresent(Expanded.class);
        return Result.of(new ListOptionContainer<>(model, (OptionControlSpec) option, LinkedHashSet::new, expanded, option.detached()));
    };

    @SuppressWarnings({"unchecked"})
    OptionComponentFactory<Map<?, ?>> SIMPLE_MAP = (model, option) -> {
        var expanded = option.isAnnotationPresent(Expanded.class);
        return Result.of(new MapOptionContainer<>(model, (OptionControlSpec) option, expanded, option.detached()));
    };

    @SuppressWarnings({"unchecked"})
    OptionComponentFactory<Object> STRUCT = (model, option) -> {
        var layout = (option.value() instanceof Record)
            ? RecordStructOptionContainer.of(model, (FieldOption<? extends Record>) (Object) option)
            : StructOptionContainer.of(model, option);

        var container = wrapStructOption(layout, option, option.isAnnotationPresent(Expanded.class));

        return new Result(container, layout);
    };

    static CollapsibleContainer wrapStructOption(AbstractStructOptionContainer<?> layout, OptionControlSpec<?> option, boolean isExpanded) {
        var key = option.key();

        var titleKey = option.labelTranslationKey();
        var expanded = !key.isRoot() && isExpanded;

        var container = Containers.collapsible(
            Sizing.fill(100), Sizing.content(),
            Text.translatable(titleKey),
            expanded
        ).<CollapsibleContainer>configure(nestedContainer -> {
            if (I18n.hasTranslation(option.tooltipTranslationKey())) {
                nestedContainer.titleLayout().tooltip(Text.translatable(option.tooltipTranslationKey()));
            }

            nestedContainer.titleLayout().child(new SearchAnchorComponent(
                nestedContainer.titleLayout(),
                key,
                () -> I18n.translate(titleKey)
            ).highlightConfigurator(highlight ->
                highlight.positioning(Positioning.absolute(-5, -5))
                    .verticalSizing(Sizing.fixed(19))
            ));

            nestedContainer.child(layout);
        });

        addEasyCopyLabel(container.titleLayout(), titleKey);

        return container;
    }

    /**
     * Create a new component fitting for, and bound to,
     * the given config option
     *
     * @param model  The UI model of the enclosing screen, used
     *               for expanding templates
     * @param option The option for which to create a component
     * @return The option component as well as a potential wrapping
     * component, this simply be the option component itself
     */
    Result<?, ?> make(UIModel model, ReflectiveOption<T> option);

    record Result<B extends Component, P extends OptionValueProvider>(B baseComponent, P optionProvider) {
        public static <P extends Component & OptionValueProvider> Result<P, P> of(P optionProvider) {
            return new Result<>(optionProvider, optionProvider);
        }
    }

    ///
    /// Wraps the given factory with an option label which is recommended depending on the
    /// given factories function as highlighted by such not being used [STRUCT][#STRUCT] factory.
    ///
    static <T> OptionComponentFactory<T> withOptionLabel(OptionComponentFactory<T> factory) {
        return (model, option) -> attachOptionLabel(factory, model, option);
    }

    static <T> Result<? extends Component, ? extends OptionValueProvider> attachOptionLabel(OptionComponentFactory<T> factory, UIModel model, OptionControlSpec<?> option) {
        var baseComponent = model.expandTemplate(FlowLayout.class,
                "config-option-base",
                Map.of("config-option-name", option.labelTranslationKey())
        );

        var optionNameHolder = baseComponent.childById(FlowLayout.class, "option-name-holder");

        addEasyCopyLabel(optionNameHolder, option.labelTranslationKey());

        var result = factory.make(model, (ReflectiveOption<T>) option);

        if (result.baseComponent() instanceof ParentComponent parentComponent) {
            var deque = new ArrayDeque<>(List.of(parentComponent));

            while (!deque.isEmpty()) {
                var currentParent = deque.poll();

                for (var child : currentParent.children()) {
                    if (child instanceof SearchAnchorComponent searchAnchorComponent) {
                        searchAnchorComponent.anchorFrame(baseComponent);
                    } else if (child instanceof ParentComponent parentComponent1) {
                        deque.push(parentComponent1);
                    }
                }
            }
        }

        baseComponent
            .child(
                new SearchAnchorComponent(
                    baseComponent,
                    option.key(),
                    () -> baseComponent.childById(LabelComponent.class, "option-name").text().getString()
                )
            );

        baseComponent.childById(FlowLayout.class, "controls")
                .child(result.baseComponent());

        return new Result<>(baseComponent, result.optionProvider());
    }

    static void addEasyCopyLabel(@NotNull FlowLayout nameHolder, String translationKey) {
        Objects.requireNonNull(nameHolder, "Unable to add label as the nameHolder was found to be null!");

        if (!Owo.DEBUG || I18n.hasTranslation(translationKey)) return;

        var baseColor = Color.ofFormatting(Formatting.AQUA);

        var unhoveredColor = baseColor.interpolate(Color.BLACK, 0.3f);
        var hoveredColor = baseColor.interpolate(Color.WHITE, 0.6f);

        nameHolder.child(0,
            Components.label(Text.literal("\uD83D\uDCCB"))
                .configure((LabelComponent component) -> {
                    component.mouseEnter().subscribe(() -> {
                        component.color(hoveredColor);

                        component.tooltip(Text.of("Copy Translation Key"));
                    });

                    component.mouseLeave().subscribe(() -> component.color(unhoveredColor));

                    component.mouseDown().subscribe((click, bl) -> {
                        var client = MinecraftClient.getInstance();

                        client.keyboard.setClipboard(translationKey);
                        UISounds.playButtonSound();

                        component.tooltip(
                                Text.literal("Translation Key Copied!")
                                        .formatted(Formatting.GREEN)
                        );

                        return true;
                    });
                })
                .color(unhoveredColor)
                .margins(Insets.of(0,0,2,2))
        );
    }
}
