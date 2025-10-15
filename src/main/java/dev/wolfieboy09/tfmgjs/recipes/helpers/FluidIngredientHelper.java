package dev.wolfieboy09.tfmgjs.recipes.helpers;

import com.simibubi.create.foundation.fluid.FluidIngredient;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class FluidIngredientHelper {
    @Contract("_ -> new")
    public static @NotNull FluidIngredient toFluidIngredient(@NotNull FluidStackJS fluidStack) {
        return FluidIngredient.fromFluidStack(FluidStackHooksForge.toForge(fluidStack.getFluidStack()));
    }
}