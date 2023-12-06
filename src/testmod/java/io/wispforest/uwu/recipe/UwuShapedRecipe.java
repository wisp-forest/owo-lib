package io.wispforest.uwu.recipe;

//public class UwuShapedRecipe extends ShapedRecipe {
//
//    public static RecipeSerializer<UwuShapedRecipe> RECIPE_SERIALIZER;
//
//    public UwuShapedRecipe(String group, CraftingRecipeCategory category, int width, int height, DefaultedList<Ingredient> ingredients, ItemStack result, boolean showNotification) {
//        super(group, category, width, height, ingredients, result, showNotification);
//    }
//
//    @Override
//    public RecipeSerializer<?> getSerializer() {
//        return RECIPE_SERIALIZER;
//    }
//
//    public static void init() {
//        RECIPE_SERIALIZER = Registry.register(
//                Registries.RECIPE_SERIALIZER,
//                new Identifier("uwu:crafting_shaped"),
//                new EndecRecipeSerializer<>(ENDEC)
//        );
//    }
//
//    //--
//
//    private static final Endec<UwuShapedRecipe> FROM_RAW_RECIPE = RawShapedRecipe.ENDEC.xmap(recipe -> {
//        String[] strings = ShapedRecipeInvoker.owo$removePadding(recipe.pattern);
//        int i = strings[0].length();
//        int j = strings.length;
//        DefaultedList<Ingredient> defaultedList = DefaultedList.ofSize(i * j, Ingredient.EMPTY);
//        Set<String> set = Sets.newHashSet(recipe.key.keySet());
//
//        for (int k = 0; k < strings.length; ++k) {
//            String string = strings[k];
//
//            for (int l = 0; l < string.length(); ++l) {
//                String string2 = string.substring(l, l + 1);
//                Ingredient ingredient = string2.equals(" ") ? Ingredient.EMPTY : (Ingredient) recipe.key.get(string2);
//
//                if (ingredient == null) {
//                    throw new IllegalStateException("Pattern references symbol '" + string2 + "' but it's not defined in the key");
//                }
//
//                set.remove(string2);
//                defaultedList.set(l + i * k, ingredient);
//            }
//        }
//
//        if (!set.isEmpty()) throw new IllegalStateException("Key defines symbols that aren't used in pattern: " + set);
//
//        return new UwuShapedRecipe(recipe.group, recipe.category, i, j, defaultedList, recipe.result, recipe.showNotification);
//    }, recipe -> {
//        throw new NotImplementedException("Serializing ShapedRecipe is not implemented yet.");
//    });
//
//    private static final Endec<DefaultedList<Ingredient>> INGREDIENTS = Endec.ofCodec(Ingredient.ALLOW_EMPTY_CODEC)
//            .listOf()
//            .xmap(ingredients -> new DefaultedList<>(ingredients, null) {}, defaulted -> defaulted);
//
//    private static final Endec<UwuShapedRecipe> FROM_INSTANCE = StructEndecBuilder.of(
//            Endec.STRING.fieldOf("group", ShapedRecipe::getGroup),
//            Endec.ofCodec(CraftingRecipeCategory.CODEC).fieldOf("category", ShapedRecipe::getCategory),
//            Endec.VAR_INT.fieldOf("width", ShapedRecipe::getWidth),
//            Endec.VAR_INT.fieldOf("height", ShapedRecipe::getHeight),
//            INGREDIENTS.fieldOf("ingredients", ShapedRecipe::getIngredients),
//            Endec.ofCodec(ItemStack.RECIPE_RESULT_CODEC).fieldOf("result", recipe -> recipe.getResult(null)),
//            Endec.BOOLEAN.fieldOf("show_notification", ShapedRecipe::showNotification),
//            UwuShapedRecipe::new
//    );
//
//    private static final Endec<UwuShapedRecipe> ENDEC = new AttributeEndecBuilder<>(FROM_RAW_RECIPE, SerializationAttribute.HUMAN_READABLE)
//            .orElse(FROM_INSTANCE);
//
//    //--
//
//
//    private record RawShapedRecipe(String group, CraftingRecipeCategory category, Map<String, Ingredient> key,
//                                   List<String> pattern, ItemStack result, boolean showNotification) {
//        private static final Endec<List<String>> PATTERN_ENDEC = Endec.STRING.listOf().validate(rows -> {
//            if (rows.size() > 3) throw new IllegalStateException("Invalid pattern: too many rows, 3 is maximum");
//            if (rows.isEmpty()) throw new IllegalStateException("Invalid pattern: empty pattern not allowed");
//
//            int i = rows.get(0).length();
//
//            for (String string : rows) {
//                if (string.length() > 3) {
//                    throw new IllegalStateException("Invalid pattern: too many columns, 3 is maximum");
//                }
//                if (i != string.length()) {
//                    throw new IllegalStateException("Invalid pattern: each row must be the same width");
//                }
//            }
//        });
//
//        public static final Endec<RawShapedRecipe> ENDEC = StructEndecBuilder.of(
//                Endec.STRING.optionalFieldOf("group", recipe -> recipe.group, ""),
//                Endec.ofCodec(CraftingRecipeCategory.CODEC).optionalFieldOf("category", recipe -> recipe.category, CraftingRecipeCategory.MISC),
//                Endec.ofCodec(Ingredient.DISALLOW_EMPTY_CODEC).mapOf().validate(map -> {
//                    for (var key : map.keySet()) {
//                        if (key.length() != 1) {
//                            throw new IllegalStateException("Invalid key entry: '" + key + "' is an invalid symbol (must be 1 character only).");
//                        } else if (" ".equals(key)) {
//                            throw new IllegalStateException("Invalid key entry: ' ' is a reserved symbol.");
//                        }
//                    }
//                }).fieldOf("key", recipe -> recipe.key),
//                PATTERN_ENDEC.fieldOf("pattern", recipe -> recipe.pattern),
//                Endec.ofCodec(ItemStack.RECIPE_RESULT_CODEC).fieldOf("result", recipe -> recipe.result),
//                Endec.BOOLEAN.optionalFieldOf("show_notification", recipe -> recipe.showNotification, true),
//                RawShapedRecipe::new
//        );
//    }
//}