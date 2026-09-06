package dev.wolfieboy09.tfmgjs.wrappers;

import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.operations.VatOperation;
import com.drmangotea.tfmg.registry.TFMGVatOperations;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.resources.ResourceLocation;

public interface VatOperationWrapper {
    TypeInfo TYPE_INFO = TypeInfo.of(VatOperation.class);

    static boolean isVatOperationLike(Object from) {
        return from instanceof VatOperation;
    }

    static VatOperation wrapVatOperation(Context cx, Object from) {
        // Maybe null and default be something else?
        return switch (from) {
            case ResourceLocation id -> TFMGRegistries.VAT_OPERATION_REGISTRY.get(id);
            case null, default -> TFMGVatOperations.NONE.get();
        };
    }
}
