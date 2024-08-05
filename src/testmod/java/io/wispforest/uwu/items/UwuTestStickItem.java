package io.wispforest.uwu.items;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.ops.WorldOps;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationContext;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.RegistriesAttribute;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import io.wispforest.uwu.Uwu;
import io.wispforest.uwu.text.BasedTextContent;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.MutableText;
import net.minecraft.network.chat.Text;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class UwuTestStickItem extends Item {

    private static final DataComponentType<Text> TEXT_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.of("uwu", "text"),
            DataComponentType.<Text>builder()
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
        super(new Item.Properties()
                .group(Uwu.SIX_TAB_GROUP).tab(3).stacksTo(1)
                .trackUsageStat()
                .stackGenerator(OwoItemGroup.DEFAULT_STACK_GENERATOR.andThen((item, stacks) -> {
                    final var stack = new ItemStack(item);
                    stack.set(DataComponents.CUSTOM_NAME, Text.literal("the stick of the test").withStyle(style -> style.withItalic(false)));
                    stacks.accept(stack);
                })));

        Uwu.CHANNEL.registerServerbound(ThatPacket.class, StructEndecBuilder.of(YEP_SAME_HERE.fieldOf("mhmm", ThatPacket::mhmm), ThatPacket::new), (message, access) -> {
            System.out.println("that's a packet received alright: " + message.mhmm);
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (user.isShiftKeyDown()) {
            if (world.isClientSide) return InteractionResultHolder.success(user.getItemInHand(hand));

            Uwu.CHANNEL.serverHandle(user).send(new Uwu.OtherTestMessage(user.getBlockPos(), "based"));

            var server = user.getServer();
            var teleportTo = world.dimension() == Level.END ? server.getLevel(Level.OVERWORLD) : server.getLevel(Level.END);

            WorldOps.teleportToWorld((ServerPlayer) user, teleportTo, new Vec3(0, 128, 0));

            return InteractionResultHolder.success(user.getItemInHand(hand));
        } else {
            if (!world.isClientSide) return InteractionResultHolder.success(user.getItemInHand(hand));

            Uwu.CHANNEL.clientHandle().send(Uwu.MESSAGE);

            Uwu.CUBE.spawn(world, user.getEyePosition().add(user.getViewVector(0).scale(3)).subtract(.5, .5, .5), null);
            user.sendSystemMessage(Text.translatable("uwu.a", "bruh"));

            return InteractionResultHolder.success(user.getItemInHand(hand));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getPlayer().isShiftKeyDown()) {
            if (context.getLevel().isClientSide) Uwu.CHANNEL.clientHandle().send(new ThatPacket("stringnite"));

            try {
                var stack = context.getItemInHand();
                var data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe()
                        .get(SerializationContext.attributes(RegistriesAttribute.of(context.getLevel().registryAccess())), KYED);

                context.getPlayer().sendSystemMessage(Text.literal("current: " + data));

                stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, nbt -> {
                    return nbt.update(nbtCompound -> nbtCompound.put(
                            SerializationContext.attributes(RegistriesAttribute.of(context.getLevel().registryAccess())),
                            KYED,
                            String.valueOf(context.getLevel().random.nextInt(10000))
                    ));
                });
                context.getPlayer().sendSystemMessage(Text.literal("modified"));
            } catch (Exception bruh) {
                context.getPlayer().sendSystemMessage(Text.literal("bruh: " + bruh.getMessage()));
            }

            return InteractionResult.SUCCESS;
        }

        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;

        final var breakStack = new ItemStack(Items.NETHERITE_PICKAXE);

        final var fortune = context.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(Enchantments.FORTUNE).orElseThrow();
        breakStack.enchant(fortune, 3);
        WorldOps.breakBlockWithItem(context.getLevel(), context.getClickedPos(), breakStack);

        final var stickStack = context.getItemInHand();

        if (!stickStack.has(TEXT_COMPONENT)) {
            stickStack.set(TEXT_COMPONENT, Text.nullToEmpty(String.valueOf(context.getLevel().random.nextInt(1000000))));
        }

        stickStack.set(TEXT_COMPONENT, MutableText.of(new BasedTextContent("basednite, ")).append(stickStack.get(TEXT_COMPONENT)));

        context.getPlayer().displayClientMessage(stickStack.get(TEXT_COMPONENT), false);

        Uwu.BREAK_BLOCK_PARTICLES.spawn(context.getLevel(), Vec3.atLowerCornerOf(context.getClickedPos()), null);

        return InteractionResult.SUCCESS;
    }

    private record ThatPacket(String mhmm) {}
}
