package io.wispforest.uwu;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.wispforest.endec.Endec;
import io.wispforest.endec.format.json.GsonDeserializer;
import io.wispforest.endec.format.json.GsonSerializer;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

public class FabledBananasClass {

    public static final Endec<FabledBananasClass> ENDEC = StructEndecBuilder.of(
            Endec.INT.fieldOf("banana_amount", FabledBananasClass::bananaAmount),
            MinecraftEndecs.ofRegistry(Registries.ITEM).fieldOf("banana_item", FabledBananasClass::bananaItem),
            MinecraftEndecs.BLOCK_POS.listOf().fieldOf("banana_positions", FabledBananasClass::bananaPositions),
            FabledBananasClass::new
    );

    private final int bananaAmount;
    private final Item bananaItem;
    private final List<BlockPos> bananaPositions;

    public FabledBananasClass(int bananaAmount, Item bananaItem, List<BlockPos> bananaPositions) {
        this.bananaAmount = bananaAmount;
        this.bananaItem = bananaItem;
        this.bananaPositions = bananaPositions;
    }

    public int bananaAmount() {return this.bananaAmount;}
    public Item bananaItem() {return this.bananaItem;}
    public List<BlockPos> bananaPositions() {return this.bananaPositions;}

    public static void main(String[] args) {
        var pos = new BlockPos(1, 2, 3);
        JsonElement result = MinecraftEndecs.BLOCK_POS.encodeFully(GsonSerializer::of, pos);

        System.out.println(result);
        BlockPos decoded = MinecraftEndecs.BLOCK_POS.decodeFully(GsonDeserializer::of, result);


        Endec<Map<Identifier, Integer>> endec = Endec.map(Identifier::toString, Identifier::of, Endec.INT);
        System.out.println(endec.encodeFully(GsonSerializer::of, Map.of(Identifier.of("a"), 6, Identifier.of("b"), 9)).toString());
        System.out.println(endec.decodeFully(GsonDeserializer::of, new Gson().fromJson("{\"a:b\":24,\"c\":17}", JsonObject.class)));

        Endec<Map<BlockPos, Identifier>> mappy = Endec.map(MinecraftEndecs.BLOCK_POS, MinecraftEndecs.IDENTIFIER);
        System.out.println(mappy.encodeFully(GsonSerializer::of, Map.of(BlockPos.ORIGIN, Identifier.of("a"), new BlockPos(69, 420, 489), Identifier.of("bruh:l"))).toString());
        System.out.println(mappy.decodeFully(GsonDeserializer::of, new Gson().fromJson("[{\"k\":[69,420,489],\"v\":\"bruh:l\"},{\"k\":[0,0,0],\"v\":\"minecraft:a\"}]", JsonArray.class)));
    }
}
