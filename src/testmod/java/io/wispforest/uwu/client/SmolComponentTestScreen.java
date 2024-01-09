package io.wispforest.uwu.client;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.SlimSliderComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SmolComponentTestScreen extends BaseUIModelScreen<FlowLayout> {

    protected SmolComponentTestScreen() {
        super(FlowLayout.class, new Identifier("uwu", "smol_components"));
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    protected void build(FlowLayout rootComponent) {
        rootComponent.childById(SlimSliderComponent.class, "precise-slider").tooltipSupplier(SlimSliderComponent.valueTooltipSupplier(2));

        rootComponent.childById(SlimSliderComponent.class, "tiny-steppy-man").tooltipSupplier(SlimSliderComponent.VALUE_TOOLTIP_SUPPLIER).onChanged().subscribe(value -> {
            this.client.player.sendMessage(Text.literal("tiny steppy man: " + value));
        });

        rootComponent.childById(SlimSliderComponent.class, "big-steppy-man").tooltipSupplier(value -> Text.literal("big steppy man: " + value)).onChanged().subscribe(value -> {
            this.client.player.sendMessage(Text.literal("big steppy man: " + value));
        });

        rootComponent.childById(SlimSliderComponent.class, "inset-slider").<SlimSliderComponent>configure(slider -> {
            slider.tooltipSupplier(value -> Text.literal("Insets: " + value.intValue()));
            slider.onChanged().subscribe(value -> {
                rootComponent.childById(FlowLayout.class, "inset-container").padding(Insets.of((int) value));
            });
        });

        this.component(SlimSliderComponent.class, "expando-slider").<SlimSliderComponent>configure(slider -> {
            slider.tooltipSupplier(SlimSliderComponent.VALUE_TOOLTIP_SUPPLIER);
            slider.onChanged().subscribe(value -> {
                this.component(BoxComponent.class, "expando-box").horizontalSizing(Sizing.fixed((int) value));
            });
        });
    }
}
