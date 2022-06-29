package io.wispforest.owo.ui.parse;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.OwoUIAdapter;
import io.wispforest.owo.ui.component.ErrorComponent;
import io.wispforest.owo.ui.definitions.Component;
import io.wispforest.owo.ui.definitions.ParentComponent;
import io.wispforest.owo.ui.definitions.Sizing;
import net.minecraft.client.gui.screen.Screen;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A specification of a UI hierarchy parsed from an
 * XML definition. You can use this to create a UI adapter for your
 * screen with {@link #createAdapter(Screen)} as well as expanding
 * templates via {@link #expandTemplate(String, Map)}
 */
public class OwoUISpec {

    private final Element componentsElement;
    private final Map<String, Element> templates;

    protected OwoUISpec(Element componentsElement, Map<String, Element> templates) {
        this.componentsElement = componentsElement;
        this.templates = templates;
    }

    protected OwoUISpec(Element docElement) {
        docElement.normalize();
        if (!docElement.getNodeName().equals("owo-ui")) {
            throw new UIParsingException("");
        }

        final var children = OwoUIParsing.childElements(docElement);
        if (!children.containsKey("components")) throw new UIParsingException("Missing 'components' element in UI specification");

        var componentsList = OwoUIParsing.<Element>allChildrenOfType(children.get("components"), Node.ELEMENT_NODE);
        if (componentsList.size() == 1) {
            this.componentsElement = componentsList.get(0);
        } else {
            throw new UIParsingException("Invalid number of children in 'components' element - a single child must be declared");
        }

        this.templates = OwoUIParsing.get(children, "templates", OwoUIParsing::childElements).orElse(Collections.emptyMap());
    }

    /**
     * Load the UI specification declared in the given file. If the file cannot
     * be found or an XML parsing error occurs, and empty specification is
     * returned and an error is logged
     *
     * @param path The file to read from
     * @return The parsed UI specification
     */
    public static OwoUISpec load(Path path) {
        try (var in = Files.newInputStream(path)) {
            return load(in);
        } catch (Exception error) {
            Owo.LOGGER.warn("Could not load UI spec from file {}", path, error);
            return Empty.INSTANCE;
        }
    }

    /**
     * Load the UI specification declared in the XML document
     * encoded by the given input stream. Contrary to {@link #load(Path)},
     * this method throws if a parsing error occurs
     *
     * @param stream The input stream to decode and read
     * @return The parsed UI specification
     */
    public static OwoUISpec load(InputStream stream) throws ParserConfigurationException, IOException, SAXException, UIParsingException {
        return new OwoUISpec(DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().parse(stream).getDocumentElement());
    }

    /**
     * Create a UI adapter which contains the component hierarchy
     * declared by this UI specification, attached to the given screen.
     * <p>
     * If there are components in your hierarchy you need to modify in
     * code after the main hierarchy has been parsed, give them an id
     * and look them up via {@link ParentComponent#childById(String)}
     */
    public OwoUIAdapter<ParentComponent> createAdapter(Screen screen) {
        return OwoUIAdapter.create(screen, (horizontalSizing, verticalSizing) -> {
            try {
                return this.parseComponentTree();
            } catch (UIParsingException e) {
                Owo.LOGGER.warn("Parsing UI specification failed", e);
                return ErrorComponent.create(e);
            }
        });
    }

    /**
     * Attempt to parse the given XMl element into a component,
     * expanding any templates encountered. If the XML does
     * not describe a valid component, a {@link UIParsingException}
     * may be thrown
     *
     * @param componentElement The XML element represented the
     *                         component to parse.
     * @return The parsed component
     */
    public Component parseComponent(Element componentElement) {
        if (componentElement.getNodeName().equals("template")) {
            var templateName = componentElement.getAttribute("name").strip();
            if (templateName.isEmpty()) {
                throw new UIParsingException("Template element is missing 'name' attribute");
            }

            var templateParams = new HashMap<String, String>();
            for (var entry : OwoUIParsing.childElements(componentElement).entrySet()) {
                templateParams.put(entry.getKey(), entry.getValue().getTextContent());
            }

            return expandTemplate(templateName, templateParams);
        }

        var component = OwoUIParsing.getFactory(componentElement).apply(componentElement);
        component.parseProperties(this, componentElement, OwoUIParsing.childElements(componentElement));
        return component;
    }

    /**
     * Expand a template into a component, applying
     * the given parameter mappings
     *
     * @param name       The name of the template to expand
     * @param parameters The parameter mappings to apply while
     *                   expanding the template
     * @return The expanded template parsed into a component
     */
    public Component expandTemplate(String name, Map<String, String> parameters) {
        return expandTemplate(name, s -> parameters.getOrDefault(s, s));
    }

    /**
     * Expand a template into a component, applying
     * parameter mappings by invoking the given mapping
     * function
     *
     * @param name              The name of the template to expand
     * @param parameterSupplier The parameter mapping function to invoke
     *                          for each parameter encountered in the template
     * @return The expanded template parsed into a component
     */
    public Component expandTemplate(String name, Function<String, String> parameterSupplier) {
        var template = (Element) this.templates.get(name);
        if (template == null) {
            return ErrorComponent.create("Unknown template '" + name + "'");
        } else {
            template = (Element) template.cloneNode(true);
        }

        applySubstitutions(template, parameterSupplier);

        return this.parseComponent(OwoUIParsing.<Element>allChildrenOfType(template, Node.ELEMENT_NODE).get(0));
    }

    protected ParentComponent parseComponentTree() {
        var documentComponent = this.parseComponent(this.componentsElement);
        if (!(documentComponent instanceof ParentComponent rootComponent)) {
            throw new UIParsingException("The root component must be a parent component");
        }

        rootComponent.sizing(Sizing.fill(100), Sizing.fill(100));
        return rootComponent;
    }

    protected static void applySubstitutions(Element element, Function<String, String> substitutionsSupplier) {
        for (var child : OwoUIParsing.<Element>allChildrenOfType(element, Node.ELEMENT_NODE)) {
            for (var node : OwoUIParsing.<Text>allChildrenOfType(child, Node.TEXT_NODE)) {
                var textContent = node.getTextContent();
                if (!textContent.matches("\\{\\{.*}}")) continue;

                node.setTextContent(substitutionsSupplier.apply(textContent.substring(2, textContent.length() - 2)));
            }
            applySubstitutions(child, substitutionsSupplier);
        }
    }

    private static class Empty extends OwoUISpec {

        private static final Empty INSTANCE = new Empty();

        private Empty() {
            super(null, Collections.emptyMap());
        }

        @Override
        public ParentComponent parseComponentTree() {
            return ErrorComponent.create("Could not load UI spec file");
        }
    }
}