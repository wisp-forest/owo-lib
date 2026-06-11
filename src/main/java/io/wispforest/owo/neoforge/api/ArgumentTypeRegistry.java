package io.wispforest.owo.neoforge.api;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.EventBus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public class ArgumentTypeRegistry {

    private static final Map<Identifier, ArgTypeInfo> argTypeInfo = new HashMap<>();

    public static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void registerArgumentType(Identifier id, Class<? extends A> clazz, ArgumentTypeInfo<A, T> serializer) {
        argTypeInfo.put(id, new ArgTypeInfo(clazz, serializer));
    }

    @ApiStatus.Internal
    public static void init(IEventBus bus) {
        bus.addListener(ArgumentTypeRegistry::register);
    }

    private record ArgTypeInfo<A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>>(
        Class<A> clazz, I info) {
        I addInfoToClassMap() {
            return ArgumentTypeInfos.registerByClass(clazz, info);
        }
    }
    private static void register(RegisterEvent event) {
        event.register(
            BuiltInRegistries.COMMAND_ARGUMENT_TYPE.key(),
            helper -> argTypeInfo.forEach((id, info) -> helper.register(id, info.addInfoToClassMap()))
        );
    }
}
