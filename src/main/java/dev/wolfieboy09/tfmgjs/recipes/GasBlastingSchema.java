package dev.wolfieboy09.tfmgjs.recipes;

import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.FluidComponents;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeConstructor;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

public interface GasBlastingSchema {
    RecipeKey<InputFluid[]> INGREDIENTS = FluidComponents.INPUT_ARRAY.key("ingredients");
    RecipeKey<OutputFluid[]> RESULTS = FluidComponents.OUTPUT_ARRAY.key("results");
    RecipeKey<Long> TIME = TimeComponent.TICKS.key("processingTime");

    RecipeConstructor.Factory FACTORY = (recipe, schemaType, keys, from) -> {
        recipe.setValue(TIME, from.getValue(recipe, TIME));

        InputFluid[] ingredients = from.getValue(recipe, INGREDIENTS);
        if (ingredients.length > 2) {
            throw new RecipeExceptionJS("Recipe can only a max of 2 ingredients");
        }
        recipe.setValue(INGREDIENTS, ingredients);

        OutputFluid[] results = from.getValue(recipe, RESULTS);
        if (results.length > 2) {
            throw new RecipeExceptionJS("Recipe can only a max of 2 results");
        }
        recipe.setValue(RESULTS, results);

    };

    RecipeSchema SCHEMA = new RecipeSchema(INGREDIENTS, RESULTS, TIME)
            .constructor(FACTORY, INGREDIENTS, RESULTS, TIME);
}
