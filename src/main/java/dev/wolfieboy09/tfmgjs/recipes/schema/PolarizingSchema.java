package dev.wolfieboy09.tfmgjs.recipes.schema;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeConstructor;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

public interface PolarizingSchema {
    RecipeKey<InputItem[]> INGREDIENTS = ItemComponents.INPUT_ARRAY.key("ingredients");
    RecipeKey<OutputItem[]> RESULTS = ItemComponents.OUTPUT_ARRAY.key("results");
    RecipeKey<Integer> ENERGY = NumberComponent.INT.key("energy");

    RecipeConstructor.Factory FACTORY = (recipe, schemaType, key, from) -> {
        recipe.setValue(ENERGY, from.getValue(recipe, ENERGY));
        InputItem[] ingredients = from.getValue(recipe, INGREDIENTS);
        if (ingredients.length != 1) {
            throw new RecipeExceptionJS("Recipe can only a max of 1 ingredients");
        }
        recipe.setValue(INGREDIENTS, ingredients);
        OutputItem[] results = from.getValue(recipe, RESULTS);
        if (results.length != 1) {
            throw new RecipeExceptionJS("Recipe can only a max of 1 results");
        }
        recipe.setValue(RESULTS, results);
    };

    RecipeSchema SCHEMA = new RecipeSchema(INGREDIENTS, RESULTS, ENERGY)
            .constructor(FACTORY, INGREDIENTS, RESULTS, ENERGY);
}
