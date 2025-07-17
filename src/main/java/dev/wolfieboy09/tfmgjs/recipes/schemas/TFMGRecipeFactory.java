package dev.wolfieboy09.tfmgjs.recipes.schemas;

import com.mojang.datafixers.util.Either;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentValueMap;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.TimeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TFMGRecipeFactory {

    @Nullable
    RecipeKey<InputFluid[]> FLUID_INGREDIENTS = null;
    @Nullable
    RecipeKey<OutputFluid[]> FLUID_RESULTS = null;
    @Nullable
    RecipeKey<InputItem[]> ITEM_INGREDIENTS = null;
    @Nullable
    RecipeKey<OutputItem[]> ITEM_RESULTS = null;
    @Nullable
    RecipeKey<Either<InputFluid, InputItem>[]> INGREDIENTS = null;
    @Nullable
    RecipeKey<Either<OutputFluid, OutputItem>[]> RESULTS = null;

    boolean hasProcessingTime = false;
    boolean usesEnergy = false;

    private int maxItemOutputs = 1;
    private int maxFluidOutputs = 1;
    private int maxItemInputs = 1;
    private int maxFluidInputs = 1;

    TFMGRecipeFactory() {

    }

    public TFMGRecipeFactory create(RecipeKey<InputFluid[]> fluidIngredients, RecipeKey<OutputFluid[]> fluidResults, RecipeKey<InputItem[]> itemIngredients, RecipeKey<OutputItem[]> itemResults) {
        this.FLUID_INGREDIENTS = fluidIngredients;
        this.FLUID_RESULTS = fluidResults;
        this.ITEM_INGREDIENTS = itemIngredients;
        this.ITEM_RESULTS = itemResults;
        return this;
    }

    public TFMGRecipeFactory hasTime() {
        this.hasProcessingTime = true;
        return this;
    }

    public TFMGRecipeFactory usesEnergy() {
        this.usesEnergy = true;
        return this;
    }

    public TFMGRecipeFactory itemOutputs(RecipeKey<OutputItem[]> itemResults, int maxItemOutputs) {
        this.ITEM_RESULTS = itemResults;
        this.maxItemOutputs = maxItemOutputs;
        return this;
    }
    public TFMGRecipeFactory fluidOutputs(RecipeKey<OutputFluid[]> fluidResults, int maxFluidOutputs) {
        this.FLUID_RESULTS = fluidResults;
        this.maxFluidOutputs = maxFluidOutputs;
        return this;
    }
    public TFMGRecipeFactory itemInputs(RecipeKey<InputItem[]> itemIngredients, int maxItemInputs) {
        this.ITEM_INGREDIENTS = itemIngredients;
        this.maxItemInputs = maxItemInputs;
        return this;
    }
    public TFMGRecipeFactory fluidInputs(RecipeKey<InputFluid[]> fluidIngredients, int maxFluidInputs) {
        this.FLUID_INGREDIENTS = fluidIngredients;
        this.maxFluidInputs = maxFluidInputs;
        return this;
    }
    public TFMGRecipeFactory ingredients(RecipeKey<Either<InputFluid, InputItem>[]> ingredients, int maxItemInputs, int maxFluidInputs) {
        this.INGREDIENTS = ingredients;
        this.maxItemInputs = maxItemInputs;
        this.maxFluidInputs = maxFluidInputs;
        return this;
    }
    public TFMGRecipeFactory results(RecipeKey<Either<OutputFluid, OutputItem>[]> results, int maxItemOutputs, int maxFluidOutputs) {
        this.RESULTS = results;
        this.maxItemOutputs = maxItemOutputs;
        this.maxFluidOutputs = maxFluidOutputs;
        return this;
    }


    RecipeConstructor.Factory factory = (recipe, schemaType, keys, from) -> {
        keyCheck();
        if (ITEM_RESULTS != null) recipe.setValue(ITEM_RESULTS, from.getValue(recipe, ITEM_RESULTS));
        if (FLUID_RESULTS != null) recipe.setValue(FLUID_RESULTS, from.getValue(recipe, FLUID_RESULTS));
        if (RESULTS != null) recipe.setValue(RESULTS, from.getValue(recipe, RESULTS));

        if (ITEM_INGREDIENTS != null) handleItemIngredients(recipe, from);
        if (FLUID_INGREDIENTS != null) handleFluidIngredients(recipe, from);
        if (INGREDIENTS != null) handlePairedIngredients(recipe, from);


        if (hasProcessingTime) recipe.setValue(TFMGRecipeSchema.PROCESSING_TIME, from.getValue(recipe, TFMGRecipeSchema.PROCESSING_TIME));
        if (usesEnergy) recipe.setValue(TFMGRecipeSchema.ENERGY, from.getValue(recipe, TFMGRecipeSchema.ENERGY));


        if (ITEM_RESULTS != null) handleItemResults(recipe, from);
        if (FLUID_RESULTS != null) handleFluidResults(recipe, from);
        if (RESULTS != null) handlePairedResults(recipe, from);
    };

    private void keyCheck() {
        if (FLUID_INGREDIENTS == null && ITEM_INGREDIENTS == null && INGREDIENTS == null) {
            throw new RecipeExceptionJS("Recipe must have at least one ingredient type defined");
        }
        if (FLUID_RESULTS == null && ITEM_RESULTS == null && RESULTS == null) {
            throw new RecipeExceptionJS("Recipe must have at least one result type defined");
        }
        if (INGREDIENTS != null && (ITEM_INGREDIENTS != null || FLUID_INGREDIENTS != null)) {
            throw new RecipeExceptionJS("Recipe cannot have both paired ingredients and separate item/fluid ingredients defined");
        }
        if (RESULTS != null && (ITEM_RESULTS != null || FLUID_RESULTS != null)) {
            throw new RecipeExceptionJS("Recipe cannot have both paired results and separate item/fluid results defined");
        }
    }

    private void handleFluidIngredients(RecipeJS recipe, ComponentValueMap from) {
        InputFluid[] ingredients = from.getValue(recipe, FLUID_INGREDIENTS);

        if (ingredients.length > maxFluidInputs) {
            throw new RecipeExceptionJS("Recipe can only a max of " + maxFluidInputs + " fluid ingredients");
        }

        recipe.setValue(FLUID_INGREDIENTS, ingredients);
    }

    private void handleItemIngredients(RecipeJS recipe, ComponentValueMap from) {
        InputItem[] ingredients = from.getValue(recipe, ITEM_INGREDIENTS);

        if (ingredients.length > maxItemInputs) {
            throw new RecipeExceptionJS("Recipe can only a max of " + maxItemInputs + " item ingredients");
        }

        recipe.setValue(ITEM_INGREDIENTS, ingredients);
    }

    private void handlePairedIngredients(RecipeJS recipe, ComponentValueMap from) {
        Either<InputFluid, InputItem>[] ingredients = from.getValue(recipe, INGREDIENTS);

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

        if (fluidIngredients.size() > maxFluidInputs)
            throw new RecipeExceptionJS("Recipe can only a max of " + maxFluidInputs + " fluid ingredients");
        if (itemIngredients.size() > maxItemInputs)
            throw new RecipeExceptionJS("Recipe can only a max of " + maxItemInputs + " item ingredients");

        recipe.setValue(INGREDIENTS, ingredients);
    }

    private void handleFluidResults(RecipeJS recipe, ComponentValueMap from) {
        OutputFluid[] results = from.getValue(recipe, FLUID_RESULTS);

        if (results.length > maxFluidOutputs) {
            throw new RecipeExceptionJS("Recipe can only a max of " + maxFluidOutputs + " fluid results");
        }

        recipe.setValue(FLUID_RESULTS, results);
    }

    private void handleItemResults(RecipeJS recipe, ComponentValueMap from) {
        OutputItem[] results = from.getValue(recipe, ITEM_RESULTS);

        if (results.length > maxItemOutputs) {
            throw new RecipeExceptionJS("Recipe can only a max of " + maxItemOutputs + " item results");
        }

        recipe.setValue(ITEM_RESULTS, results);
    }

    private void handlePairedResults(RecipeJS recipe, ComponentValueMap from) {
        Either<OutputFluid, OutputItem>[] results = from.getValue(recipe, RESULTS);

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

        if (fluidResults.size() > maxFluidOutputs)
            throw new RecipeExceptionJS("Recipe can only a max of " + maxFluidOutputs + " fluid results");
        if (itemResults.size() > maxItemOutputs)
            throw new RecipeExceptionJS("Recipe can only a max of " + maxItemOutputs + " item results");

        recipe.setValue(RESULTS, results);
    }
}
