package io.wispforest.uwu.client;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.resources.Identifier;

public class ParseFailScreen extends BaseUIModelScreen<FlowLayout> {

    public ParseFailScreen() {
        super(FlowLayout.class, DataSource.asset(Identifier.of("uwu", "parse_fail")));
    }

    @Override
    protected void build(FlowLayout rootComponent) {

    }
}