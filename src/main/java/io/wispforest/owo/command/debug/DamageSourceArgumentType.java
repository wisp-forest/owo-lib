package io.wispforest.owo.command.debug;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DamageSourceArgumentType implements ArgumentType<DamageSource> {

    private static final SimpleCommandExceptionType UNKNOWN_SOURCE = new SimpleCommandExceptionType(Text.of("Unknown damage type"));
    private static final DamageSourceArgumentType INSTANCE = new DamageSourceArgumentType();

    private static final Map<String, DamageSource> SOURCES = ImmutableMap.<String, DamageSource>builder()
            .put("in_fire", DamageSource.IN_FIRE)
            .put("lightning_bolt", DamageSource.LIGHTNING_BOLT)
            .put("on_fire", DamageSource.ON_FIRE)
            .put("lava", DamageSource.LAVA)
            .put("hot_floor", DamageSource.HOT_FLOOR)
            .put("in_wall", DamageSource.IN_WALL)
            .put("cramming", DamageSource.CRAMMING)
            .put("drown", DamageSource.DROWN)
            .put("starve", DamageSource.STARVE)
            .put("cactus", DamageSource.CACTUS)
            .put("fall", DamageSource.FALL)
            .put("fly_into_wall", DamageSource.FLY_INTO_WALL)
            .put("out_of_world", DamageSource.OUT_OF_WORLD)
            .put("generic", DamageSource.GENERIC)
            .put("magic", DamageSource.MAGIC)
            .put("wither", DamageSource.WITHER)
            .put("dragon_breath", DamageSource.DRAGON_BREATH)
            .put("dryout", DamageSource.DRYOUT)
            .put("sweet_berry_bush", DamageSource.SWEET_BERRY_BUSH)
            .put("freeze", DamageSource.FREEZE)
            .put("stalagmite", DamageSource.STALAGMITE).build();

    public static DamageSourceArgumentType damageSource() {
        return INSTANCE;
    }

    public static <S> DamageSource getDamageSource(CommandContext<S> context, String name) {
        return context.getArgument(name, DamageSource.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(SOURCES.keySet(), builder);
    }

    @Override
    public DamageSource parse(StringReader reader) throws CommandSyntaxException {
        return Optional.ofNullable(SOURCES.get(reader.readString())).orElseThrow(UNKNOWN_SOURCE::create);
    }
}
