package dev.wolfieboy09.tfmgjs.wrappers;

import com.drmangotea.tfmg.TFMGRegistries;
import com.drmangotea.tfmg.content.machinery.vat.base.registry.operations.VatOperation;
import com.drmangotea.tfmg.registry.TFMGVatOperations;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.resources.ResourceLocation;

public interface VatOperationWrapper {
    TypeInfo TYPE_INFO = TypeInfo.of(VatOperation.class);

    static boolean isVatOperationLike(Object from) {
        return from instanceof VatOperation;
    }

    static VatOperation wrapVatOperation(Context cx, Object from) {
        return switch (from) {
            case null -> TFMGVatOperations.NONE.get();
            case VatOperation id -> id;
            case ResourceLocation id -> TFMGRegistries.VAT_OPERATION_REGISTRY.get(id);
            case String id -> {
                ResourceLocation rl = ResourceLocation.tryParse(id);
                VatOperation op = rl != null ? TFMGRegistries.VAT_OPERATION_REGISTRY.get(rl) : null;
                if (op == null) {
                    throw new KubeRuntimeException("Unknown vat operation %s".formatted(id));
                }
                yield op;
            }
            default -> throw new KubeRuntimeException("Failed to read vat operation %s".formatted(from)).source(SourceLine.of(cx));
        };
    }
}
