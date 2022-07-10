
package io.wispforest.owo.ui.parsing;

import io.wispforest.owo.Owo;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
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
import java.util.*;
import java.util.function.Function;

/**
 * A model of a UI hierarchy parsed from an
 * XML definition. You can use this to create a UI adapter for your
 * screen with {@link #createAdapter(Class, Screen)} as well as expanding
 * templates via {@link #expandTemplate(Class, String, Map)}
 */
public class UIModel {

    private final Element componentsElement;
    private final Map<String, Element> templates;

    private final Deque<ExpansionFrame> expansionStack = new ArrayDeque<>();

    protected UIModel(Element componentsElement, Map<String, Element> templates) {
        this.componentsElement = componentsElement;
        this.templates = templates;
    }

    protected UIModel(Element docElement) {
        docElement.normalize();
        if (!docElement.getNodeName().equals("owo-ui")) {
            throw new UIModelParsingException("Missing owo-ui root element");
        }

        final var children = UIParsing.childElements(docElement);
        if (!children.containsKey("components")) throw new UIModelParsingException("Missing 'components' element in UI model");

        var componentsList = UIParsing.<Element>allChildrenOfType(children.get("components"), Node.ELEMENT_NODE);
        if (componentsList.size() == 1) {
            this.componentsElement = componentsList.get(0);
        } else {
            throw new UIModelParsingException("Invalid number of children in 'components' element - a single child must be declared");
        }

        this.templates = UIParsing.get(children, "templates", UIParsing::childElements).orElse(Collections.emptyMap());
    }

    /**
     * Load the UI model declared in the given file. If the file cannot
     * be found or an XML parsing error occurs, null is
     * returned and the error is logged
     *
     * @param path The file to read from
     * @return The parsed UI model
     */
    public static @Nullable UIModel load(Path path) {
        try (var in = Files.newInputStream(path)) {
            return load(in);
        } catch (Exception error) {
            Owo.LOGGER.warn("Could not load UI model from file {}", path, error);
            return null;
        }
    }

    /**
     * Load the UI model declared in the XML document
     * encoded by the given input stream. Contrary to {@link #load(Path)},
     * this method throws if a parsing error occurs
     *
     * @param stream The input stream to decode and read
     * @return The parsed UI model
     */
    public static UIModel load(InputStream stream) throws ParserConfigurationException, IOException, SAXException, UIModelParsingException {
        return new UIModel(DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().parse(stream).getDocumentElement());
    }

    /**
     * Create a UI adapter which contains the component hierarchy
     * declared by this UI model, attached to the given screen.
     * <p>
     * If there are components in your hierarchy you need to modify in
     * code after the main hierarchy has been parsed, give them an id
     * and look them up via {@link ParentComponent#childById(Class, String)}
     *
     * @param expectedRootComponentClass The class the created root component is expected to have.
     *                                   Should this be violated, an exception is thrown. If there
     *                                   are no specific expectations about the type of
     *                                   root component to create, pass {@link Component}
     */
    public <T extends ParentComponent> OwoUIAdapter<T> createAdapter(Class<T> expectedRootComponentClass, Screen screen) {
        return OwoUIAdapter.create(screen, (horizontalSizing, verticalSizing) -> this.parseComponentTree(expectedRootComponentClass));
    }

    /**
     * Attempt to parse the given XMl element into a component,
     * expanding any templates encountered. If the XML does
     * not describe a valid component, a {@link UIModelParsingException}
     * may be thrown
     *
     * @param expectedClass    The class the parsed component is expected to
     *                         have. Should this be violated, an exception is
     *                         thrown. If there are no specific expectations about
     *                         the type of component to parse, pass {@link Component}
     * @param componentElement The XML element represented the
     *                         component to parse.
     * @return The parsed component
     */
    @SuppressWarnings("unchecked")
    public <T extends Component> T parseComponent(Class<T> expectedClass, Element componentElement) {
        if (componentElement.getNodeName().equals("template")) {
            var templateName = componentElement.getAttribute("name").strip();
            if (templateName.isEmpty()) {
                throw new UIModelParsingException("Template element is missing 'name' attribute");
            }

            var templateParams = new HashMap<String, String>();
            var childParams = new HashMap<String, Element>();
            for (var element : UIParsing.<Element>allChildrenOfType(componentElement, Node.ELEMENT_NODE)) {
                if (element.getNodeName().equals("child")) {
                    childParams.put(
                            element.getAttribute("id"),
                            UIParsing.<Element>allChildrenOfType(element, Node.ELEMENT_NODE).get(0)
                    );
                } else {
                    templateParams.put(element.getNodeName(), element.getTextContent());
                }
            }

            return this.expandTemplate(expectedClass, templateName, templateParams::get, childParams::get);
        }

        var component = UIParsing.getFactory(componentElement).apply(componentElement);
        component.parseProperties(this, componentElement, UIParsing.childElements(componentElement));

        if (!expectedClass.isAssignableFrom(component.getClass())) {
            var idString = componentElement.hasAttribute("id")
                    ? " with id '" + componentElement.getAttribute("id") + "'"
                    : "";

            throw new IncompatibleUIModelException(
                    "Expected component '" + componentElement.getNodeName() + "'"
                            + idString
                            + " to be a " + expectedClass.getSimpleName()
                            + ", but it is a " + component.getClass().getSimpleName()
            );
        }

        return (T) component;
    }

    /**
     * Expand a template into a component, applying
     * parameter mappings by invoking the given mapping
     * function and creating template children using the given
     * child supplier
     *
     * @param expectedClass     The class the expanded template is expected to
     *                          have. Should this be violated, an exception is
     *                          thrown. If there are no specific expectations about
     *                          the type of component to create, pass {@link Component}
     * @param name              The name of the template to expand
     * @param parameterSupplier The parameter mapping function to invoke
     *                          for each parameter encountered in the template
     * @param childSupplier     The template child mapping function to invoke
     *                          for each template child the target template defines
     * @return The expanded template parsed into a component
     */
    @SuppressWarnings("unchecked")
    public <T extends Component> T expandTemplate(Class<T> expectedClass, String name, Function<String, String> parameterSupplier, Function<String, Element> childSupplier) {
        if (this.expansionStack.isEmpty()) {
            this.expansionStack.push(new ExpansionFrame(parameterSupplier, childSupplier));
        } else {
            final var currentFrame = this.expansionStack.peek();
            this.expansionStack.push(new ExpansionFrame(
                    this.cascadeIfNull(currentFrame.parameterSupplier, parameterSupplier),
                    this.cascadeIfNull(currentFrame.childSupplier, childSupplier)
            ));
        }

        var template = (Element) this.templates.get(name);
        if (template == null) {
            throw new UIModelParsingException("Unknown template '" + name + "'");
        } else {
            template = (Element) template.cloneNode(true);
        }

        this.expandChildren(template);
        this.applySubstitutions(template);

        final var component = this.parseComponent(Component.class, UIParsing.<Element>allChildrenOfType(template, Node.ELEMENT_NODE).get(0));
        if (!expectedClass.isAssignableFrom(component.getClass())) {
            throw new IncompatibleUIModelException(
                    "Expected template '" + name + "'"
                            + " to expand into a " + expectedClass.getSimpleName()
                            + ", but it expanded into a " + component.getClass().getSimpleName()
            );
        }

        this.expansionStack.pop();
        return (T) component;
    }

    /**
     * Expand a template into a component, applying
     * the given parameter mappings. If the template defines child
     * elements, this method will most likely fail because
     * parameters for those can only be provided in XML
     *
     * @param expectedClass The class the expanded template is expected to
     *                      have. Should this be violated, an exception is
     *                      thrown. If there are no specific expectations about
     *                      the type of component to create, pass {@link Component}
     * @param name          The name of the template to expand
     * @param parameters    The parameter mappings to apply while
     *                      expanding the template
     * @return The expanded template parsed into a component
     */
    public <T extends Component> T expandTemplate(Class<T> expectedClass, String name, Map<String, String> parameters) {
        return this.expandTemplate(expectedClass, name, parameters::get, s -> null);
    }

    protected <T extends ParentComponent> T parseComponentTree(Class<T> expectedRootComponentClass) {
        var documentComponent = this.parseComponent(expectedRootComponentClass, this.componentsElement);
        documentComponent.sizing(Sizing.fill(100), Sizing.fill(100));
        return documentComponent;
    }

    protected void applySubstitutions(Element template) {
        final var parameterSupplier = this.expansionStack.peek().parameterSupplier;

        for (var child : UIParsing.<Element>allChildrenOfType(template, Node.ELEMENT_NODE)) {
            for (var node : UIParsing.<Text>allChildrenOfType(child, Node.TEXT_NODE)) {
                var textContent = node.getTextContent();
                if (!textContent.matches("\\{\\{.*}}")) continue;

                final var substitution = parameterSupplier.apply(textContent.substring(2, textContent.length() - 2));
                if (substitution != null) {
                    node.setTextContent(substitution);
                }
            }
            applySubstitutions(child);
        }
    }

    protected void expandChildren(Element template) {
        final var childSupplier = this.expansionStack.peek().childSupplier;

        for (var child : UIParsing.<Element>allChildrenOfType(template, Node.ELEMENT_NODE)) {
            if (child.getNodeName().equals("template-child")) {
                var childId = child.getAttribute("id");

                var expanded = childSupplier.apply(childId);
                if (expanded != null) {
                    expanded = (Element) expanded.cloneNode(true);
                    for (var element : UIParsing.<Element>allChildrenOfType(child, Node.ELEMENT_NODE)) {
                        if (expanded.getElementsByTagName(element.getNodeName()).getLength() != 0) continue;
                        expanded.appendChild(element);
                    }

                    template.replaceChild(expanded, child);
                }
            }

            expandChildren(child);
        }
    }

    protected <T, S> Function<T, S> cascadeIfNull(Function<T, S> first, Function<T, S> second) {
        return t -> {
            var firstValue = first.apply(t);
            return firstValue == null ? second.apply(t) : firstValue;
        };
    }

    private record ExpansionFrame(Function<String, String> parameterSupplier, Function<String, Element> childSupplier) {}
}