package io.wispforest.owo.config.ui.component.struct;

import io.wispforest.owo.Owo;
import io.wispforest.owo.config.ConfigReflectionUtils;
import io.wispforest.owo.config.annotation.Expanded;
import io.wispforest.owo.config.options.FieldOption;
import io.wispforest.owo.config.options.ReflectiveOption;
import io.wispforest.owo.config.base.Key;
import io.wispforest.owo.config.options.OptionControlSpec;
import io.wispforest.owo.config.ui.OptionComponentFactory;
import io.wispforest.owo.config.ui.component.*;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.util.NumberReflection;
import io.wispforest.owo.util.ReflectionUtils;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractStructOptionContainer<O extends ReflectiveOption> extends FlowLayout implements OptionValueProvider {

    protected final Map<O, OptionValueProvider> optionsProviders = new LinkedHashMap<>();

    protected final Map<Key, O> options = new LinkedHashMap<>();

    protected final UIModel model;

    protected final Identifier configId;
    protected final Key optionKey;

    protected boolean sideBySideFormat = true;

    protected AbstractStructOptionContainer(Sizing horizontalSizing, Sizing verticalSizing, UIModel uiModel, OptionControlSpec<?> option) {
        super(horizontalSizing, verticalSizing, Algorithm.VERTICAL);

        this.model = uiModel;

        this.configId = option.configId();
        this.optionKey = option.key();

        this.horizontalAlignment(HorizontalAlignment.LEFT);
    }

    @Nullable
    protected final OptionValueProvider getProvider(Key key) {
        var option = this.options.get(key);

        if (option != null) return optionsProviders.get(option);

        return null;
    }

    public AbstractStructOptionContainer<O> sideBySideFormating(boolean value) {
        this.sideBySideFormat = value;

        return this;
    }

    @Nullable
    private FlowLayout currentRow = null;

    private void addToRow(Component component) {
        if (this.currentRow == null || componentIndex % 2 == 0 || !sideBySideFormat) {
            this.currentRow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            this.child(currentRow);
        }

        this.currentRow.child(component);

        componentIndex++;
    }

    private int componentIndex = 0;

    protected final void addOptionComponent(O option) {
        var optionClazz = (Class<?>) option.clazz();

        OptionComponentFactory.Result<? extends Component, ? extends OptionValueProvider> result = null;

        // TODO: DEHARDCODE?
        if (NumberReflection.isNumberType(option.clazz())) {
            result = adjustOptionLabel(OptionComponentFactory.NUMBER.make(model, option), sideBySideFormat, true);
        } else if (optionClazz == String.class) {
            result = adjustOptionLabel(OptionComponentFactory.STRING.make(model, option), sideBySideFormat, true);
        } else if (optionClazz == Boolean.class || optionClazz == boolean.class){
            result = adjustOptionLabel(OptionComponentFactory.BOOLEAN.make(model, option), sideBySideFormat, true);
        } else if (optionClazz == Identifier.class) {
            result = adjustOptionLabel(OptionComponentFactory.IDENTIFIER.make(model, option), sideBySideFormat, true);
        } else if (optionClazz == Color.class) {
            result = adjustOptionLabel(OptionComponentFactory.COLOR.make(model, option), sideBySideFormat, true);
        }  else if (optionClazz.isEnum()) {
            result = adjustOptionLabel(OptionComponentFactory.ENUM.make(model, option), sideBySideFormat, true);
        } else if (optionClazz == List.class) {
            result = adjustOptionLabel(OptionComponentFactory.LIST.make(model, option), true, false);
        } else if (optionClazz == Set.class) {
            result = adjustOptionLabel(OptionComponentFactory.SET.make(model, option), true, false);
        } else if (option.clazz() == Map.class) {
            if (ConfigReflectionUtils.getMapType(option.getGenericType()) == ConfigReflectionUtils.CollectionType.SIMPLE) {
                result = adjustOptionLabel(OptionComponentFactory.SIMPLE_MAP.make(model, option), true, false);
            }
        } else if (optionClazz.isRecord()) {
            var layout = RecordStructOptionContainer.of(model, option);
            result = new OptionComponentFactory.Result<>(layout, layout);
        } else if (option.clazz() != Map.class) {
            try {
                ReflectionUtils.getNoArgsConstructor(option.clazz());

                var layout = StructOptionContainer.of(model, option);
                result = new OptionComponentFactory.Result<>(layout, layout);
            } catch (IllegalStateException ignored) {}
        }

        if (result != null) {
            List<Component> components = result.baseComponent() instanceof AbstractStructOptionContainer container
                ? container.unpackInnerOptions()
                : List.of(result.baseComponent());

            for (var child : components) this.addToRow(child);

            this.optionsProviders.put(option, result.optionProvider());
        } else {
            Owo.LOGGER.warn("Could not create UI component for config option within a StructOptionContainer {}", option);
        }
    }

    protected List<Component> unpackInnerOptions() {
        return this.children.stream().flatMap(component -> {
            return (component instanceof ParentComponent parentComponent)
                ? parentComponent.children().stream()
                : Stream.of(component);
        }).toList();
    }

    private static OptionComponentFactory.Result<? extends Component, ? extends OptionValueProvider> adjustOptionLabel(
        OptionComponentFactory.Result<? extends Component, ? extends OptionValueProvider> labeledResult,
        boolean sideBySideFormat,
        boolean adjustParentPadding) {
        var optionComponent = labeledResult.baseComponent();

        if (sideBySideFormat) optionComponent.horizontalSizing(Sizing.fill(50));

        if (adjustParentPadding && optionComponent instanceof ParentComponent parentComponent) {
            var currentPadding = parentComponent.padding().get();

            parentComponent.padding(Insets.of(currentPadding.top() - 5, currentPadding.bottom() - 5, currentPadding.left(), currentPadding.right()));
        }

        return labeledResult;
    }

    @Override
    public abstract Object parsedValue();

    @Override
    public boolean isValid() {
        return true;
    }
}
