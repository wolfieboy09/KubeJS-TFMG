package dev.wolfieboy09.tfmgjs.recipes;

import com.mojang.datafixers.util.Either;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.FluidComponents;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeConstructor;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

public interface CokingSchema {
    RecipeKey<InputItem[]> INGREDIENT = ItemComponents.INPUT_ARRAY.key("ingredients");
    RecipeKey<Either<OutputFluid, OutputItem>[]> RESULTS = FluidComponents.OUTPUT_OR_ITEM_ARRAY.key("results");
    RecipeKey<Long> TIME = TimeComponent.TICKS.key("processingTime");

    RecipeConstructor.Factory FACTORY = (recipe, schemaType, keys, from) -> {
        recipe.setValue(TIME, from.getValue(recipe, TIME));
        if (from.getValue(recipe, INGREDIENT).length > 1) {
            throw new RecipeExceptionJS("Recipe can only a max of 1 ingredients");
        }

        recipe.setValue(INGREDIENT, from.getValue(recipe, INGREDIENT));

        Either<OutputFluid, OutputItem>[] results = from.getValue(recipe, RESULTS);
        if (results.length != 2) {
            throw new RecipeExceptionJS("Recipe must have exactly one item and one fluid output");
        }

        OutputFluid fluidOutput = null;
        OutputItem itemOutput = null;

        for (Either<OutputFluid, OutputItem> result : results) {
            if (result.left().isPresent()) {
                if (fluidOutput != null) {
                    throw new RecipeExceptionJS("Multiple fluid outputs are not allowed");
                }
                fluidOutput = result.left().get();
            } else if (result.right().isPresent()) {
                if (itemOutput != null) {
                    throw new RecipeExceptionJS("Multiple item outputs are not allowed");
                }
                itemOutput = result.right().get();
            }
        }

        if (fluidOutput == null || itemOutput == null) {
            throw new RecipeExceptionJS("Recipe must have exactly one fluid and one item output");
        }

        recipe.setValue(RESULTS, from.getValue(recipe, RESULTS));
    };

    RecipeSchema SCHEMA = new RecipeSchema(INGREDIENT, RESULTS, TIME)
            .constructor(FACTORY, INGREDIENT, RESULTS, TIME);


}
