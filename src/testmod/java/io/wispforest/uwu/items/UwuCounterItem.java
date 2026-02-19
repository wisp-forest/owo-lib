package io.wispforest.uwu.items;

import io.wispforest.endec.Endec;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class UwuCounterItem extends Item {
    private static final DataComponentType<Integer> COUNT = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        Identifier.fromNamespaceAndPath("uwu", "count"),
        DataComponentType.<Integer>builder()
            .endec(Endec.INT)
            .build()
    );

    public UwuCounterItem(Item.Properties settings) {
        super(settings.rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        var stack = user.getItemInHand(hand);

        if (user.isShiftKeyDown()) {
            stack.update(COUNT, 0, old -> old - 1);
        } else {
            stack.update(COUNT, 0, old -> old + 1);
        }

        return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
    }

    @Override
    public void deriveStackComponents(DataComponentMap source, DataComponentPatch.Builder target) {
        target.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(Identifier.fromNamespaceAndPath("uwu", "counter_attribute"), source.getOrDefault(COUNT, 0), AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .build());
    }
}
