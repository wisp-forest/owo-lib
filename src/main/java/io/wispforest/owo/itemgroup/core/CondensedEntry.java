package io.wispforest.owo.itemgroup.core;

import io.wispforest.owo.Owo;
import io.wispforest.owo.itemgroup.base.ItemStacksSupplier;
import io.wispforest.owo.serialization.IdentifiedData;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

///
/// An entry within an [ItemGroup] that is condensable allowing the user to
/// expand or shrink the entry to show or hide its entries saving space by
/// grouping similar entries
///
public record CondensedEntry(Identifier id, ItemStacksSupplier childrenEntries, boolean useItemMatching) {

    public String getTranslationKey() {
        return (this.childrenEntries() instanceof ItemStacksSupplier.RegistryTag registryTag)
            ? registryTag.tagKey().getTranslationKey()
            : "condensed_entry." + this.id().toTranslationKey();
    }

    public void addExtraInfo(Consumer<Text> tooltipAddCallback, TooltipType type) {
        if (!type.isAdvanced()) return;

        if (Owo.CONFIG.info.showTagData() && childrenEntries instanceof ItemStacksSupplier.RegistryTag registryTag) {
            var tagKey = registryTag.tagKey();

            tooltipAddCallback.accept(Text.empty());

            var registry = tagKey.registryRef().getValue();

            tooltipAddCallback.accept(Text.translatable("text.owo.condensed_entries.tag.registry", Objects.equals(registry.getNamespace(), "minecraft") ? registry.getPath() : registry.toString()));
            tooltipAddCallback.accept(Text.translatable("text.owo.condensed_entries.tag.key", tagKey.id().toString()));
        }

        if (Owo.CONFIG.info.showEntryData()) {
            tooltipAddCallback.accept(Text.empty());

            tooltipAddCallback.accept(Text.translatable("text.owo.condensed_entries.type", Text.translatable(childrenEntries.translationKey())));
            tooltipAddCallback.accept(Text.translatable("text.owo.condensed_entries.entry_id", id));
        }
    }

    @ApiStatus.Internal
    public State createState(ItemStack parent, List<ItemStack> children) {
        return new State(parent, children);
    }

    /*
     * Ordering of children entries will be first based on any existing entry orderers and then based on the order in which the entries are given
     */
    public class State {

        private final ItemStack parent;
        private final List<ItemStack> children;

        public State(ItemStack parent, List<ItemStack> children) {
            this.parent = parent;
            this.children = children;
        }

        private boolean showChildren = false;

        @Nullable
        private ItemStack iconStack = null;

        private double totalTime;

        public CondensedEntry entry() {
            return CondensedEntry.this;
        }

        public ItemStack getDisplayStack(double delta) {
            if (Owo.CONFIG.showEntryShuffle()) this.totalTime += delta * 50;

            // TODO: ADJUSTABLE TIME?
            if (this.iconStack == null || (this.totalTime > Owo.CONFIG.entryShuffleTime())) {
                this.totalTime = 0;

                ItemStack chosenIconStack = null;

                while (chosenIconStack == null) {
                    // TODO: ADD ABILITY TO TOGGLE DISPLAY ROTATION ON AND OFF
                    int index = new Random().nextInt(0, this.children.size());

                    var entry = this.children.get(index);

                    if (this.iconStack != entry || this.children.size() == 1) {
                        chosenIconStack = entry;
                    }
                }

                this.iconStack = chosenIconStack;
            }

            return this.iconStack;
        }

        public boolean showChildren() {
            return this.showChildren;
        }

        public void toggleChildren(List<ItemStack> displayStacks) {
            var startingIndex = displayStacks.indexOf(parent);

            if (startingIndex <= 0) {
                System.out.println("Invalid Index detected `" + startingIndex + "` for entry State: " + this.entry());
            }

            if (this.showChildren) {
                children.forEach(displayStacks::remove);
                //displayStacks.removeAll(children);
            } else {
                displayStacks.addAll(startingIndex + 1, children);
            }

            toggleChildren();
        }

        private void toggleChildren() {
            this.showChildren = !this.showChildren;
        }

        public Text title() {
            return Text.translatable(entry().getTranslationKey()).formatted(Formatting.WHITE);
        }

        @Nullable
        public Text description() {
            var key = entry().getTranslationKey() + ".tooltip";

            return Language.getInstance().hasTranslation(key)
                ? Text.translatable(key).formatted(Formatting.GRAY)
                : null;
        }

        public void appendTooltip(TooltipType type, Consumer<Text> addCallback) {
            addCallback.accept(this.title());

            var description = this.description();

            if (description != null) addCallback.accept(description);

            this.entry().addExtraInfo(addCallback, type);
        }
    }
}
