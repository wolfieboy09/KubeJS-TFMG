package dev.wolfieboy09.tfmgjs.recipes.helpers;

import com.simibubi.create.foundation.fluid.FluidIngredient;
import dev.architectury.hooks.fluid.forge.FluidStackHooksForge;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;

public class FluidIngredientHelper {
    public static FluidIngredient toFluidIngredient(FluidStackJS fluidStack) {
        return FluidIngredient.fromFluidStack(FluidStackHooksForge.toForge(fluidStack.getFluidStack()));
    }
}