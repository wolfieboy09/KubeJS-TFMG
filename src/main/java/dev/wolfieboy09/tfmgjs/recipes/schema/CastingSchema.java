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
import dev.wolfieboy09.tfmgjs.recipes.CastingJS;

public interface CastingSchema {
    RecipeKey<InputFluid> INGREDIENTS = FluidComponents.INPUT.key("ingredients");
    RecipeKey<OutputItem[]> RESULTS = ItemComponents.OUTPUT.asArray().key("results");
    RecipeKey<Long> TIME = TimeComponent.TICKS.key("processingTime").alt("time");

    RecipeConstructor.Factory FACTORY = (recipe, schemaType, keys, from) -> {
        recipe.setValue(RESULTS, from.getValue(recipe, RESULTS));

        OutputItem[] outputItems = recipe.getValue(RESULTS);
        switch (outputItems.length) {
            case 1, 2, 3 -> recipe.setValue(INGREDIENTS, from.getValue(recipe, INGREDIENTS));
            default -> throw new RecipeExceptionJS("Casting recipe cannot have more then 3 item outputs");
        }
    };

    RecipeSchema SCHEMA = new RecipeSchema(CastingJS.class, CastingJS::new, INGREDIENTS, RESULTS, TIME)
            .constructor(FACTORY, INGREDIENTS, RESULTS, TIME);
}
