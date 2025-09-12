package dev.wolfieboy09.tfmgjs.recipes.schemas.vat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.recipe.BlockTagIngredient;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.item.ingredient.TagContext;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.MapJS;
import dev.wolfieboy09.tfmgjs.recipes.helpers.CreateInputFluid;
import dev.wolfieboy09.tfmgjs.recipes.helpers.FluidIngredientHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

@SuppressWarnings("unused")
public interface VatRecipeSchema {
    RecipeKey<Either<OutputFluid, OutputItem>[]> RESULTS = FluidComponents.OUTPUT_OR_ITEM_ARRAY.key("results");
    RecipeKey<Either<InputFluid, InputItem>[]> INGREDIENTS = FluidComponents.INPUT_OR_ITEM_ARRAY.key("ingredients");

    RecipeKey<String[]> MACHINES = StringComponent.ID.asArray().key("machines").optional(new String[]{}).allowEmpty();
    RecipeKey<String[]> VAT_TYPES = StringComponent.ID.asArray().key("allowedVatTypes").optional(new String[]{}).allowEmpty();
    RecipeKey<Integer> MIN_SIZE = NumberComponent.INT.key("minSize").optional(1).allowEmpty();

    RecipeKey<HeatCondition> HEAT_REQUIREMENT = new EnumComponent<>(HeatCondition.class)
            .key("heatRequirement")
            .optional(HeatCondition.NONE)
            .allowEmpty();

    RecipeKey<Long> PROCESSING_TIME_REQUIRED = TimeComponent.TICKS.key("processingTime").optional(100L).alwaysWrite();

    class VatRecipeJS extends RecipeJS {
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
            return setValue(HEAT_REQUIREMENT, HeatCondition.HEATED);
        }

        public RecipeJS superheated() {
            return setValue(HEAT_REQUIREMENT, HeatCondition.SUPERHEATED);
        }

        public RecipeJS machines(String... machines) {
            return setValue(MACHINES, machines);
        }

        public RecipeJS allowedVatTypes(String... types) {
            return setValue(VAT_TYPES, types);
        }

        public RecipeJS allowAllVatTypes() {
            return setValue(VAT_TYPES, new String[]{"tfmg:steel_vat", "tfmg:cast_iron_vat", "tfmg:firebrick_lined_vat"});
        }

        public RecipeJS minSize(int size) {
            return setValue(MIN_SIZE, size);
        }

        public RecipeJS processingTime(int time) {
            return setValue(PROCESSING_TIME_REQUIRED, (long) time);
        }
    }

    RecipeSchema VAT = new RecipeSchema(VatRecipeJS.class, VatRecipeJS::new, INGREDIENTS, RESULTS, MACHINES, VAT_TYPES, MIN_SIZE, PROCESSING_TIME_REQUIRED, HEAT_REQUIREMENT)
            .constructor(new VatRecipeFactory().factory, INGREDIENTS, RESULTS);
}
