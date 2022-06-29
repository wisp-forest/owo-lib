package io.wispforest.owo.ui.parse;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.definitions.Component;
import io.wispforest.owo.ui.definitions.Sizing;
import io.wispforest.owo.ui.layout.Layouts;
import io.wispforest.owo.ui.layout.ScrollContainer;
import net.minecraft.text.Text;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class OwoUIParsing {

    private static final Map<String, Function<Element, Component>> COMPONENT_FACTORIES = new HashMap<>();

    public static void registerFactory(String componentTagName, Function<Element, Component> factory) {
        if (COMPONENT_FACTORIES.containsKey(componentTagName)) {
            throw new IllegalStateException("A component factory with name " + componentTagName + " is already registered");
        }

        COMPONENT_FACTORIES.put(componentTagName, factory);
    }

    public static Function<Element, Component> getFactory(Element element) {
        var factory = COMPONENT_FACTORIES.get(element.getNodeName());
        if (factory == null) {
            throw new UIParsingException("Unknown component type: " + element.getNodeName());
        }

        return factory;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Node> List<T> allChildrenOfType(Element element, short type) {
        var list = new ArrayList<T>();
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            var child = element.getChildNodes().item(i);
            if (child.getNodeType() != type) continue;
            list.add((T) child);
        }
        return list;
    }

    public static Map<String, Element> childElements(Element element) {
        var children = element.getChildNodes();
        var map = new HashMap<String, Element>();

        for (int i = 0; i < children.getLength(); i++) {
            var child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) continue;

            if (map.containsKey(child.getNodeName())) {
                throw new UIParsingException("Duplicate child " + child.getNodeName() + " in element " + element.getNodeName());
            }

            map.put(child.getNodeName(), (Element) child);
        }

        return map;
    }

    public static int parseColor(Element element) {
        var text = element.getTextContent().strip();
        if (text.matches("#([A-Fa-f\\d]{2}){3,4}")) {
            return Integer.parseUnsignedInt(text.substring(1), 16);
        } else {
            throw new UIParsingException("Invalid color value '" + text + "', expected hex color of format #RRGGBB or #AARRGGBB");
        }
    }

    public static int parseSignedInt(Element element) {
        return parseInt(element, true);
    }

    public static int parseUnsignedInt(Element element) {
        return parseInt(element, false);
    }

    public static int parseInt(Element element, boolean allowNegative) {
        var data = element.getTextContent().strip();
        if (data.matches((allowNegative ? "-?" : "") + "\\d+")) {
            return Integer.parseInt(data);
        } else {
            throw new UIParsingException("Invalid value '" + data + "', expected " + (allowNegative ? "" : "positive") + " integer");
        }
    }

    public static boolean parseBool(Element element) {
        return element.getTextContent().strip().equalsIgnoreCase("true");
    }

    public static Text parseText(Element element) {
        return element.getAttribute("translate").equalsIgnoreCase("true")
                ? Text.translatable(element.getTextContent())
                : Text.literal(element.getTextContent());
    }

    public static <T> Optional<T> get(Map<String, Element> properties, String key, Parser<T> parser) {
        if (!properties.containsKey(key)) return Optional.empty();
        return Optional.of(parser.parse(properties.get(key)));
    }

    public static <T> void apply(Map<String, Element> properties, String key, Parser<T> parser, Consumer<T> consumer) {
        if (!properties.containsKey(key)) return;
        consumer.accept(parser.parse(properties.get(key)));
    }

    public interface Parser<T> {
        T parse(Element element);
    }

    static {
        registerFactory("flow-layout", element -> {
            return element.getAttribute("direction").equals("vertical")
                    ? Layouts.verticalFlow(Sizing.content(), Sizing.content())
                    : Layouts.horizontalFlow(Sizing.content(), Sizing.content());
        });

        registerFactory("scroll", element -> {
            return element.getAttribute("direction").equals("vertical")
                    ? ScrollContainer.vertical(Sizing.content(), Sizing.content(), null)
                    : ScrollContainer.horizontal(Sizing.content(), Sizing.content(), null);
        });

        registerFactory("label", element -> Components.label(Text.empty()));
        registerFactory("button", element -> Components.button(Text.empty(), button -> {}));
        registerFactory("text-box", element -> Components.textBox(Sizing.content()));
        registerFactory("slider", element -> Components.slider(Sizing.content(), Text.empty()));
    }

}
