package dev.wolfieboy09.tfmgjs.recipes.schema;

import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.FluidComponents;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeConstructor;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

public interface CastingSchema {
    RecipeKey<InputFluid> INGREDIENTS = FluidComponents.INPUT.key("ingredients");
    RecipeKey<OutputItem[]> RESULTS = ItemComponents.OUTPUT.asArray().key("results");
    RecipeKey<Long> TIME = TimeComponent.TICKS.key("processingTime").alt("time");

    RecipeConstructor.Factory FACTORY = (recipe, schemaType, keys, from) -> {
        recipe.setValue(RESULTS, from.getValue(recipe, RESULTS));
        recipe.setValue(INGREDIENTS, from.getValue(recipe, INGREDIENTS));
        recipe.setValue(TIME, from.getValue(recipe, TIME));

        OutputItem[] outputItems = recipe.getValue(RESULTS);
        switch (outputItems.length) {
            case 1, 2, 3 -> recipe.setValue(RESULTS, from.getValue(recipe, RESULTS));
            default -> throw new RecipeExceptionJS("Casting recipe cannot have more then 3 item outputs");
        }
    };

    RecipeSchema SCHEMA = new RecipeSchema(INGREDIENTS, RESULTS, TIME)
            .constructor(FACTORY, INGREDIENTS, RESULTS, TIME);
}
