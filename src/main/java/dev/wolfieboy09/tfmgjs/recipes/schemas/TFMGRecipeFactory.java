package dev.wolfieboy09.tfmgjs.recipes.schemas;

import com.mojang.datafixers.util.Either;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ComponentValueMap;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import javax.annotation.Nullable;
import java.util.List;

public final class TFMGRecipeFactory {
    @Nullable
    RecipeKey<List<FluidIngredient>> FLUID_INGREDIENTS = null;
    @Nullable
    RecipeKey<List<FluidStack>> FLUID_RESULTS = null;
    @Nullable
    RecipeKey<List<Ingredient>> ITEM_INGREDIENTS = null;
    @Nullable
    RecipeKey<List<ItemStack>> ITEM_RESULTS = null;
    @Nullable
    RecipeKey<List<Either<FluidIngredient, Ingredient>>> INGREDIENTS = null;
    @Nullable
    RecipeKey<List<Either<FluidStack, ItemStack>>> RESULTS = null;

    boolean hasProcessingTime = false;
    boolean usesEnergy = false;

    private int maxItemOutputs = 0;
    private int maxFluidOutputs = 0;
    private int maxItemInputs = 0;
    private int maxFluidInputs = 0;

    public TFMGRecipeFactory() {}

    public TFMGRecipeFactory create(RecipeKey<List<FluidIngredient>> fluidIngredients, RecipeKey<List<FluidStack>> fluidResults, RecipeKey<List<Ingredient>> itemIngredients, RecipeKey<List<ItemStack>> itemResults) {
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

    public TFMGRecipeFactory itemOutputs(RecipeKey<List<ItemStack>> itemResults, int maxItemOutputs) {
        this.ITEM_RESULTS = itemResults;
        this.maxItemOutputs = maxItemOutputs;
        return this;
    }

    public TFMGRecipeFactory fluidOutputs(RecipeKey<List<FluidStack>> fluidResults, int maxFluidOutputs) {
        this.FLUID_RESULTS = fluidResults;
        this.maxFluidOutputs = maxFluidOutputs;
        return this;
    }

    public TFMGRecipeFactory itemInputs(RecipeKey<List<Ingredient>> itemIngredients, int maxItemInputs) {
        this.ITEM_INGREDIENTS = itemIngredients;
        this.maxItemInputs = maxItemInputs;
        return this;
    }

    public TFMGRecipeFactory fluidInputs(RecipeKey<List<FluidIngredient>> fluidIngredients, int maxFluidInputs) {
        this.FLUID_INGREDIENTS = fluidIngredients;
        this.maxFluidInputs = maxFluidInputs;
        return this;
    }

    public TFMGRecipeFactory ingredients(RecipeKey<List<Either<FluidIngredient, Ingredient>>> ingredients, int maxItemInputs, int maxFluidInputs) {
        this.INGREDIENTS = ingredients;
        this.maxItemInputs = maxItemInputs;
        this.maxFluidInputs = maxFluidInputs;
        return this;
    }

    public TFMGRecipeFactory results(RecipeKey<List<Either<FluidStack, ItemStack>>> results, int maxItemOutputs, int maxFluidOutputs) {
        this.RESULTS = results;
        this.maxItemOutputs = maxItemOutputs;
        this.maxFluidOutputs = maxFluidOutputs;
        return this;
    }

    private void keyCheck() {
        if (FLUID_INGREDIENTS == null && ITEM_INGREDIENTS == null && INGREDIENTS == null) {
            throw new KubeRuntimeException("Recipe must have at least one ingredient type defined");
        }
        if (FLUID_RESULTS == null && ITEM_RESULTS == null && RESULTS == null) {
            throw new KubeRuntimeException("Recipe must have at least one result type defined");
        }
        if (INGREDIENTS != null && (ITEM_INGREDIENTS != null || FLUID_INGREDIENTS != null)) {
            throw new KubeRuntimeException("Recipe cannot have both paired ingredients and separate item/fluid ingredients defined");
        }
        if (RESULTS != null && (ITEM_RESULTS != null || FLUID_RESULTS != null)) {
            throw new KubeRuntimeException("Recipe cannot have both paired results and separate item/fluid results defined");
        }
    }

    private void handleFluidIngredients(RecipeSchema recipe, ComponentValueMap from) {

    }
}
