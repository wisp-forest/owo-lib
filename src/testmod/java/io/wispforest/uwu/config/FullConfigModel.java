package io.wispforest.uwu.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.Nest;
import io.wispforest.owo.config.annotation.SectionHeader;
import net.minecraft.util.Identifier;

import java.util.*;

@Modmenu(modId = "uwu", priorityOrder = 0)
@Config(modId = "uwu", name = "full_test", wrapperName = "FullTest")
public class FullConfigModel {

    public int number1 = 21;

    public String string1 = "valid";

    public Identifier id1 = Identifier.of("minecraft_thing");

    @SectionHeader("struct_objects")
    public StructTestObj1 struct1 = new StructTestObj1();

    public StructTestObj2 struct2 = new StructTestObj2();

    public RecordTestObj struct3 = new RecordTestObj();

    //--

    @SectionHeader("list_objects")
    public List<Integer> listNumbers = List.of(1);

    public List<String> listStrings = List.of("one");

    public List<Identifier> listIds = List.of(Identifier.of("test", "one"));

    public List<StructTestObj1> listStructList1 = List.of(new StructTestObj1());

    public List<StructTestObj2> listStructList2 = List.of(new StructTestObj2());

    public List<RecordTestObj> listStructList3 = List.of(new RecordTestObj());

    //--

    @SectionHeader("set_objects")
    public Set<Integer> setNumbers = Set.of(1);

    public Set<String> setStrings = Set.of("one");

    public Set<Identifier> setIds = Set.of(Identifier.of("test", "one"));

    public Set<StructTestObj1> setStruct1 = Set.of(new StructTestObj1());

    public Set<StructTestObj2> setStruct2 = Set.of(new StructTestObj2());

    public Set<RecordTestObj> setStruct3 = Set.of(new RecordTestObj());

    //--

    @SectionHeader("string_map_objects")
    public Map<String, Integer> mapStringToNumber = Map.of("test", 3);

    public Map<String, String> mapStringToString = Map.of("test", "wooo");

    //--

    @SectionHeader("int_map_objects")
    public Map<Integer, Integer> mapNumberToNumber = Map.of(1, 1);

    public Map<Integer, String> mapNumberToString = Map.of(3, "weee");

}
