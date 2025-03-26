package io.wispforest.owo.config.ui.component.struct;

import io.wispforest.owo.config.*;
import io.wispforest.owo.config.base.BoundedAccess;
import io.wispforest.owo.config.options.OptionControlSpec;
import io.wispforest.owo.config.options.RecordOption;
import io.wispforest.owo.config.ui.component.OptionValueProvider;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.util.ReflectionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.LinkedHashMap;
import java.util.function.Function;

public class RecordStructOptionContainer<T extends Record> extends AbstractStructOptionContainer<RecordOption> {

    private Function<Object[], T> canonicalConstructor;

    protected T backingValue;

    protected RecordStructOptionContainer(UIModel uiModel, OptionControlSpec<?> option, Class<T> clazz, T value) {
        super(Sizing.expand(), Sizing.content(), uiModel, option);

        this.buildContainer(clazz, value);
    }

    public static <T extends Record> RecordStructOptionContainer<T> of(UIModel uiModel, OptionControlSpec<T> option) {
        return of(uiModel, option, option.clazz(), option.value());
    }

    public static <T extends Record> RecordStructOptionContainer<T> of(UIModel uiModel, OptionControlSpec<?> option, Class<T> clazz, T value) {
        return new RecordStructOptionContainer<T>(uiModel, option, clazz, value);
    }

    public void buildContainer(Class<T> clazz, T value) {
        var lookup = MethodHandles.publicLookup();

        var fields = new LinkedHashMap<RecordComponent, Function<Record, Object>>();
        var canonicalConstructorArgs = new Class[clazz.getRecordComponents().length];

        for (int i = 0; i < clazz.getRecordComponents().length; ++i) {
            try {
                var component = clazz.getRecordComponents()[i];
                var handle = lookup.unreflect(component.getAccessor());

                fields.put(component, (instance) -> getRecordEntry(instance, handle));
                canonicalConstructorArgs[i] = component.getType();
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Failed to create method handle for record component accessor", e);
            }
        }

        try {
            var constructor = clazz.getConstructor(canonicalConstructorArgs);

            this.canonicalConstructor = fieldValues -> {
                try {
                    return constructor.newInstance(fieldValues);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException var8) {
                    throw new IllegalStateException("Error while deserializing record", var8);
                }
            };
        } catch (NoSuchMethodException var9) {
            throw new IllegalStateException("Could not locate canonical record constructor");
        }

        this.backingValue = this.canonicalConstructor.apply(fields.values().stream().map(func -> func.apply(value)).toArray());

        //--

        //this.sideBySideFormat(fields.size() >= 2);

        var defaultValue = ReflectionUtils.tryInstantiateWithNoArgs(clazz);

        this.sideBySideFormating(sideBySideFormat && fields.size() > 1);

        for (var entry : fields.entrySet()) {
            var component = entry.getKey();

            var boundField = new BoundedAccess.BoundRecordComponent<>(backingValue, component, entry.getValue());

            try {
                var innerOption = new RecordOption<>(
                        configId,
                        optionKey.child(boundField.component().getName()),
                        boundField.withOwner(defaultValue).getValue(),
                        boundField,
                        ConfigReflectionUtils.getConstraint(boundField),
                        boundField.getValue()
                );

                options.put(innerOption.key(), innerOption);

                this.addOptionComponent(clazz, innerOption);
            } catch (IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException("Failed to initialize Struct Layout for config [" + this.configId + "] due to an error with field [" + component.getName() + "]", e);
            }
        }
    }

    private static <R extends Record> Object getRecordEntry(R instance, MethodHandle accessor) {
        try {
            return accessor.invoke(instance);
        } catch (Throwable e) {
            throw new IllegalStateException("Unable to get record component value", e);
        }
    }

    public Object parsedValue() {
        var objects = this.optionsProviders.values().stream().map(OptionValueProvider::parsedValue).toArray();
        return this.canonicalConstructor.apply(objects);
    }
}
