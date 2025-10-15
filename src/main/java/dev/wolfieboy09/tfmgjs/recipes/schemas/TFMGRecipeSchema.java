package dev.wolfieboy09.tfmgjs.recipes.schemas;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.wolfieboy09.tfmgjs.recipes.helpers.CreateInputFluid;
import dev.wolfieboy09.tfmgjs.recipes.helpers.FluidIngredientHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

public interface TFMGRecipeSchema {
    RecipeKey<List<Either<SizedFluidIngredient, SizedIngredient>>> RESULTS = SizedFluidIngredientComponent.FLAT.instance().or(SizedIngredientComponent.SIZED_INGREDIENT.instance()).asList().outputKey("results");
    RecipeKey<List<FluidStack>> FLUID_RESULTS = FluidComponents.OUTPUT_ARRAY.key("results");
    RecipeKey<FluidStack[]> ITEM_RESULTS = ItemComponents.OUTPUT_ARRAY.key("results");

    RecipeKey<Either<FluidStack, ItemStack>[]> INGREDIENTS = FluidComponents.INPUT_OR_ITEM_ARRAY.key("ingredients");
    RecipeKey<FluidStack[]> FLUID_INGREDIENTS = FluidComponents.INPUT_ARRAY.key("ingredients");
    RecipeKey<FluidStack[]> ITEM_INGREDIENTS = ItemComponents.INPUT_ARRAY.key("ingredients");

    RecipeKey<Either<FluidStack, InputItem>[]> INGREDIENTS_UNWRAPPED = new RecipeComponentWithParent<Either<FluidStack, InputItem>[]>() {
        @Override
        public RecipeComponent<Either<FluidStack, InputItem>[]> parentComponent() {
            return INGREDIENTS.component;
        }

        @Override
        public JsonElement write(RecipeJS recipe, Either<FluidStack, InputItem>[] value) {
            // during writing, unwrap all stacked input items
            JsonArray json = new JsonArray();
            for (var either : value) {
                either.ifLeft(fluid -> json.add(FluidComponents.INPUT.write(recipe, fluid)))
                        .ifRight(item -> {
                            for (InputItem unwrapped : item.unwrap()) {
                                json.add(ItemStackComponent.INPUT.write(recipe, unwrapped));
                            }
                        });
            }
            return json;
        }
    }.key("ingredients");

    RecipeKey<FluidStack[]> FLUID_INGREDIENT_UNWRAPPED = new RecipeComponentWithParent<FluidStack[]>() {
        @Override
        public RecipeComponent<FluidStack[]> parentComponent() {
            return FLUID_INGREDIENTS.component;
        }

        @Override
        public JsonElement write(RecipeJS recipe, InputFluid[] values) {
            JsonArray json = new JsonArray();
            for (InputFluid unwrapped : values) {
                json.add(FluidComponents.INPUT.write(recipe, unwrapped));
            }
            return json;
        }
    }.key("ingredients");

    RecipeKey<InputItem[]> ITEM_INGREDIENT_UNWRAPPED = new RecipeComponentWithParent<InputItem[]>() {
        @Override
        public RecipeComponent<InputItem[]> parentComponent() {
            return ITEM_INGREDIENTS.component;
        }

        @Override
        public JsonElement write(RecipeJS recipe, InputItem[] value) {
            JsonArray json = new JsonArray();
            for (InputItem item : value) {
                for (InputItem unwrapped : item.unwrap()) {
                    json.add(ItemComponents.INPUT.write(recipe, unwrapped));
                }
            }
            return json;
        }
    }.key("ingredients");

    RecipeKey<Long> PROCESSING_TIME = TimeComponent.TICKS.key("processingTime").optional(100L);
    // specifically for crushing, cutting and milling
    RecipeKey<Long> PROCESSING_TIME_REQUIRED = TimeComponent.TICKS.key("processingTime").optional(100L).alwaysWrite();

    RecipeKey<Integer> ENERGY = NumberComponent.INT.key("energy");

    RecipeKey<Integer> RPM_MAX = NumberComponent.INT.key("rpm_max").optional(1);
    RecipeKey<Integer> RPM_MIN = NumberComponent.INT.key("rpm_min").optional(1);

    RecipeKey<Float> POWER = NumberComponent.FLOAT.key("power").optional(1.0f);



    RecipeKey<String> HEAT_REQUIREMENT = new StringComponent("not a valid heat condition!", s -> {
        for (var h : HeatCondition.values()) {
            if (h.name().equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }).key("heatRequirement").defaultOptional().allowEmpty();

    // specifically used in item application
    RecipeKey<Boolean> KEEP_HELD_ITEM = BooleanComponent.BOOLEAN.key("keepHeldItem").optional(false);

    class ProcessingRecipeJS extends RecipeJS {
        @Override
        public InputFluid readInputFluid(Object from) {
            if (from instanceof CreateInputFluid fluid) {
                return fluid;
            } else if (from instanceof FluidIngredient fluid) {
                return new CreateInputFluid(fluid);
            } else if (from instanceof FluidStackJS fluid) {
                return new CreateInputFluid(FluidIngredientHelper.toFluidIngredient(fluid));
            } else if (from instanceof FluidStack fluid) {
                return new CreateInputFluid(FluidIngredient.fromFluidStack(fluid));
            } else {
                var json = MapJS.json(from);
                if (json != null) {
                    return new CreateInputFluid(FluidIngredient.deserialize(json));
                }
                return CreateInputFluid.EMPTY;
            }
        }

        @Override
        public JsonElement writeInputFluid(InputFluid value) {
            if (value instanceof CreateInputFluid fluid) {
                return fluid.ingredient().serialize();
            } else if (value instanceof FluidIngredient fluid) {
                return fluid.serialize();
            } else if (value instanceof FluidStackJS fluid) {
                return FluidIngredientHelper.toFluidIngredient(fluid).serialize();
            } else {
                return FluidIngredient.EMPTY.serialize();
            }
        }

        @Override
        public boolean inputItemHasPriority(Object from) {
            if (from instanceof InputItem || from instanceof Ingredient || from instanceof ItemStack) {
                return true;
            }

            var input = readInputItem(from);
            if (input.ingredient instanceof BlockTagIngredient blockTag) {
                return !TagContext.INSTANCE.getValue().isEmpty(blockTag.getTag());
            }

            return !input.isEmpty();
        }

        @Override
        public boolean inputFluidHasPriority(Object from) {
            return from instanceof InputFluid || FluidIngredient.isFluidIngredient(MapJS.json(from));
        }

        @Override
        public OutputItem readOutputItem(Object from) {
            if (from instanceof ProcessingOutput output) {
                return OutputItem.of(output.getStack(), output.getChance());
            } else {
                var outputItem = super.readOutputItem(from);
                if (from instanceof JsonObject j && j.has("chance")) {
                    return outputItem.withChance(j.get("chance").getAsFloat());
                }
                return outputItem;
            }
        }

        public RecipeJS heated() {
            return setValue(HEAT_REQUIREMENT, HeatCondition.HEATED.serialize());
        }

        public RecipeJS superheated() {
            return setValue(HEAT_REQUIREMENT, HeatCondition.SUPERHEATED.serialize());
        }
    }

    class ItemApplicationRecipeJS extends ProcessingRecipeJS {
        public RecipeJS keepHeldItem() {
            return setValue(KEEP_HELD_ITEM, true);
        }
    }

    RecipeSchema PROCESSING_DEFAULT = new RecipeSchema(ProcessingRecipeJS.class, ProcessingRecipeJS::new, RESULTS, INGREDIENTS, PROCESSING_TIME, HEAT_REQUIREMENT);

    RecipeSchema PROCESSING_WITH_TIME = new RecipeSchema(ProcessingRecipeJS.class, ProcessingRecipeJS::new, RESULTS, INGREDIENTS, PROCESSING_TIME_REQUIRED, HEAT_REQUIREMENT);

    RecipeSchema PROCESSING_WITH_ENERGY = new RecipeSchema(ProcessingRecipeJS.class, ProcessingRecipeJS::new, RESULTS, INGREDIENTS, ENERGY, PROCESSING_TIME, HEAT_REQUIREMENT);

    RecipeSchema PROCESSING_UNWRAPPED = new RecipeSchema(ProcessingRecipeJS.class, ProcessingRecipeJS::new, RESULTS, INGREDIENTS_UNWRAPPED, PROCESSING_TIME, HEAT_REQUIREMENT);

    RecipeSchema ITEM_APPLICATION = new RecipeSchema(ItemApplicationRecipeJS.class, ItemApplicationRecipeJS::new, RESULTS, INGREDIENTS, PROCESSING_TIME, HEAT_REQUIREMENT, KEEP_HELD_ITEM);

//    RecipeSchema CASTING = new RecipeSchema(FLUID_INGREDIENTS, RESULTS, PROCESSING_TIME)
//            .constructor(new TFMGRecipeFactory().fluidInputs(FLUID_INGREDIENTS, 1).itemOutputs(ITEM_RESULTS, 1).hasTime().factory, FLUID_INGREDIENTS, RESULTS, PROCESSING_TIME);
    RecipeSchema COKING = new RecipeSchema(ProcessingRecipeJS.class, ProcessingRecipeJS::new, ITEM_INGREDIENTS, RESULTS, PROCESSING_TIME)
            .constructor(new TFMGRecipeFactory().itemInputs(ITEM_INGREDIENTS, 1).results(RESULTS, 1, 2).hasTime().factory, ITEM_INGREDIENTS, RESULTS, PROCESSING_TIME);
    RecipeSchema DISTILLATION = new RecipeSchema(FLUID_INGREDIENTS, FLUID_RESULTS)
            .constructor(new TFMGRecipeFactory().fluidInputs(FLUID_INGREDIENTS, 1).fluidOutputs(FLUID_RESULTS, 6).factory, FLUID_INGREDIENTS, FLUID_RESULTS);
    RecipeSchema INDUSTRIAL_BLASTING = new RecipeSchema(ITEM_INGREDIENTS, FLUID_RESULTS, PROCESSING_TIME)
            .constructor(new TFMGRecipeFactory().itemInputs(ITEM_INGREDIENTS, 2).fluidOutputs(FLUID_RESULTS, 3).hasTime().factory, ITEM_INGREDIENTS, FLUID_RESULTS, PROCESSING_TIME);
    RecipeSchema POLARIZING = new RecipeSchema(ITEM_INGREDIENTS, ITEM_RESULTS, ENERGY)
            .constructor(new TFMGRecipeFactory().itemInputs(ITEM_INGREDIENTS, 1).itemOutputs(ITEM_RESULTS, 1).usesEnergy().factory, ITEM_INGREDIENTS, ITEM_RESULTS, ENERGY);
    RecipeSchema WINDING = new RecipeSchema(ITEM_INGREDIENTS, ITEM_RESULTS, PROCESSING_TIME)
            .constructor(new TFMGRecipeFactory().itemInputs(ITEM_INGREDIENTS, 2).itemOutputs(ITEM_RESULTS, 1).hasTime().factory, ITEM_INGREDIENTS, ITEM_RESULTS, PROCESSING_TIME);
    RecipeSchema HOT_BLASTING = new RecipeSchema(FLUID_INGREDIENTS, FLUID_RESULTS, PROCESSING_TIME)
            .constructor(new TFMGRecipeFactory().fluidInputs(FLUID_INGREDIENTS, 2).fluidOutputs(FLUID_RESULTS, 2).hasTime().factory, FLUID_INGREDIENTS, FLUID_RESULTS, PROCESSING_TIME);
}
