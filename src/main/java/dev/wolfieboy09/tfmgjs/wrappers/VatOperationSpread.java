package dev.wolfieboy09.tfmgjs.wrappers;

import com.drmangotea.tfmg.content.machinery.vat.base.registry.operations.VatOperation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record VatOperationSpread(VatOperation operation, int amount) {
    @Contract("_ -> new")
    public static @NotNull VatOperationSpread of(VatOperation operation) {
        return new VatOperationSpread(operation, 1);
    }

    @Contract("_, _ -> new")
    public static @NotNull VatOperationSpread of(VatOperation operation, int amount) {
        return new VatOperationSpread(operation, amount);
    }
}
