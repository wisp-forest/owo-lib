package io.wispforest.owo.config.ui.component.struct;

import io.wispforest.owo.config.*;
import io.wispforest.owo.config.base.BoundedAccess;
import io.wispforest.owo.config.options.FieldOption;
import io.wispforest.owo.config.options.OptionControlSpec;
import io.wispforest.owo.config.base.SyncMode;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.util.Observable;
import io.wispforest.owo.util.ReflectionUtils;
import it.unimi.dsi.fastutil.Pair;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

public class StructOptionContainer<T> extends AbstractStructOptionContainer<FieldOption> {

    protected T backingValue;

    protected StructOptionContainer(UIModel uiModel, OptionControlSpec<?> option, Class<T> clazz, List<Pair<Type, Class<?>>> genericTypes, T value, Function<Class<T>, T> defaultConstructor) {
        super(Sizing.expand(), Sizing.content(), uiModel, option);

        this.buildContainer(clazz, genericTypes, value, defaultConstructor);
    }

    public static <T> StructOptionContainer<T> of(UIModel uiModel, OptionControlSpec<T> option) {
        return of(uiModel, option, option.clazz(), option.value());
    }

    public static <T> StructOptionContainer<T> of(UIModel uiModel, OptionControlSpec<?> option, Class<T> clazz, T value) {
        return new StructOptionContainer<T>(uiModel, option, clazz, List.of(), value, ReflectionUtils::tryInstantiateWithNoArgs);
    }

    public static <T> StructOptionContainer<T> of(UIModel uiModel, OptionControlSpec<?> option, Class<T> clazz, List<Pair<Type, Class<?>>> genericTypes, T value, Function<Class<T>, T> defaultConstructor) {
        return new StructOptionContainer<T>(uiModel, option, clazz, genericTypes, value, defaultConstructor);
    }

    public void buildContainer(Class<T> clazz, List<Pair<Type, Class<?>>> genericTypes, T value, Function<Class<T>, T> defaultConstructor) {
        var fields = Arrays.stream(clazz.getFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()))
                .toList();

        var extraTypeInfo = !genericTypes.isEmpty();

        if (extraTypeInfo && fields.size() != genericTypes.size()) {
            throw new IllegalStateException("Unable to create Struct Option Container due to mismatch field amount to the passed generic types!");
        }

        var defaultValue = defaultConstructor.apply(clazz);

        this.backingValue = defaultConstructor.apply(clazz);

        this.sideBySideFormating(sideBySideFormat && fields.size() > 1);

        for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);
            var typeInfo = extraTypeInfo ? genericTypes.get(i) : null;

            var genericType = typeInfo != null ? typeInfo.left() : field.getGenericType();
            var type = typeInfo != null ? typeInfo.right() : field.getType();

            var boundField = new BoundedAccess.BoundField<>(backingValue, field, type, genericType);

            try {
                var currentValue = boundField.withOwner(value).getValue();

                boundField.setValue(currentValue);

                var option = new FieldOption<>(
                        configId,
                        optionKey.child(field.getName()),
                        boundField.withOwner(defaultValue).getValue(),
                        Observable.of(currentValue),
                        boundField,
                        ConfigReflectionUtils.getConstraint(boundField),
                        SyncMode.NONE,
                        null
                );

                options.put(option.key(), option);

                this.addOptionComponent(option);
            } catch (IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException("Failed to initialize Struct Layout for config [" + this.configId + "] due to an error with the given class [" + clazz.getSimpleName() + "] field [" + field.getName() + "]", e);
            }
        }
    }

    public Object parsedValue() {
        this.optionsProviders.forEach((option, optionValueProvider) -> option.set(optionValueProvider.parsedValue()));

        this.options.values().forEach(FieldOption::synchronizeWithBackingField);

        return backingValue;
    }
}
