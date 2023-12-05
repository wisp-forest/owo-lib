package io.wispforest.uwu.items;

import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.owo.serialization.BuiltInEndecs;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.impl.KeyedEndec;
import io.wispforest.uwu.Uwu;
import io.wispforest.uwu.text.BasedTextContent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class UwuTestStickItem extends Item {

    private static final KeyedEndec<Text> TEXT_KEY = BuiltInEndecs.TEXT.keyed("Text", Text.empty());

    public UwuTestStickItem() {
        super(new OwoItemSettings().group(Uwu.SIX_TAB_GROUP).tab(3).maxCount(1)
                .trackUsageStat()
                .stackGenerator(OwoItemGroup.DEFAULT_STACK_GENERATOR.andThen((item, stacks) -> {
                    final var stack = new ItemStack(item);
                    stack.setCustomName(Text.literal("the stick of the test").styled(style -> style.withItalic(false)));
                    stacks.add(stack);
                })));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking()) {
            if (world.isClient) return TypedActionResult.success(user.getStackInHand(hand));

            Uwu.CHANNEL.serverHandle(user).send(new Uwu.OtherTestMessage(user.getBlockPos(), "based"));

            var server = user.getServer();
            var teleportTo = world.getRegistryKey() == World.END ? server.getWorld(World.OVERWORLD) : server.getWorld(World.END);

            WorldOps.teleportToWorld((ServerPlayerEntity) user, teleportTo, new Vec3d(0, 128, 0));

            return TypedActionResult.success(user.getStackInHand(hand));
        } else {
            if (!world.isClient) return TypedActionResult.success(user.getStackInHand(hand));

            Uwu.CHANNEL.clientHandle().send(Uwu.MESSAGE);

            Uwu.CUBE.spawn(world, user.getEyePos().add(user.getRotationVec(0).multiply(3)).subtract(.5, .5, .5), null);

            return TypedActionResult.success(user.getStackInHand(hand));
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getPlayer().isSneaking()) return ActionResult.PASS;
        if (context.getWorld().isClient) return ActionResult.SUCCESS;

        final var breakStack = new ItemStack(Items.NETHERITE_PICKAXE);
        breakStack.addEnchantment(Enchantments.FORTUNE, 3);
        WorldOps.breakBlockWithItem(context.getWorld(), context.getBlockPos(), breakStack);

        final var stickStack = context.getStack();

        if (!stickStack.has(TEXT_KEY)) {
            stickStack.put(TEXT_KEY, Text.of(String.valueOf(context.getWorld().random.nextInt(1000000))));
        }

        stickStack.mutate(TEXT_KEY, text -> MutableText.of(new BasedTextContent("basednite, ")).append(text));

        context.getPlayer().sendMessage(stickStack.getNbt().get(TEXT_KEY), false);

        Uwu.BREAK_BLOCK_PARTICLES.spawn(context.getWorld(), Vec3d.of(context.getBlockPos()), null);

        return ActionResult.SUCCESS;
    }
}
