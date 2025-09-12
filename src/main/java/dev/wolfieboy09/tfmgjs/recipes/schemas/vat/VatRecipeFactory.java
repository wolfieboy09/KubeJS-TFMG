package dev.wolfieboy09.tfmgjs.recipes.schemas.vat;

import com.mojang.datafixers.util.Either;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentValueMap;
import dev.latvian.mods.kubejs.recipe.schema.RecipeConstructor;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class VatRecipeFactory {
    public VatRecipeFactory() {}

    // It works, so sssshhhhhh, and don't complain to me
    private static <T> T getOrDefault(RecipeJS recipe, RecipeSchemaType schemaType, @NotNull ComponentValueMap from, @NotNull RecipeKey<T> recipeKey) {
        if (recipeKey.optional() && from.getValue(recipe, recipeKey) == null) {
            return recipeKey.optional.getDefaultValue(schemaType);
        }
        return from.getValue(recipe, recipeKey);
    }

    RecipeConstructor.Factory factory = (recipe, schemaType, keys, from) -> {
        String[] machines = getOrDefault(recipe, schemaType, from, VatRecipeSchema.MACHINES);
        String[] vatTypes = getOrDefault(recipe, schemaType, from, VatRecipeSchema.VAT_TYPES);
        Integer minSize =  getOrDefault(recipe, schemaType, from, VatRecipeSchema.MIN_SIZE);
        HeatCondition heatCondition = getOrDefault(recipe, schemaType, from, VatRecipeSchema.HEAT_REQUIREMENT);
        Long processingTime = getOrDefault(recipe, schemaType, from, VatRecipeSchema.PROCESSING_TIME_REQUIRED);


        handleIngredients(recipe, from);
        handleResults(recipe, from);

        recipe.setValue(VatRecipeSchema.MACHINES, machines);
        recipe.setValue(VatRecipeSchema.VAT_TYPES, vatTypes);
        if (minSize <= 0)
            throw new RecipeExceptionJS("Vat Recipe min size must be greater than 0");
        recipe.setValue(VatRecipeSchema.MIN_SIZE, minSize);
        recipe.setValue(VatRecipeSchema.HEAT_REQUIREMENT, heatCondition);
        recipe.setValue(VatRecipeSchema.PROCESSING_TIME_REQUIRED, processingTime);
    };

    private void handleIngredients(RecipeJS recipe,  @NotNull ComponentValueMap from) {
        Either<InputFluid, InputItem>[] ingredients = from.getValue(recipe, VatRecipeSchema.INGREDIENTS);

        List<InputFluid> fluidIngredients = new ArrayList<>();
        List<InputItem> itemIngredients = new ArrayList<>();

        for (Either<InputFluid, InputItem> ingredient : ingredients) {
            if (ingredient.left().isPresent()) {
                fluidIngredients.add(ingredient.left().get());
            }
            if (ingredient.right().isPresent()) {
                itemIngredients.add(ingredient.right().get());
            }
        }

        if (fluidIngredients.size() > 4)
            throw new RecipeExceptionJS("Vat Recipe can only have a max of 4 fluid ingredients");
        if (itemIngredients.size() > 4)
            throw new RecipeExceptionJS("Vat Recipe can only have a max of 4 item ingredients");

        recipe.setValue(VatRecipeSchema.INGREDIENTS, ingredients);
    }

    private void handleResults(RecipeJS recipe, @NotNull ComponentValueMap from) {
        Either<OutputFluid, OutputItem>[] results = from.getValue(recipe, VatRecipeSchema.RESULTS);

        List<OutputFluid> fluidResults = new ArrayList<>();
        List<OutputItem> itemResults = new ArrayList<>();

        for (Either<OutputFluid, OutputItem> result : results) {
            if (result.left().isPresent()) {
                fluidResults.add(result.left().get());
            }
            if (result.right().isPresent()) {
                itemResults.add(result.right().get());
            }
        }

        if (fluidResults.size() > 4)
            throw new RecipeExceptionJS("Vat Recipe can only have a max of 4 fluid outputs");
        if (itemResults.size() > 4)
            throw new RecipeExceptionJS("Vat Recipe can only have a max of 4 item outputs");

        recipe.setValue(VatRecipeSchema.RESULTS, results);
    }
}
