package io.wispforest.uwu.client;

import io.wispforest.owo.ui.base.BaseUIModelContainerScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.uwu.EpicMenu;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;

public class EpicContainerModelScreen extends BaseUIModelContainerScreen<FlowLayout, EpicMenu> {

    public EpicContainerModelScreen(EpicMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title, FlowLayout.class, BaseUIModelScreen.DataSource.file("epic_handled_screen.xml"));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var indexField = rootComponent.childById(EditBox.class, "index-field");
        indexField.setFilter(s -> s.matches("\\d*"));

        rootComponent.childById(ButtonComponent.class, "enable-button").onPress(button -> this.enableSlot(Integer.parseInt(indexField.getValue())));
        rootComponent.childById(ButtonComponent.class, "disable-button").onPress(button -> this.disableSlot(Integer.parseInt(indexField.getValue())));
    }
}
