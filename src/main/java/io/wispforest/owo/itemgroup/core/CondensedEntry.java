package io.wispforest.owo.itemgroup.core;

import io.wispforest.owo.itemgroup.base.ItemStacksSupplier;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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
        if (type.isAdvanced()) {
            tooltipAddCallback.accept(Text.empty());

            if (childrenEntries instanceof ItemStacksSupplier.RegistryTag registryTag) {
                tooltipAddCallback.accept(Text.translatable("text.owo.condensed_entries.tag_key", Text.translatable(registryTag.tagKey().getTranslationKey())));
            }

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
            this.totalTime += delta * 50;

            // TODO: ADJUSTABLE TIME?
            if (this.iconStack == null || (this.totalTime > 1500)) {
                this.totalTime = 0;

                ItemStack chosenIconStack = null;

                while (chosenIconStack == null) {
                    // TODO: ADD ABILITY TO TOGGLE DISPLAY ROTATION ON AND OFF
                    int index = new Random().nextInt(0, this.children.size());

                    var entry = this.children.get(index);

                    if (this.iconStack != entry) {
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

            if (this.showChildren) {
                displayStacks.removeAll(children);
            } else {
                displayStacks.addAll(startingIndex + 1, children);
            }

            toggleChildren();
        }

        public void toggleChildren() {
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
    }
}
