package dev.wolfieboy09.tfmgjs.recipes.schema;

import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.FluidComponents;
import dev.latvian.mods.kubejs.recipe.schema.RecipeConstructor;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

public interface DistillationSchema {
    RecipeKey<InputFluid[]> INGREDIENT = FluidComponents.INPUT_ARRAY.key("ingredients");
    RecipeKey<OutputFluid[]> RESULTS = FluidComponents.OUTPUT_ARRAY.key("results");

    RecipeConstructor.Factory FACTORY = (recipe, schemaType, keys, from) -> {
        if (from.getValue(recipe, INGREDIENT).length != 1) {
            throw new RecipeExceptionJS("Recipe can only a max of 1 ingredients");
        }
        recipe.setValue(INGREDIENT, from.getValue(recipe, INGREDIENT));

        if (from.getValue(recipe, RESULTS).length > 6) {
            throw new RecipeExceptionJS("Recipe can only have up to 6 fluid outputs");
        }
        recipe.setValue(RESULTS, from.getValue(recipe, RESULTS));
    };

    RecipeSchema SCHEMA = new RecipeSchema(INGREDIENT, RESULTS)
            .constructor(FACTORY, INGREDIENT, RESULTS);
}
