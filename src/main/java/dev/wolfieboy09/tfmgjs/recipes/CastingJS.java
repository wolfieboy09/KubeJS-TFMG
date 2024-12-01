package dev.wolfieboy09.tfmgjs.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import net.minecraft.world.item.crafting.Ingredient;


public class CastingJS extends RecipeJS {
    @Override
    public InputFluid readInputFluid(Object from) {
        if (from instanceof JsonObject json) {
            Ingredient ingredient;
            int amount = 1;

            if (json.has("value")) {
                ingredient = Ingredient.fromJson(json.get("value"));
            } else {
                ingredient = Ingredient.fromJson(json);
            }

            if (json.has("count")) {
                amount = json.get("count").getAsInt();
            } else if (json.has("amount"))  {
                amount = json.get("amount").getAsInt();
            }
            return InputFluid.of(ingredient, amount);
        }
        return super.readInputFluid(from);
    }

    @Override
    public OutputItem readOutputItem(Object from) {
        return super.readOutputItem(from);
    }

    @Override
    public JsonElement writeInputFluid(InputFluid value) {
        if (value instanceof FluidIngredient fluid) {
            return fluid.toJson();
        } else {
            return FluidIngredient.EMPTY.toJson();
        }
    }


    @Override
    public JsonElement writeOutputFluid(OutputFluid value) {
        return super.writeOutputFluid(value);
    }
}
