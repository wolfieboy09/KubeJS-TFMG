package dev.wolfieboy09.tfmgjs.recipes;

import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.FluidComponents;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeConstructor;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

public interface IndustrialBlastingSchema {
    RecipeKey<InputItem[]> INGREDIENT = ItemComponents.INPUT_ARRAY.key("ingredients");
    RecipeKey<OutputFluid[]> RESULTS = FluidComponents.OUTPUT_ARRAY.key("results");
    RecipeKey<Long> TIME = TimeComponent.TICKS.key("processingTime");

    RecipeConstructor.Factory FACTORY = (recipe, schemaType, keys, from) -> {
        InputItem[] ingredients = from.getValue(recipe, INGREDIENT);
        if (ingredients.length > 1) {
            throw new RecipeExceptionJS("Recipe can only have 1 item input");
        }
        recipe.setValue(INGREDIENT, ingredients);
        recipe.setValue(TIME, from.getValue(recipe, TIME));
        OutputFluid[] results = from.getValue(recipe, RESULTS);
        if (results.length > 2) {
            throw new RecipeExceptionJS("Recipe can only have up to 2 fluid outputs");
        }
        recipe.setValue(RESULTS, results);
    };

    RecipeSchema SCHEMA = new RecipeSchema(INGREDIENT, RESULTS, TIME)
            .constructor(FACTORY, INGREDIENT, RESULTS, TIME);

}
