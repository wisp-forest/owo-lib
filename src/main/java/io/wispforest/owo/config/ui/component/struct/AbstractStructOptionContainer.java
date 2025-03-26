package io.wispforest.owo.config.ui.component.struct;

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

//        this.padding(Insets.vertical(4));
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

    private void addToRow(Component component, boolean resetRow) {
        if (this.currentRow == null || resetRow) {
            this.currentRow = Containers.horizontalFlow(Sizing.content(), Sizing.content());
            this.child(currentRow);
        }

        this.currentRow.child(component);
    }

    private int componentIndex = 0;

    protected final void addOptionComponent(Class<?> clazz, O option) {
        var optionClazz = (Class<?>) option.clazz();

        OptionComponentFactory.Result<? extends Component, ? extends OptionValueProvider> result;

        boolean unpackResult = false;

        // TODO: DEHARDCODE?
        if (NumberReflection.isNumberType(option.clazz())) {
            result = attachOptionLabel(OptionComponentFactory.NUMBER.make(model, option), sideBySideFormat, true);
        } else if (optionClazz == String.class) {
            result = attachOptionLabel(OptionComponentFactory.STRING.make(model, option), sideBySideFormat, true);
        } else if (optionClazz == Boolean.class || optionClazz == boolean.class){
            result = attachOptionLabel(OptionComponentFactory.BOOLEAN.make(model, option), sideBySideFormat, true);
        } else if (optionClazz == Identifier.class) {
            result = attachOptionLabel(OptionComponentFactory.IDENTIFIER.make(model, option), sideBySideFormat, true);
        } else if (optionClazz == Color.class) {
            result = attachOptionLabel(OptionComponentFactory.COLOR.make(model, option), sideBySideFormat, true);
        } else if (optionClazz == List.class) {
            var layout = new ListOptionContainer<>(model, (FieldOption<List<Object>>) option, ArrayList::new, false, false);
            layout.horizontalSizing(Sizing.fill(50));
            result = new OptionComponentFactory.Result(layout, layout);
            //sideBySideFormat = false;
        } else if (optionClazz == Set.class) {
            var layout = new ListOptionContainer<>(model, (FieldOption<Set<Object>>) option, LinkedHashSet::new, false, false);
            layout.horizontalSizing(Sizing.fill(50));
            result = new OptionComponentFactory.Result(layout, layout);
            //sideBySideFormat = false;
        } else if (optionClazz.isEnum()) {
            result = attachOptionLabel(OptionComponentFactory.ENUM.make(model, option), sideBySideFormat, true);
        } else if (optionClazz.isRecord()) {
            var layout = RecordStructOptionContainer.of(model, option);
            result = new OptionComponentFactory.Result<>(layout, layout);
            unpackResult = true;
        } else {
            var layout = StructOptionContainer.of(model, option);
            result = new OptionComponentFactory.Result<>(layout, layout);
            unpackResult = true;
        }

        if(unpackResult) {
            var layout = (AbstractStructOptionContainer<FieldOption>) result.baseComponent();

            var components = layout.children.stream().flatMap(component -> {
                if (component instanceof ParentComponent parentComponent) {
                    return parentComponent.children().stream();
                }

                return Stream.of(component);
            }).toList();

            for (var child : components) {
                this.addToRow(child, componentIndex % 2 == 0 || !sideBySideFormat);

                componentIndex++;
            }
        } else {
            this.addToRow(result.baseComponent(), componentIndex % 2 == 0 || !sideBySideFormat);

            componentIndex++;
        }

        this.optionsProviders.put(option, result.optionProvider());
    }

    private static OptionComponentFactory.Result<? extends Component, ? extends OptionValueProvider> attachOptionLabel(OptionComponentFactory.Result<? extends Component, ? extends OptionValueProvider> labeledResult, boolean sideBySideFormat, boolean reducedPadding) {
        var optionComponent = labeledResult.baseComponent();

        if (sideBySideFormat) optionComponent.horizontalSizing(Sizing.fill(50)); // Difference 1

        if (reducedPadding && optionComponent instanceof ParentComponent parentComponent) {
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
