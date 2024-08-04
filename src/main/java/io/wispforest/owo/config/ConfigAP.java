package io.wispforest.owo.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Hook;
import io.wispforest.owo.config.annotation.Nest;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@ApiStatus.Internal
@SupportedAnnotationTypes("io.wispforest.owo.config.annotation.Config")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class ConfigAP extends AbstractProcessor {

    private static final String WRAPPER_TEMPLATE = """
            package {package};

            import blue.endless.jankson.Jankson;
            import io.wispforest.owo.config.ConfigWrapper;
            import io.wispforest.owo.config.Option;
            import io.wispforest.owo.util.Observable;

            import java.util.HashMap;
            import java.util.Map;
            import java.util.function.Consumer;

            public class {wrapper_class_name} extends ConfigWrapper<{config_class_name}> {

                public final Keys keys = new Keys();

            {option_instances}

                private {wrapper_class_name}() {
                    super({config_class_name}.class);
                }

                private {wrapper_class_name}(Consumer<Jankson.Builder> janksonBuilder) {
                    super({config_class_name}.class, janksonBuilder);
                }

                public static {wrapper_class_name} createAndLoad() {
                    var wrapper = new {wrapper_class_name}();
                    wrapper.load();
                    return wrapper;
                }

                public static {wrapper_class_name} createAndLoad(Consumer<Jankson.Builder> janksonBuilder) {
                    var wrapper = new {wrapper_class_name}(janksonBuilder);
                    wrapper.load();
                    return wrapper;
                }

            {accessors}

            {type_interfaces}

                public static class Keys {
            {key_constants}
                }
            }
            """;

    private static final String GET_ACCESSOR_TEMPLATE = """
            public {field_type} {field_name}() {
                return {option_instance}.value();
            }
            """;

    private static final String SET_ACCESSOR_TEMPLATE = """
            public void {field_name}({field_type} value) {
                {option_instance}.set(value);
            }
            """;

    private static final String SUBSCRIBE_TEMPLATE = """
            public void subscribeTo{field_name}(Consumer<{field_type}> subscriber) {
                {option_instance}.observe(subscriber);
            }
            """;

    private final Set<TypeElement> nestTypes = new LinkedHashSet<>();
    private Map<TypeMirror, TypeMirror> primitivesToWrappers;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        final var typeUtils = processingEnv.getTypeUtils();
        final var elementUtils = processingEnv.getElementUtils();

        this.primitivesToWrappers = Map.of(
                typeUtils.getPrimitiveType(TypeKind.BYTE), elementUtils.getTypeElement("java.lang.Byte").asType(),
                typeUtils.getPrimitiveType(TypeKind.CHAR), elementUtils.getTypeElement("java.lang.Character").asType(),
                typeUtils.getPrimitiveType(TypeKind.SHORT), elementUtils.getTypeElement("java.lang.Short").asType(),
                typeUtils.getPrimitiveType(TypeKind.INT), elementUtils.getTypeElement("java.lang.Integer").asType(),
                typeUtils.getPrimitiveType(TypeKind.LONG), elementUtils.getTypeElement("java.lang.Long").asType(),
                typeUtils.getPrimitiveType(TypeKind.FLOAT), elementUtils.getTypeElement("java.lang.Float").asType(),
                typeUtils.getPrimitiveType(TypeKind.DOUBLE), elementUtils.getTypeElement("java.lang.Double").asType(),
                typeUtils.getPrimitiveType(TypeKind.BOOLEAN), elementUtils.getTypeElement("java.lang.Boolean").asType()
        );
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (var annotation : annotations) {
            var annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            for (var annotated : annotatedElements) {
                if (annotated.getKind() != ElementKind.CLASS) continue;

                var clazz = (TypeElement) annotated;
                var className = clazz.getQualifiedName().toString();
                var wrapperName = annotated.getAnnotation(Config.class).wrapperName();

                try {
                    var file = this.processingEnv.getFiler().createSourceFile(wrapperName);
                    try (var writer = new PrintWriter(file.openWriter())) {
                        writer.println(makeWrapper(wrapperName, className, this.collectFields(Option.Key.ROOT, clazz, clazz.getAnnotation(Config.class).defaultHook())));
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to generate config wrapper", e);
                }
            }
        }

        return true;
    }

    private List<ConfigField> collectFields(Option.Key parent, TypeElement clazz, boolean defaultHook) {
        var messager = this.processingEnv.getMessager();
        var list = new ArrayList<ConfigField>();

        for (var field : clazz.getEnclosedElements()) {
            if (field.getKind() != ElementKind.FIELD) continue;

            var fieldType = field.asType();
            var fieldName = field.getSimpleName().toString();

            if (fieldType.getKind() == TypeKind.TYPEVAR) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Generic field types are not allowed in config classes");
            }

            TypeElement typeElement = null;
            if (fieldType.getKind() == TypeKind.DECLARED) {
                typeElement = (TypeElement) ((DeclaredType) fieldType).asElement();

                if (typeElement == clazz) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Illegal self-reference in nested config object");
                }
            }

            if (typeElement != null && field.getAnnotation(Nest.class) != null) {
                this.nestTypes.add(typeElement);
                list.add(new NestField(fieldName, collectFields(parent.child(fieldName), typeElement, defaultHook), typeElement.getSimpleName().toString()));
            } else {
                list.add(new ValueField(fieldName, parent.child(fieldName), field.asType(),
                        defaultHook || field.getAnnotation(Hook.class) != null));
            }
        }

        return list;
    }

    private String makeWrapper(String wrapperClassName, String configClassName, List<ConfigField> fields) {
        var baseWrapper = WRAPPER_TEMPLATE
                .replace("{wrapper_class_name}", wrapperClassName)
                .replace("{package}", configClassName.substring(0, configClassName.lastIndexOf(".")))
                .replace("{config_class_name}", configClassName);

        var accessorMethods = new Writer(new StringBuilder());
        var optionInstances = new Writer(new StringBuilder());
        var keyConstants = new Writer(new StringBuilder());
        var typeInterfaces = new Writer(new StringBuilder());

        for (var nestType : this.nestTypes) {
            typeInterfaces.beginLine("public interface ").write(nestType.getSimpleName().toString()).endLine(" {");
            typeInterfaces.beginBlock();
            for (var enclosed : nestType.getEnclosedElements()) {
                if (enclosed.getKind() != ElementKind.FIELD) continue;
                if (enclosed.getAnnotation(Nest.class) != null) continue;

                typeInterfaces.beginLine(enclosed.asType().toString()).write(" ").write(enclosed.getSimpleName().toString()).endLine("();");
                typeInterfaces.beginLine("void ").write(enclosed.getSimpleName().toString()).write("(").write(enclosed.asType().toString()).endLine(" value);");
            }
            typeInterfaces.endBlock();
            typeInterfaces.line("}");
        }

        keyConstants.beginBlock();
        for (var field : fields) {
            field.appendAccessors(accessorMethods, optionInstances, keyConstants);
        }

        return baseWrapper
                .replace("{option_instances}", optionInstances.finish())
                .replace("{type_interfaces}\n", typeInterfaces.finish())
                .replace("{key_constants}", keyConstants.finish())
                .replace("{accessors}\n", accessorMethods.finish());
    }

    private String makeGetAccessor(String fieldName, Option.Key fieldKey, TypeMirror fieldType) {
        return GET_ACCESSOR_TEMPLATE
                .replace("{option_instance}", constantNameOf(fieldKey))
                .replace("{field_name}", fieldName)
                .replace("{field_type}", fieldType.toString());
    }

    private String makeSetAccessor(String fieldName, Option.Key fieldKey, TypeMirror fieldType) {
        return SET_ACCESSOR_TEMPLATE
                .replace("{option_instance}", constantNameOf(fieldKey))
                .replace("{field_name}", fieldName)
                .replace("{field_type}", fieldType.toString());
    }

    private String makeSubscribe(String fieldName, Option.Key fieldKey, TypeMirror fieldType) {
        return SUBSCRIBE_TEMPLATE
                .replace("{option_instance}", constantNameOf(fieldKey))
                .replace("{field_name}", fieldName)
                .replace("{field_type}", this.primitivesToWrappers.getOrDefault(fieldType, fieldType).toString());
    }

    private String constantNameOf(Option.Key key) {
        return key.asString().replace(".", "_");
    }

    private interface ConfigField {
        void appendAccessors(Writer accessors, Writer optionInstances, Writer keyConstants);
    }

    private final class ValueField implements ConfigField {
        private final String name;
        private final Option.Key key;
        private final TypeMirror type;
        private final boolean makeSubscribe;

        private ValueField(String name, Option.Key key, TypeMirror type, boolean makeSubscribe) {
            this.name = name;
            this.key = key;
            this.type = type;
            this.makeSubscribe = makeSubscribe;
        }

        @Override
        public void appendAccessors(Writer accessors, Writer optionInstances, Writer keyConstants) {
            keyConstants.line("public final Option.Key " + constantNameOf(this.key) + " = new Option.Key(\"" + this.key.asString() + "\");");
            optionInstances.line("private final Option<" + primitivesToWrappers.getOrDefault(type, type) + "> " + constantNameOf(this.key) + " = this.optionForKey(this.keys." + constantNameOf(this.key) + ");");

            accessors.append(makeGetAccessor(this.name, this.key, this.type)).write("\n");
            accessors.append(makeSetAccessor(this.name, this.key, this.type)).write("\n");
            if (this.makeSubscribe) accessors.append(makeSubscribe(capitalize(this.name), this.key, this.type)).write("\n");
        }
    }

    private record NestField(String nestName, List<ConfigField> children, String typeName) implements ConfigField {
        @Override
        public void appendAccessors(Writer accessors, Writer optionInstances, Writer keyConstants) {
            var nestClassName = capitalize(nestName);
            if (nestClassName.equals(typeName)) nestClassName += "_";

            // TODO replace type interface with class and instantiate instead of one class per field

            accessors.beginLine("public final ").write(nestClassName).write(" ").write(nestName).write(" = new ").write(nestClassName).endLine("();");
            accessors.beginLine("public class ").write(nestClassName).write(" implements ").write(typeName).endLine(" {");
            accessors.beginBlock();
            for (var child : children) {
                child.appendAccessors(accessors, optionInstances, keyConstants);
            }
            accessors.endBlock();
            accessors.line("}");
        }
    }

    private static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase(Locale.ROOT) + string.substring(1);
    }

    private static class Writer implements CharSequence {

        private final StringBuilder builder;
        private int indentLevel = 1;

        private Writer(StringBuilder builder) {
            this.builder = builder;
        }

        public Writer beginLine(CharSequence text) {
            this.builder.append(" ".repeat(this.indentLevel * 4)).append(text);
            return this;
        }

        public void endLine(CharSequence text) {
            this.builder.append(text).append("\n");
        }

        public void line(CharSequence text) {
            this.builder.append("    ".repeat(this.indentLevel)).append(text).append("\n");
        }

        public Writer append(String text) {
            for (var line : text.split("\n")) {
                this.line(line);
            }

            return this;
        }

        public Writer write(CharSequence text) {
            this.builder.append(text);
            return this;
        }

        public void beginBlock() {
            this.indentLevel++;
        }

        public void endBlock() {
            this.indentLevel--;
        }

        public String finish() {
            if (this.builder.isEmpty()) return "";
            if (this.builder.charAt(builder.length() - 1) == '\n') {
                this.builder.deleteCharAt(this.builder.length() - 1);
            }

            return this.builder.toString();
        }

        @Override
        public int length() {
            return this.builder.length();
        }

        @Override
        public char charAt(int index) {
            return this.builder.charAt(index);
        }

        @Override
        public @NotNull CharSequence subSequence(int start, int end) {
            return this.builder.subSequence(start, end);
        }

        @Override
        public @NotNull String toString() {
            return this.builder.toString();
        }
    }
}
