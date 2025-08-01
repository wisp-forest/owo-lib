package io.wispforest.owo.config.ui;

import blue.endless.jankson.JsonObject;
import io.wispforest.owo.Owo;
import io.wispforest.owo.config.*;
import io.wispforest.owo.config.annotation.*;
import io.wispforest.owo.config.base.Key;
import io.wispforest.owo.config.options.FieldOption;
import io.wispforest.owo.config.ui.component.*;
import io.wispforest.owo.packets.OwoPackets;
import io.wispforest.owo.packets.c2s.AskToOpenServerConfig;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.*;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.owo.util.NumberReflection;
import io.wispforest.owo.util.ReflectionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.function.Predicate;

/**
 * A screen which generates components for each option in the
 * provided config. The general structure of the screen is determined
 * by the XML config model it uses - the default one is located at
 * {@code assets/owo/owo_ui/config.xml}. Changing which model is used
 * via {@link #createWithCustomModel(Identifier, ConfigWrapper, Screen)}
 * can often be enough to visually customize the generated screen - should
 * you need custom functionality however, extending this class is usually
 * your best bet
 *
 * @see io.wispforest.owo.config.annotation.Modmenu
 * @see ConfigWrapper
 */
public class ConfigScreen extends BaseUIModelScreen<FlowLayout> {

    public static final Identifier DEFAULT_MODEL_ID = Identifier.of("owo", "config");

    private static final Map<Predicate<FieldOption<?>>, OptionComponentFactory<?>> DEFAULT_FACTORIES = new LinkedHashMap<>();
    /**
     * A set of extra option factories - add to this if you want to override
     * some default factories or add extra ones for specific config options
     * the standard ones don't support
     */
    protected final Map<Predicate<FieldOption<?>>, OptionComponentFactory<?>> extraFactories = new LinkedHashMap<>();

    public final Screen parent;
    protected final ConfigWrapper<?> config;
    @SuppressWarnings("rawtypes") protected final Map<FieldOption, OptionValueProvider> options = new HashMap<>();

    protected String lastSearchFieldText = "";
    protected @Nullable SearchMatches currentMatches = null;
    protected int currentMatchIndex = 0;

    @Nullable
    protected ConfigWrapper<?> serverConfig = null;

    protected ConfigScreen(Identifier modelId, ConfigWrapper<?> config, @Nullable Screen parent) {
        super(FlowLayout.class, DataSource.asset(modelId));
        this.parent = parent;
        this.config = config;
    }

    protected Map<Identifier, JsonObject> serverConfigData = Map.of();

    protected Map<String, LabelComponent> prevLabels = Map.of();
    protected Map<String, LabelComponent> currentLabels = new HashMap<>();

    protected double prevScrollProgress = -1;

    protected ConfigScreen setConfigScreenData(ConfigScreen prevScreen) {
        this.prevLabels = prevScreen.currentLabels;
        this.prevScrollProgress = prevScreen.uiAdapter.rootComponent.childById(ScrollContainer.class, "titles-scroll").scrollProgress();

        return this;
    }

    protected ConfigScreen setServerConfigData(Map<Identifier, JsonObject> configData) {
        this.serverConfigData = configData;

        return this;
    }

    /**
     * Create a config screen with the default model ({@code owo:config})
     *
     * @param config The config to create a screen for
     * @param parent The parent screen to return to
     *               when the created screen is closed
     */
    public static ConfigScreen create(ConfigWrapper<?> config, @Nullable Screen parent) {
        return createWithCustomModel(DEFAULT_MODEL_ID, config, parent);
    }

    public static ConfigScreen create(ConfigWrapper<?> config, @Nullable Screen parent, ConfigComponentBuilder builder) {
        return createWithCustomModel(DEFAULT_MODEL_ID, config, parent, builder);
    }

    /**
     * Create a config screen with a custom model
     * located in your mod's assets
     *
     * @param modelId The ID of the model to use
     * @param config  The config to create a screen for
     * @param parent  The parent screen to return to
     *                when the created screen is closed
     */
    public static ConfigScreen createWithCustomModel(Identifier modelId, ConfigWrapper<?> config, @Nullable Screen parent) {
        return new ConfigScreen(modelId, config, parent);
    }

    public static ConfigScreen createWithCustomModel(Identifier modelId, ConfigWrapper<?> config, @Nullable Screen parent, ConfigComponentBuilder builder) {
        var screen = createWithCustomModel(modelId, config, parent);
        builder.build(config, new ComponentFactoryRegister() {
            @Override
            public void register(FieldOption<?> option, OptionComponentFactory<?> factory) {
                if (!config.allOptions().containsKey(option.key())) {
                    throw new IllegalStateException("Option Component Factory was registered for an option not found within the config!");
                }

                registerPredicate(option1 -> option1.equals(option), factory);
            }

            @Override
            public void registerPredicate(Predicate<FieldOption<?>> predicate, OptionComponentFactory<?> factory) {
                screen.extraFactories.put(predicate, factory);
            }
        });
        return screen;
    }

    public interface ConfigComponentBuilder {
        void build(ConfigWrapper<?> wrapper, ComponentFactoryRegister registerCallback);
    }

    public interface ComponentFactoryRegister {
        void register(FieldOption<?> option, OptionComponentFactory<?> factory);

        void registerPredicate(Predicate<FieldOption<?>> predicate, OptionComponentFactory<?> factory);
    }

    @Nullable
    private ConfigTranslationHelper.TranslationsStorage translationStorage = null;

    @Override
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    protected void build(FlowLayout rootComponent) {
        this.options.clear();

        var btn = rootComponent.childById(ToggleButton.class, "environment-type");

        var minecraft = MinecraftClient.getInstance();

        if ((minecraft.getServer() != null || minecraft.world == null) || !minecraft.player.hasPermissionLevel(3)) {
            rootComponent.childById(ParentComponent.class, "button-config-controls")
                    .removeChild(btn);
        } else {
            // True -> Server
            // False -> Client
            btn.enabled(this.config.isServerConfig());

            btn.onPress((toggleBtn, isServerConfig) -> {
                if (!minecraft.player.hasPermissionLevel(3)) {
                    toggleBtn.rollbackPress();

                    return;
                }

                var configId = this.config.id();

                if (isServerConfig && this.serverConfig == null) {
                    OwoPackets.MAIN.clientHandle().send(new AskToOpenServerConfig(configId));
                    return;
                }

                var configWrapper = isServerConfig ? this.serverConfig : ConfigWrapper.getConfig(configId);

                if (configWrapper == null) {
                    throw new IllegalStateException("Unable to transfer to the desired environment [" + (isServerConfig ? "Server" : "Client") + "] for the given config: " + configId);
                }

                var newScreen = ConfigScreenProviders.get(configId).openScreenSafely(this.parent, configWrapper);

                if (newScreen instanceof ConfigScreen configScreen) {
                    configScreen.serverConfig = this.serverConfig;
                }

                minecraft.setScreen(newScreen);
            });

            btn.tooltip(
                    Text.translatable("text.owo.config.label.selected.environment")
                            .append(Text.translatable("text.owo.config.label.environment." + (!config.isServerConfig() ? "server" : "client")))
            );
        }

        var topHolder = rootComponent.childById(FlowLayout.class, "titles-and-option-holder");

        var titles = topHolder.childById(FlowLayout.class, "titles");

        var modProviders = ConfigScreenProviders.getSortedProviders()
                .get(config.id().getNamespace());

        if (modProviders.isEmpty()) {
            var titleKey = ConfigTranslationHelper.createConfigTitleTranslation(this.config.id());

            var titleHolder = this.model.expandTemplate(FlowLayout.class, "current-config-selection", Map.of("title-translation-key", titleKey));

            OptionComponentFactory.addEasyCopyLabel(titleHolder, titleKey);

            titles.child(titleHolder);
        } else {
            var titleScroll = topHolder.childById(ScrollContainer.class, "titles-scroll");

            for (var configName : modProviders) {
                var titleKey = ConfigTranslationHelper.createConfigTitleTranslation(this.config.id().withPath(configName));

                ParentComponent titleHolder;

                if (this.config.name().equals(configName)) {
                    titleHolder = this.model.expandTemplate(FlowLayout.class, "current-config-selection", Map.of("title-translation-key", titleKey));

                    OptionComponentFactory.addEasyCopyLabel((FlowLayout) titleHolder, titleKey);

                    titles.child(titleHolder);
                } else {
                    titleHolder = this.model.expandTemplate(SelectableContainer.class, "alternative-config-selection", Map.of("title-translation-key", titleKey));

                    OptionComponentFactory.addEasyCopyLabel(titleHolder.childById(FlowLayout.class, "alternative-config-title-holder"), titleKey);

                    titles.child(titleHolder);

                    titleHolder.mouseDown().subscribe((mouseX, mouseY, button) -> {
                        if (ConfigScreenProviders.safelyOpenConfigScreen(this.config.id().withPath(configName), parent, this)) {
                            UISounds.playButtonSound();

                            return true;
                        }

                        return false;
                    });

                    titleHolder.keyPress().subscribe((keyCode, scanCode, modifiers) -> {
                        if (keyCode == GLFW.GLFW_KEY_ENTER) {
                            if (ConfigScreenProviders.safelyOpenConfigScreen(this.config.id().withPath(configName), parent, this)) {
                                UISounds.playButtonSound();

                                return true;
                            }
                        }

                        return false;
                    });
                }

                var titleLabel = titleHolder.childById(LabelComponent.class, "title");

                if (this.prevLabels.containsKey(configName)) {
                    titleLabel.copyScrollData(this.prevLabels.get(configName));
                }

                currentLabels.put(configName, titleLabel);
            }

            if (prevScrollProgress != -1) {
                titleScroll.scrollTo(this.prevScrollProgress);
            }
        }

        if (topHolder.surface() == Surface.BLANK) {
            var brightColor = Color.ofArgb(0x4dFFFFFF);
            var darkerColor = Color.ofArgb(0x99000000);

            topHolder.surface(
                    Surface.partialOutline(brightColor.argb(), Surface.OutlineSide.BOTTOM)
                            .and(Surface.partialOutline(darkerColor.argb(), 1, Surface.OutlineSide.BOTTOM))
                            .and((context, component) -> {
                                var titleHolder = component.childById(FlowLayout.class, "title-holder");
                                var mainPanel = component.childById(FlowLayout.class, "main-panel-stack");

                                var lineY = mainPanel.y();

                                // Left Line X values
                                var lineStart1 = mainPanel.x();
                                var lineEnd1 = titleHolder.x() + 1;

                                // Right Line X values
                                var lineStart2 = titleHolder.x() + titleHolder.width() - 2;
                                var lineEnd2 = mainPanel.x() + mainPanel.width();

                                context.drawHorizontalLine(lineStart1, lineEnd1, lineY, brightColor.argb());
                                context.drawHorizontalLine(lineStart1, lineEnd1, lineY + 1, darkerColor.argb());

                                context.drawHorizontalLine(lineStart2, lineEnd2, lineY, brightColor.argb());
                                context.drawHorizontalLine(lineStart2, lineEnd2, lineY + 1, darkerColor.argb());

                                var bqColor = Color.BLACK.withAlpha(0.20f).argb();

                                context.fill(lineStart1, lineY + 2, lineEnd2, mainPanel.y() + mainPanel.height() - 2, bqColor);
                                context.fill(lineEnd1 + 1, titleHolder.y() + 2, lineStart2, titleHolder.y() + titleHolder.height() + 2, bqColor);

                                var selectedLineWidth = Math.round(titleHolder.width() * 0.33f);
                                var selectedLineStart = titleHolder.x() + ((titleHolder.width() - selectedLineWidth) / 2);

                                context.drawHorizontalLine(selectedLineStart, selectedLineStart + selectedLineWidth, lineY, Color.WHITE.argb());
                            })
            );
        }

        rootComponent.childById(ButtonComponent.class, "done-button").onPress(button -> this.close());
        rootComponent.childById(ButtonComponent.class, "reload-button").onPress(button -> {
            this.config.reload();
            this.uiAdapter = null;
            this.clearAndInit();

            // TODO: check if any options changed and warn
        });

        var dumpBtn = rootComponent.childById(ButtonComponent.class, "dump-all-translations");

        if (Owo.DEBUG) {
            dumpBtn.onPress(button -> {
                if (this.translationStorage != null) {
                    ConfigTranslationHelper.dumpData(this.translationStorage, Language.DEFAULT_LANGUAGE, Owo.LOGGER::info);
                }
            });
        } else {
            rootComponent.childById(ParentComponent.class, "button-config-controls")
                .removeChild(dumpBtn);
        }

        var optionPanel = rootComponent.childById(FlowLayout.class, "option-panel");
        var sections = new LinkedHashMap<Component, String>();

        var containers = new HashMap<Key, FlowLayout>();
        containers.put(Key.ROOT, optionPanel);

        rootComponent.childById(TextBoxComponent.class, "search-field").<TextBoxComponent>configure(searchField -> {
            var matchIndicator = rootComponent.childById(LabelComponent.class, "search-match-indicator");
            var optionScroll = rootComponent.childById(ScrollContainer.class, "option-panel-scroll");

            var searchHint = I18n.translate("text.owo.config.search");
            searchField.setSuggestion(searchHint);
            searchField.onChanged().subscribe(s -> {
                searchField.setSuggestion(s.isEmpty() ? searchHint : "");
                if (!s.equals(this.lastSearchFieldText)) {
                    searchField.setEditableColor(TextBoxComponent.DEFAULT_EDITABLE_COLOR);
                    matchIndicator.text(Text.empty());
                }
            });

            searchField.keyPress().subscribe((keyCode, scanCode, modifiers) -> {
                if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER) return false;

                var query = searchField.getText().toLowerCase(Locale.ROOT);
                if (query.isBlank()) return false;

                if (this.currentMatches != null && this.currentMatches.query.equals(query)) {
                    if (this.currentMatches.matches().isEmpty()) {
                        this.currentMatchIndex = -1;
                    } else {
                        this.currentMatchIndex = (this.currentMatchIndex + 1) % this.currentMatches.matches.size();
                    }
                } else {
                    var splitQuery = query.split(" ");

                    this.currentMatchIndex = 0;
                    this.currentMatches = new SearchMatches(query, this.collectSearchAnchors(optionScroll)
                            .stream()
                            .filter(anchor -> Arrays.stream(splitQuery).allMatch(anchor.currentSearchText()::contains))
                            .toList());
                }

                if (this.currentMatches.matches.isEmpty()) {
                    matchIndicator.text(Text.translatable("text.owo.config.search.no_matches"));
                    searchField.setEditableColor(0xEB1D36);
                } else {
                    matchIndicator.text(Text.translatable("text.owo.config.search.matches", this.currentMatchIndex + 1, this.currentMatches.matches.size()));
                    searchField.setEditableColor(0x28FFBF);

                    var selectedMatch = this.currentMatches.matches.get(this.currentMatchIndex);
                    var anchorFrame = selectedMatch.anchorFrame();

                    // we specifically build the path backwards, so we can then iterate
                    // it root -> key, otherwise we could potentially be manipulating
                    // unmounted components which is absolutely not desirable
                    var pathToRoot = new ArrayDeque<Key>();
                    var key = selectedMatch.key();
                    while (!key.isRoot()) {
                        pathToRoot.push(key);
                        key = key.parent();
                    }

                    while (!pathToRoot.isEmpty()) {
                        if (containers.get(pathToRoot.pop()) instanceof CollapsibleContainer collapsible && !collapsible.expanded()) {
                            collapsible.toggleExpansion();
                        }
                    }

                    // in the same vein, the component is mounted after the layout is fully
                    // restored, as we would otherwise be mounting onto a partially-built subtree
                    if (anchorFrame instanceof FlowLayout flow) {
                        flow.child(0, selectedMatch.configure(new SearchHighlighterComponent()));
                    }

                    if (anchorFrame.y() < optionScroll.y() || anchorFrame.y() + anchorFrame.height() > optionScroll.y() + optionScroll.height()) {
                        optionScroll.scrollTo(selectedMatch.anchorFrame());
                    }
                }

                return true;
            });
        });

        ConfigTranslationHelper.pushConfigId(this.config.id());

        this.config.forEachOption(option -> {
            if (option.isAnnotationPresent(ExcludeFromScreen.class)) return;

            var parentKey = option.key().parent();
            if (!parentKey.isRoot() && this.config.fieldForKey(parentKey).isAnnotationPresent(ExcludeFromScreen.class))
                return;

            var factory = this.factoryForOption(option);
            if (factory == null) {
                Owo.LOGGER.warn("Could not create UI component for config option {}", option);
                return;
            }

            var container = containers.computeIfAbsent(
                parentKey,
                key -> {
                    var parentContainerPresent = containers.containsKey(parentKey.parent());

                    // Must go before container due to how it's required to setup translation dumper util
                    if (parentContainerPresent) {
                        if (this.config.fieldForKey(parentKey).isAnnotationPresent(SectionHeader.class)) {
                            this.appendSection(sections, parentKey, this.config.fieldForKey(parentKey), containers.get(parentKey.parent()));
                        }
                    }

                    var expanded = !parentKey.isRoot() && this.config.fieldForKey(parentKey).isAnnotationPresent(Expanded.class);
                    var categoryTranslation = ConfigTranslationHelper.createConfigCategoryTranslation(parentKey);

                    var collapsibleContainer = Containers.collapsible(
                        Sizing.fill(100), Sizing.content(),
                        Text.translatable(categoryTranslation),
                        expanded
                    ).<CollapsibleContainer>configure(nestedContainer -> {
                        var tooltipText = ConfigTranslationHelper.createConfigCategoryTranslation(parentKey, true);
                        if (I18n.hasTranslation(tooltipText)) {
                            nestedContainer.titleLayout().tooltip(Text.translatable(tooltipText));
                        }

                        nestedContainer.titleLayout().child(new SearchAnchorComponent(
                            nestedContainer.titleLayout(),
                            option.key(),
                            () -> I18n.translate(categoryTranslation)
                        ).highlightConfigurator(highlight ->
                            highlight.positioning(Positioning.absolute(-5, -5))
                                .verticalSizing(Sizing.fixed(19))
                        ));
                    });

                    OptionComponentFactory.addEasyCopyLabel(collapsibleContainer.titleLayout(), categoryTranslation);

                    if (parentContainerPresent) {
                        containers.get(parentKey.parent()).child(collapsibleContainer);
                    }

                    return collapsibleContainer;
                }
            );

            if (option.isAnnotationPresent(SectionHeader.class)) {
                this.appendSection(sections, option.key().parent(), option, container);
            }

            var result = factory.make(this.model, option);
            this.options.put(option, result.optionProvider());

            if (option.detached()) {
                result.baseComponent().tooltip(
                        this.client.textRenderer.wrapLines(Text.translatable("text.owo.config.managed_by_server"), Integer.MAX_VALUE)
                                .stream().map(TooltipComponent::of).toList()
                );
            } else {
                var tooltipText = new ArrayList<OrderedText>();
                var tooltipTranslationKey = option.translationTooltipKey();

                if (I18n.hasTranslation(tooltipTranslationKey)) {
                    tooltipText.addAll(this.client.textRenderer.wrapLines(Text.translatable(tooltipTranslationKey), Integer.MAX_VALUE));
                }

                if (option.isAnnotationPresent(RestartRequired.class)) {
                    tooltipText.add(Text.translatable("text.owo.config.applies_after_restart").asOrderedText());
                }

                if (option.isAnnotationPresent(ReloadRequired.class)) {
                    tooltipText.add(Text.translatable("text.owo.config.applies_after_reload").asOrderedText());
                }

                if (!tooltipText.isEmpty()) {
                    result.baseComponent().tooltip(tooltipText.stream().map(TooltipComponent::of).toList());
                }
            }

            container.child(result.baseComponent());

            ConfigTranslationHelper.popOptionKey();
        });

        if (!sections.isEmpty()) {
            boolean sectionsOnRight = true;

            var overlay = this.model.expandTemplate(FlowLayout.class, "section-overlay", Map.of("overlay-side", sectionsOnRight ? "left" : "right"));

            var sectionState = new SectionPanelState(overlay, sectionsOnRight);

            overlay.configure((FlowLayout overlayComponent) -> {
                overlayComponent.mouseDown().subscribe((mouseX, mouseY, button) -> true);
                overlayComponent.mouseUp().subscribe((mouseX, mouseY, button) -> true);

                overlayComponent.componentUpdate().subscribe((delta, mouseX, mouseY) -> {
                    if (!overlayComponent.isInBoundingBox(mouseX, mouseY) && !sectionState.isPanelMoving && sectionState.isPanelOpened) {
                        sectionState.togglePanel();
                    }
                });

                overlayComponent.positioning(Positioning.relative(sectionsOnRight ? 100 : 0, 0))
                        .zIndex(10);
            });

            var panelScroll = rootComponent.childById(ScrollContainer.class, "option-panel-scroll");
            panelScroll.margins(sectionsOnRight ? Insets.right(10) : Insets.left(10));

            var buttonPanel = overlay.childById(FlowLayout.class, "section-buttons");
            sections.forEach((component, text) -> {
                final var label = this.model.expandTemplate(LabelComponent.class, "section-overlay-label", Map.of("section-name", text));

                label.mouseDown().subscribe((mouseX, mouseY, button) -> {
                    panelScroll.scrollTo(component);
                    UISounds.playInteractionSound();
                    return true;
                });

                buttonPanel.child(label);
            });

            var panelContainer = rootComponent.childById(FlowLayout.class, "option-panel-container");

            panelContainer.child(sectionState.closeButton);

            panelContainer.mouseDown().subscribe((mouseX, mouseY, button) -> {
                if ((sectionsOnRight && mouseX > panelContainer.width() - 10) || (!sectionsOnRight && mouseX < 10)) {
                    sectionState.togglePanel();

                    return true;
                }

                return false;
            });
            panelContainer.mouseEnter().subscribe(() -> {
                if (sectionState.isPanelOpened) {
                    sectionState.togglePanel();
                }
            });

            rootComponent.childById(FlowLayout.class, "main-panel-stack").child(overlay);
        }

        this.translationStorage = ConfigTranslationHelper.popConfigId();
    }

    private static class SectionPanelState {

        private boolean isPanelOpened = false;
        private boolean isPanelMoving = false;

        private final Component overlay;

        private final LabelComponent closeButton;

        private final String disabledChar;
        private final String enabledChar;

        SectionPanelState(Component overlay, boolean sectionsOnRight) {
            this.overlay = overlay;

            if (sectionsOnRight) {
                this.disabledChar = "<";
                this.enabledChar = ">";
            } else {
                this.disabledChar = ">";
                this.enabledChar = "<";
            }

            this.closeButton = Components.label(Text.literal(this.disabledChar).formatted(Formatting.BOLD))
                    .configure((LabelComponent label) -> {
                        label.tooltip(Text.translatable("text.owo.config.sections_tooltip"))
                                .positioning(Positioning.relative(sectionsOnRight ? 100 : 0, 50))
                                .cursorStyle(CursorStyle.HAND)
                                .margins(Insets.right(2));
                    });
        }

        public void togglePanel() {
            if (overlay.horizontalSizing().animation() == null) {
                var animation = overlay.horizontalSizing().animate(350, Easing.CUBIC, Sizing.content());

                animation.finished().subscribe((direction, looping) -> isPanelMoving = false);
            }

            isPanelOpened = !isPanelOpened;

            overlay.horizontalSizing().animation().reverse();
            isPanelMoving = true;

            closeButton.text(Text.literal(closeButton.text().getString().equals(enabledChar) ? disabledChar : enabledChar).formatted(Formatting.BOLD));

            UISounds.playInteractionSound();
        }
    }

    protected void appendSection(Map<Component, String> sections, Key parentKey, AnnotatedElement element, FlowLayout container) {
        appendSection(sections, parentKey, element.getAnnotation(SectionHeader.class), container);
    }

    protected void appendSection(Map<Component, String> sections, Key parentKey, SectionHeader annotation, FlowLayout container) {
        var translationKey = ConfigTranslationHelper.createSectionTranslation(parentKey, annotation.value());

        final var header = this.model.expandTemplate(FlowLayout.class, "section-header", Map.of("section-name", translationKey));
        header.childById(LabelComponent.class, "header").<LabelComponent>configure(label -> {
            header.child(new SearchAnchorComponent(header, Key.ROOT, () -> label.text().getString()));
        });

        OptionComponentFactory.addEasyCopyLabel(header.childById(FlowLayout.class, "label-holder"), translationKey);

        var tooltipText = ConfigTranslationHelper.createSectionTranslation(parentKey, annotation.value(), true);

        if (I18n.hasTranslation(tooltipText)) {
            header.tooltip(Text.translatable(tooltipText));
        }

        sections.put(header, translationKey);

        container.child(header);
    }

    protected List<SearchAnchorComponent> collectSearchAnchors(ParentComponent root) {
        var discovered = new ArrayList<SearchAnchorComponent>();
        var candidates = new ArrayDeque<>(root.children());

        while (!candidates.isEmpty()) {
            var candidate = candidates.poll();
            if (candidate instanceof CollapsibleContainer collapsible) {
                candidates.addAll(collapsible.children());
                if (!collapsible.expanded()) candidates.addAll(collapsible.collapsibleChildren());
            } else if (candidate instanceof ParentComponent parentComponent) {
                candidates.addAll(parentComponent.children());
            } else if (candidate instanceof SearchAnchorComponent anchor) {
                discovered.add(anchor);
            }
        }

        return discovered;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_F && ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0)) {
            this.uiAdapter.rootComponent.focusHandler().focus(
                    this.uiAdapter.rootComponent.childById(Component.class, "search-field"),
                    Component.FocusSource.MOUSE_CLICK
            );
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    private TriConsumer<ConfigWrapper<?>, Boolean, Boolean> onConfigChanges = (configWrapper, shouldRestart, shouldReload) -> {};

    void addRemovedHook(TriConsumer<ConfigWrapper<?>, Boolean, Boolean> value) {
        onConfigChanges = value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void close() {
        boolean shouldRestart = false;
        boolean shouldReload = false;

        for (var entry : this.options.entrySet()) {
            var option = entry.getKey();
            var component = entry.getValue();

            if (Objects.equals(option.value(), component.parsedValue())) continue;

            if (option.isAnnotationPresent(RestartRequired.class)) {
                shouldRestart = true;
            } else if (option.isAnnotationPresent(ReloadRequired.class)) {
                shouldReload = true;
            }
        }

        this.client.setScreen(
            tryClosingInfoScreen(shouldRestart, shouldReload)
        );
    }

    private Screen tryClosingInfoScreen(boolean shouldRestart, boolean shouldReload) {
        if (!shouldRestart && !shouldReload) return this.parent;

        Map<String, Runnable> buttonAdditions;
        String titleKey;
        String messageKey;

        if (shouldRestart) {
            if (!this.config.isServerConfig()) {
                buttonAdditions = Map.of(
                    "text.owo.config.button.exit_minecraft", () -> MinecraftClient.getInstance().scheduleStop(),
                    "text.owo.config.button.restart_later", () -> MinecraftClient.getInstance().currentScreen.close()
                );
            } else {
                buttonAdditions = Map.of(
                    "text.owo.config.button.restart_later", () -> MinecraftClient.getInstance().currentScreen.close()
                );
            }

            titleKey = "text.owo.config.restart_prompt.title";
            messageKey = "text.owo.config.restart_prompt.message";
        } else {
            buttonAdditions = Map.of(
                "text.owo.config.button.reload_server", () -> {
                    MinecraftClient.getInstance().player.networkHandler.sendCommand("reload");
                    MinecraftClient.getInstance().currentScreen.close();
                },
                "text.owo.config.button.reload_later", () -> MinecraftClient.getInstance().currentScreen.close()
            );

            titleKey = "text.owo.config.reload_prompt.title";
            messageKey = "text.owo.config.reload_prompt.message";
        }

        return new SimpleButtonScreen(titleKey, messageKey, buttonAdditions){
            @Override
            public void close() {
                this.client.setScreen(ConfigScreen.this.parent);
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removed() {
        boolean hasOptionsChanged = false;
        boolean shouldRestart = false;
        boolean shouldReload = false;

        for (var entry : this.options.entrySet()) {
            var option = entry.getKey();
            var component = entry.getValue();

            if (!component.isValid()) continue;

            if (Objects.equals(option.value(), component.parsedValue())) continue;
            if (option.isAnnotationPresent(RestartRequired.class)) {
                shouldRestart = true;
            }
            if (option.isAnnotationPresent(ReloadRequired.class)) {
                shouldReload = true;
            }

            hasOptionsChanged = true;

            option.set(component.parsedValue());
        }

        if (hasOptionsChanged) {
            onConfigChanges.accept(this.config, shouldRestart, shouldReload);
        }

        super.removed();
    }

    @SuppressWarnings("rawtypes")
    protected @Nullable OptionComponentFactory factoryForOption(FieldOption<?> option) {
        for (var predicate : this.extraFactories.keySet()) {
            if (!predicate.test(option)) continue;
            return this.extraFactories.get(predicate);
        }

        for (var predicate : DEFAULT_FACTORIES.keySet()) {
            if (!predicate.test(option)) continue;
            return DEFAULT_FACTORIES.get(predicate);
        }

        return null;
    }

    static {
        DEFAULT_FACTORIES.put(option -> NumberReflection.isNumberType(option.clazz()), OptionComponentFactory.NUMBER);
        DEFAULT_FACTORIES.put(option -> option.clazz() == String.class, OptionComponentFactory.STRING);
        DEFAULT_FACTORIES.put(option -> option.clazz() == Boolean.class || option.clazz() == boolean.class, OptionComponentFactory.BOOLEAN);
        DEFAULT_FACTORIES.put(option -> option.clazz() == Identifier.class, OptionComponentFactory.IDENTIFIER);
        DEFAULT_FACTORIES.put(option -> option.clazz() == Color.class, OptionComponentFactory.COLOR);
        DEFAULT_FACTORIES.put(option -> option.clazz() == List.class && ConfigReflectionUtils.getCollectionType(option.getGenericType()) != null, OptionComponentFactory.LIST);
        DEFAULT_FACTORIES.put(option -> option.clazz() == Set.class && ConfigReflectionUtils.getCollectionType(option.getGenericType()) != null, OptionComponentFactory.SET);
        DEFAULT_FACTORIES.put(option -> option.clazz() == Map.class && ConfigReflectionUtils.getMapType(option.getGenericType()) == ConfigReflectionUtils.CollectionType.SIMPLE, OptionComponentFactory.SIMPLE_MAP);
        DEFAULT_FACTORIES.put(option -> option.clazz().isEnum(), OptionComponentFactory.ENUM);
        DEFAULT_FACTORIES.put(option -> {
            if (option.clazz() != Map.class) {
                try {
                    ReflectionUtils.getNoArgsConstructor(option.clazz());

                    return true;
                } catch (IllegalStateException ignored) {
                }
            }

            return false;
        } , OptionComponentFactory.STRUCT);

        UIParsing.registerFactory("config-slider", element -> new ConfigSlider());
        UIParsing.registerFactory("config-toggle-button", element -> new ConfigToggleButton());
        UIParsing.registerFactory("config-enum-button", element -> new ConfigEnumButton());
        UIParsing.registerFactory("config-text-box", element -> new ConfigTextBox());
        UIParsing.registerFactory("selectable-scroll", SelectableScrollContainer::parse);
    }

    protected record SearchMatches(String query, List<SearchAnchorComponent> matches) {}

    public static class SearchHighlighterComponent extends BaseComponent {

        private final Color startColor = Color.ofArgb(0x008d9be0);
        private final Color endColor = Color.ofArgb(0x4c8d9be0);

        private float age = 0;

        public SearchHighlighterComponent() {
            this.positioning(Positioning.absolute(0, 0));
            this.sizing(Sizing.fill(100), Sizing.fill(100));
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            final var mainColor = startColor.interpolate(endColor, (float) Math.sin(age / 25 * Math.PI)).argb();

            int segmentWidth = (int) (this.width * .3f);
            int baseX = (int) ((this.x - segmentWidth) + (Easing.CUBIC.apply(this.age / 25)) * (this.width + segmentWidth * 2));

            context.drawGradientRect(
                    baseX - segmentWidth, this.y,
                    segmentWidth, this.height,
                    0, mainColor,
                    mainColor, 0
            );
            context.drawGradientRect(
                    baseX, this.y,
                    segmentWidth, this.height,
                    mainColor, 0,
                    0, mainColor
            );
        }

        @Override
        public void update(float delta, int mouseX, int mouseY) {
            super.update(delta, mouseX, mouseY);
            if ((this.age += delta) > 25) {
                this.parent.queue(() -> this.parent.removeChild(this));
            }
        }
    }
}
