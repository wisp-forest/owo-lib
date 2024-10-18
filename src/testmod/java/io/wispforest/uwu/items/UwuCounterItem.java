package io.wispforest.uwu.items;

import io.wispforest.endec.Endec;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

public class UwuCounterItem extends Item {
    private static final ComponentType<Integer> COUNT = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        Identifier.of("uwu", "count"),
        ComponentType.<Integer>builder()
            .endec(Endec.INT)
            .build()
    );

    public UwuCounterItem(Item.Settings settings) {
        super(settings.rarity(Rarity.UNCOMMON));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);

        if (user.isSneaking()) {
            stack.apply(COUNT, 0, old -> old - 1);
        } else {
            stack.apply(COUNT, 0, old -> old + 1);
        }

        return ActionResult.SUCCESS.withNewHandStack(stack);
    }

    @Override
    public void deriveStackComponents(ComponentMap source, ComponentChanges.Builder target) {
        target.add(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.builder()
            .add(EntityAttributes.ATTACK_DAMAGE,
                new EntityAttributeModifier(Identifier.of("uwu", "counter_attribute"), source.getOrDefault(COUNT, 0), EntityAttributeModifier.Operation.ADD_VALUE),
                AttributeModifierSlot.MAINHAND)
            .build());
    }
}
