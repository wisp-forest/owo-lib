package io.wispforest.owo.config.ui.component.struct;

import io.wispforest.owo.config.ConfigReflectionUtils;
import io.wispforest.owo.config.options.OptionControlSpec;
import io.wispforest.owo.config.options.ReflectiveOption;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.config.ui.component.OptionValueProvider;
import io.wispforest.owo.config.ui.component.OrderedOptionContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.util.NumberReflection;
import io.wispforest.owo.util.ReflectionUtils;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ListOptionContainer<C extends Collection<T>, T> extends OrderedOptionContainer<C, T> {

    protected final Function<Collection<T>, C> collectionConstructor;
    protected Class<T> listType = null;

    protected ConfigReflectionUtils.CollectionType type;

    public ListOptionContainer(UIModel uiModel, OptionControlSpec<C> option, Function<Collection<T>, C> collectionConstructor, boolean expanded, boolean isDetached) {
        super(uiModel, option, expanded, isDetached);

        this.collectionConstructor = collectionConstructor;
    }

    protected ConfigReflectionUtils.CollectionType type() {
        if (this.type == null) {
            this.type = ConfigReflectionUtils.getCollectionType(backingOption.getGenericType());
        }

        return this.type;
    }

    protected Class<T> listType() {
        if (this.listType == null) {
            this.listType = (Class<T>) ReflectionUtils.getTypeArgument(this.backingOption.getGenericType(), 0);
        }

        return this.listType;
    }

    @Override
    protected <P extends OptionValueProvider & Component> P createProviderComponent(T listEntry) {
        var structLike = this.type() == ConfigReflectionUtils.CollectionType.COMPLEX;

        if (structLike) {
            return (P) ((listEntry instanceof Record)
                    ? RecordStructOptionContainer.of(this.uiModel, this.backingOption, (Class<Record>) listType(), (Record) listEntry)
                    : StructOptionContainer.of(this.uiModel, this.backingOption, listType(), listEntry));
        } else {
            final var box = this.uiModel.expandTemplate(ConfigTextBox.class, "collection-text-box", Map.of("initial-value", listEntry.toString()));

            if (!this.isDetached) {
                box.onChanged().subscribe(s -> {
                    if (!box.isValid()) return;

                    this.refreshResetButton();
                });
            } else {
                box.active = false;
            }

            if (NumberReflection.isNumberType(listType())) {
                var numberType = (Class<? extends Number>) listType();
                var data = ConfigReflectionUtils.getConstraintData(numberType, this.backingOption);

                box.configureForNumber(numberType, data.min(), data.max());
            } else if(listType() == Identifier.class) {
                box.configureForIdentifier();
            }

            return (P) box;
        }
    }

    @Override
    protected boolean tickAtTop() {
        return this.type() == ConfigReflectionUtils.CollectionType.COMPLEX;
    }

    public T createDefaultValue() {
        return this.type == ConfigReflectionUtils.CollectionType.COMPLEX
                ? ReflectionUtils.tryInstantiateWithNoArgs(this.listType())
                : (T) "";
    }

    @Override
    protected List<T> convertToList(C data) {
        return new ArrayList<>(data);
    }

    @Override
    protected C convertToCollection(List<T> backingList) {
        return collectionConstructor.apply(backingList);
    }
}
