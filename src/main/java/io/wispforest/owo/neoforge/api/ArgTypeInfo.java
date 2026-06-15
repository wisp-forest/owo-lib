package io.wispforest.owo.neoforge.api;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;

record ArgTypeInfo<A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>>(
    Class<A> clazz, I info) {
    I addInfoToClassMap() {
        return ArgumentTypeInfos.registerByClass(clazz, info);
    }
}
