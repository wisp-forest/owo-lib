package io.wispforest.uwu.items;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.OwoItemSettings;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.RegistriesAttribute;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.uwu.Uwu;
import io.wispforest.uwu.text.BasedTextContent;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class UwuTestStickItem extends Item {

    private static final ComponentType<Text> TEXT_COMPONENT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of("uwu", "text"),
            ComponentType.<Text>builder()
                    .endec(MinecraftEndecs.TEXT)
                    .build()
    );

    private static final Codec<String> THIS_CODEC_NEEDS_REGISTRIES = new Codec<>() {
        @Override
        public <T> DataResult<Pair<String, T>> decode(DynamicOps<T> ops, T input) {
            if (!(ops instanceof RegistryOps<T>)) return DataResult.error(() -> "need the registries bro");
            return DataResult.success(new Pair<>(ops.getStringValue(input).getOrThrow(), input));
        }
        @Override
        public <T> DataResult<T> encode(String input, DynamicOps<T> ops, T prefix) {
            if (!(ops instanceof RegistryOps<T>)) return DataResult.error(() -> "need the registries bro");
            return DataResult.success(ops.createString(input));
        }
    };

    private static final Endec<String> YEP_SAME_HERE = CodecUtils.toEndec(CodecUtils.toCodec(CodecUtils.toEndec(THIS_CODEC_NEEDS_REGISTRIES)));
    private static final KeyedEndec<String> KYED = YEP_SAME_HERE.keyed("kyed", (String) null);

    public UwuTestStickItem() {
        super(new OwoItemSettings()
                .group(Uwu.SIX_TAB_GROUP).tab(3).maxCount(1)
                .trackUsageStat()
                .stackGenerator(OwoItemGroup.DEFAULT_STACK_GENERATOR.andThen((item, stacks) -> {
                    final var stack = new ItemStack(item);
                    stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("the stick of the test").styled(style -> style.withItalic(false)));
                    stacks.add(stack);
                })));

        Uwu.CHANNEL.registerServerbound(ThatPacket.class, StructEndecBuilder.of(YEP_SAME_HERE.fieldOf("mhmm", ThatPacket::mhmm), ThatPacket::new), (message, access) -> {
            System.out.println("that's a packet received alright: " + message.mhmm);
        });
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        test(UwuItems.TEST_STICK);
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
            user.sendMessage(Text.translatable("uwu.a", "bruh"));

            return TypedActionResult.success(user.getStackInHand(hand));
        }
    }

    public void test(RegistryEntry<Item> entry) {
        System.out.println(entry.value());
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getPlayer().isSneaking()) {
            if (context.getWorld().isClient) Uwu.CHANNEL.clientHandle().send(new ThatPacket("stringnite"));

            try {
                var stack = context.getStack();
                var data = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt()
                        .get(SerializationContext.attributes(RegistriesAttribute.of(context.getWorld().getRegistryManager())), KYED);

                context.getPlayer().sendMessage(Text.literal("current: " + data));

                stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, nbt -> {
                    return nbt.apply(nbtCompound -> nbtCompound.put(
                            SerializationContext.attributes(RegistriesAttribute.of(context.getWorld().getRegistryManager())),
                            KYED,
                            String.valueOf(context.getWorld().random.nextInt(10000))
                    ));
                });
                context.getPlayer().sendMessage(Text.literal("modified"));
            } catch (Exception bruh) {
                context.getPlayer().sendMessage(Text.literal("bruh: " + bruh.getMessage()));
            }

            return ActionResult.SUCCESS;
        }

        if (context.getWorld().isClient) return ActionResult.SUCCESS;

        final var breakStack = new ItemStack(Items.NETHERITE_PICKAXE);

        final var fortune = context.getWorld().getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.FORTUNE).orElseThrow();
        breakStack.addEnchantment(fortune, 3);
        WorldOps.breakBlockWithItem(context.getWorld(), context.getBlockPos(), breakStack);

        final var stickStack = context.getStack();

        if (!stickStack.contains(TEXT_COMPONENT)) {
            stickStack.set(TEXT_COMPONENT, Text.of(String.valueOf(context.getWorld().random.nextInt(1000000))));
        }

        stickStack.set(TEXT_COMPONENT, MutableText.of(new BasedTextContent("basednite, ")).append(stickStack.get(TEXT_COMPONENT)));

        context.getPlayer().sendMessage(stickStack.get(TEXT_COMPONENT), false);

        Uwu.BREAK_BLOCK_PARTICLES.spawn(context.getWorld(), Vec3d.of(context.getBlockPos()), null);

        return ActionResult.SUCCESS;
    }

    private record ThatPacket(String mhmm) {}
}
