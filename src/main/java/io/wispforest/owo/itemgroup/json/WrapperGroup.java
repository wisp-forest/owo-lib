package io.wispforest.owo.itemgroup.json;

import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;

/**
 * Used to replace a vanilla or modded item group to add the JSON-defined
 * tabs while keeping the same name, id and icon
 */
@ApiStatus.Internal
public class WrapperGroup extends OwoItemGroup {

    private final ItemGroup parent;
    private boolean extension = false;

    public WrapperGroup(ItemGroup parent, List<ItemGroupTab> tabs, List<ItemGroupButton> buttons) {
        super(parent.getIndex(), parent.getName());

        this.tabs.clear();
        this.tabs.addAll(tabs);

        this.buttons.clear();
        this.buttons.addAll(buttons);

        this.parent = parent;
    }

    public void addTabs(Collection<ItemGroupTab> tabs) {
        this.tabs.addAll(tabs);
    }

    public void addButtons(Collection<ItemGroupButton> buttons) {
        this.buttons.addAll(buttons);
    }

    public void markExtension() {
        if (this.extension) return;
        this.extension = true;

        if (this.tabs.get(0) == PLACEHOLDER_TAB) {
            this.tabs.remove(0);
        }

        this.tabs.add(0, new ItemGroupTab(
                Icon.of(this.parent.getIcon()),
                this.parent.getDisplayName(),
                this.parent::appendStacks,
                ItemGroupTab.DEFAULT_TEXTURE,
                true
        ));
    }

    @Override
    protected void setup() {}

    @Override
    public ItemStack createIcon() {
        return this.parent.createIcon();
    }
}
