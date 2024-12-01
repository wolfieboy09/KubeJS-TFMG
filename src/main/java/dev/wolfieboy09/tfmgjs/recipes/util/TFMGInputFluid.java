package dev.wolfieboy09.tfmgjs.recipes.util;

import dev.architectury.fluid.FluidStack;
import dev.latvian.mods.kubejs.fluid.FluidLike;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record TFMGInputFluid(FluidStack input) implements InputFluid {
    @Override
    public long kjs$getAmount() {
        return input.getAmount();
    }

    @Override
    public boolean kjs$isEmpty() {
        return input.isEmpty();
    }

    @Contract("_ -> new")
    @Override
    public @NotNull FluidLike kjs$copy(long amount) {
        return new TFMGInputFluid(input.copyWithAmount(amount));
    }
}
