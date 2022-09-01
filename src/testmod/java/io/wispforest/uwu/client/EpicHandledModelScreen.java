package io.wispforest.uwu.client;

import io.wispforest.owo.ui.base.BaseUIModelHandledScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.uwu.EpicScreenHandler;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class EpicHandledModelScreen extends BaseUIModelHandledScreen<FlowLayout, EpicScreenHandler> {

    public EpicHandledModelScreen(EpicScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title, FlowLayout.class, BaseUIModelScreen.DataSource.file("epic_handled_screen.xml"));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        var indexField = rootComponent.childById(TextFieldWidget.class, "index-field");
        indexField.setTextPredicate(s -> s.matches("\\d*"));

        rootComponent.childById(ButtonWidget.class, "enable-button").onPress(button -> this.enableSlot(Integer.parseInt(indexField.getText())));
        rootComponent.childById(ButtonWidget.class, "disable-button").onPress(button -> this.disableSlot(Integer.parseInt(indexField.getText())));
    }
}
