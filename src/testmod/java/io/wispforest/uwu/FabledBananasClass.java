package io.wispforest.uwu;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.BuiltInEndecs;
import io.wispforest.owo.serialization.endec.StructEndecBuilder;
import io.wispforest.owo.serialization.format.json.JsonDeserializer;
import io.wispforest.owo.serialization.format.json.JsonSerializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

public class FabledBananasClass {

    public static final Endec<FabledBananasClass> ENDEC = StructEndecBuilder.of(
            Endec.INT.fieldOf("banana_amount", FabledBananasClass::bananaAmount),
            BuiltInEndecs.ofRegistry(Registries.ITEM).fieldOf("banana_item", FabledBananasClass::bananaItem),
            BuiltInEndecs.BLOCK_POS.listOf().fieldOf("banana_positions", FabledBananasClass::bananaPositions),
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
        JsonElement result = BuiltInEndecs.BLOCK_POS.encodeFully(JsonSerializer::of, pos);

        System.out.println(result);
        BlockPos decoded = BuiltInEndecs.BLOCK_POS.decodeFully(JsonDeserializer::of, result);


        Endec<Map<Identifier, Integer>> endec = Endec.map(Identifier::toString, Identifier::new, Endec.INT);
        System.out.println(endec.encodeFully(JsonSerializer::of, Map.of(new Identifier("a"), 6, new Identifier("b"), 9)).toString());
        System.out.println(endec.decodeFully(JsonDeserializer::of, new Gson().fromJson("{\"a:b\":24,\"c\":17}", JsonObject.class)));

        Endec<Map<BlockPos, Identifier>> mappy = Endec.map(BuiltInEndecs.BLOCK_POS, BuiltInEndecs.IDENTIFIER);
        System.out.println(mappy.encodeFully(JsonSerializer::of, Map.of(BlockPos.ORIGIN, new Identifier("a"), new BlockPos(69, 420, 489), new Identifier("bruh:l"))).toString());
        System.out.println(mappy.decodeFully(JsonDeserializer::of, new Gson().fromJson("[{\"k\":[69,420,489],\"v\":\"bruh:l\"},{\"k\":[0,0,0],\"v\":\"minecraft:a\"}]", JsonArray.class)));
    }
}
