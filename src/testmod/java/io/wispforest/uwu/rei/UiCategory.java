package io.wispforest.uwu.rei;

// TODO: now there's even REI stuff to fix in uwu
public class UiCategory /*implements DisplayCategory<Display>*/ {
//
//    public static CategoryIdentifier<UiDisplay> ID = CategoryIdentifier.of(Owo.id("ui"));
//
//    @Override
//    public List<Widget> setupDisplay(Display display, Rectangle bounds) {
//        var adapter = new ReiUIAdapter<>(bounds, UIContainers::verticalFlow);
//        var root = adapter.rootComponent();
//
//        root.horizontalAlignment(HorizontalAlignment.CENTER)
//                .surface(Surface.DARK_PANEL)
//                .padding(Insets.of(8));
//
//        var inner = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
//        inner.horizontalAlignment(HorizontalAlignment.CENTER).surface(Surface.flat(0xFF00FFAF));
//
//        inner.child(UIComponents.label(Component.nullToEmpty("A demonstration\ninside REI"))
//                .color(Color.BLACK)
//                .positioning(Positioning.absolute(3, 3))
//        );
//
//        var animation = inner.horizontalSizing().animate(250, Easing.QUADRATIC, Sizing.fill(65));
//        inner.child(UIComponents.button(Component.nullToEmpty("shrink"), (ButtonComponent button) -> animation.forwards())
//                .margins(Insets.vertical(25))
//                .horizontalSizing(Sizing.fixed(60)));
//        inner.child(UIComponents.button(Component.nullToEmpty("grow"), (ButtonComponent button) -> animation.backwards())
//                .margins(Insets.vertical(25))
//                .horizontalSizing(Sizing.fixed(60)));
//
//        inner.child(adapter.wrap(Widgets.createSlot(new Point(0, 0)).entry(EntryStacks.of(Items.ECHO_SHARD))));
//
//        root.child(UIContainers.verticalScroll(Sizing.content(), Sizing.fill(100), inner));
//
//        adapter.prepare();
//        return List.of(adapter);
//    }
//
//    @Override
//    public Renderer getIcon() {
//        return EntryStacks.of(Items.ECHO_SHARD);
//    }
//
//    @Override
//    public Component getTitle() {
//        return Component.nullToEmpty("yes its gui very epic");
//    }
//
//    @Override
//    public CategoryIdentifier<? extends Display> getCategoryIdentifier() {
//        return ID;
//    }
//
//    public static class UiDisplay implements Display {
//
//        @Override
//        public List<EntryIngredient> getInputEntries() {
//            return List.of(EntryIngredients.of(UwuItems.SCREEN_SHARD));
//        }
//
//        @Override
//        public List<EntryIngredient> getOutputEntries() {
//            return Collections.emptyList();
//        }
//
//        @Override
//        public CategoryIdentifier<?> getCategoryIdentifier() {
//            return ID;
//        }
//
//        @Override
//        public Optional<Identifier> getDisplayLocation() {
//            return Optional.empty();
//        }
//
//        @Override
//        public @Nullable DisplaySerializer<? extends Display> getSerializer() {
//            return null;
//        }
//    }
}
